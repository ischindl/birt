/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.birt.sdk;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Ignore;

@SuppressWarnings("javadoc")
@Ignore
public class RuntimeOSGiTest extends BaseTestTemplate {

	static {
		// KRITICKÉ PRE Java 21
		forceJava21OSGiCompatibility();
	}

	private static void forceJava21OSGiCompatibility() {
		System.out.println("=== FORCING Java 21 OSGi Compatibility ===");
		System.out.println("Java version: " + System.getProperty("java.version"));

		// 1. VYPNÚŤ plurl COMPLETELY
		System.setProperty("org.eclipse.equinox.plurl.disable", "true");
		System.setProperty("equinox.plurl.disabled", "true");

		// 2. Nastaviť boot delegation pre java.net
		System.setProperty("org.osgi.framework.bootdelegation", "java.net.*,sun.*,javax.*,jdk.*,com.sun.*,sun.misc.*");

		// 3. OSGi parent classloader
		System.setProperty("osgi.parentClassloader", "app");
		System.setProperty("osgi.compatibility.bootdelegation", "true");

		// 4. Equinox specific
		System.setProperty("eclipse.ignoreApp", "true");
		System.setProperty("equinox.resolve.constraint", "false");
		System.setProperty("org.eclipse.osgi.framework.internal.core.FrameworkContext", "");


		// 6. Debug
		System.setProperty("org.eclipse.osgi.framework.debug", "false");
		System.setProperty("osgi.console", "none");
	}
	private ClassLoader osgiClassLoader = null;
	private Object platformLauncher = null;

	@Override
	protected Class<?> getClass(String bundle, String className) throws Exception {
		System.out.println("OSGi getClass: " + bundle + " -> " + className);

		// Ensure OSGi platform is started
		initOSGiPlatform();

		// Get the bundle
		Object bundleObj = getBundle(bundle);
		if (bundleObj == null) {
			throw new RuntimeException("Bundle not found: " + bundle);
		}

		// Load class from bundle
		return loadClassFromBundle(bundleObj, className);
	}

	/**
	 * Initialize OSGi platform
	 */
	private synchronized void initOSGiPlatform() throws Exception {
		if (platformLauncher != null) {
			return; // Already initialized
		}

		System.out.println("Initializing OSGi platform for Java 21...");

		// Set BIRT_HOME
		File birtHome = new File("./target/birt-runtime-osgi/ReportEngine/platform/");
		System.setProperty("BIRT_HOME", birtHome.getAbsolutePath());
		System.out.println("BIRT_HOME: " + birtHome.getAbsolutePath());

		// Create classloader with OSGi dependencies
		ClassLoader loader = createOSGiClassLoader();

		// Start the Platform to start the Equinox framework
		Class<?> platformClass = loader.loadClass("org.eclipse.birt.core.framework.Platform");

		try {
			// Try Java 9+ MethodHandle approach
			MethodHandle startup = MethodHandles.lookup().findStatic(platformClass, "startup",
					MethodType.methodType(void.class));
			startup.invoke();
		} catch (Throwable e) {
			// Fallback to reflection
			System.out.println("MethodHandle failed, using reflection: " + e.getMessage());
			Method startupMethod = platformClass.getMethod("startup");
			startupMethod.invoke(null);
		}

		// Get the launcher from the started Platform
		Field launcherField = platformClass.getDeclaredField("launcher");
		launcherField.setAccessible(true);
		platformLauncher = launcherField.get(null);

		System.out.println("OSGi platform initialized successfully");
	}

	/**
	 * Create classloader for OSGi runtime
	 */
	private ClassLoader createOSGiClassLoader() throws Exception {
		if (osgiClassLoader != null) {
			return osgiClassLoader;
		}

		List<URL> urls = new ArrayList<>();

		// Add OSGi framework JARs
		File libDir = new File("./target/birt-runtime-osgi/ReportEngine/lib");
		if (libDir.exists() && libDir.isDirectory()) {
			File[] jars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
			if (jars != null) {
				for (File jar : jars) {
					urls.add(jar.toURI().toURL());
					System.out.println("Added to classpath: " + jar.getName());
				}
			}
		}

		// Add plugins directory (OSGi bundles)
		File pluginsDir = new File("./target/birt-runtime-osgi/ReportEngine/platform/plugins");
		if (pluginsDir.exists() && pluginsDir.isDirectory()) {
			File[] jars = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
			if (jars != null) {
				for (File jar : jars) {
					urls.add(jar.toURI().toURL());
					System.out.println("Added to classpath: " + jar.getName());
				}
			}
		}

		// Add configuration directory
		File configDir = new File("./target/birt-runtime-osgi/ReportEngine/platform/configuration");
		if (configDir.exists()) {
			urls.add(configDir.toURI().toURL());
		}

		// Create classloader with current classloader as parent
		osgiClassLoader = new OSGiClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

		// Set as context classloader
		Thread.currentThread().setContextClassLoader(osgiClassLoader);

		return osgiClassLoader;
	}

	/**
	 * Get bundle from OSGi platform
	 */
	private Object getBundle(String bundleSymbolicName) throws Exception {
		if (platformLauncher == null) {
			throw new IllegalStateException("OSGi platform not initialized");
		}

		// Try different method signatures
		try {
			Method getBundleMethod = platformLauncher.getClass().getMethod("getBundle", String.class);
			return getBundleMethod.invoke(platformLauncher, bundleSymbolicName);
		} catch (NoSuchMethodException e) {
			// Try alternative method name
			Method getBundleMethod = platformLauncher.getClass().getMethod("findBundle", String.class);
			return getBundleMethod.invoke(platformLauncher, bundleSymbolicName);
		}
	}

	/**
	 * Load class from OSGi bundle
	 */
	private Class<?> loadClassFromBundle(Object bundle, String className) throws Exception {
		try {
			// Try standard loadClass method
			Method loadClassMethod = bundle.getClass().getMethod("loadClass", String.class);
			return (Class<?>) loadClassMethod.invoke(bundle, className);
		} catch (NoSuchMethodException e) {
			// Try alternative
			Method loadClassMethod = bundle.getClass().getMethod("getBundleLoader");
			Object bundleLoader = loadClassMethod.invoke(bundle);

			Method findClassMethod = bundleLoader.getClass().getMethod("findClass", String.class);
			return (Class<?>) findClassMethod.invoke(bundleLoader, className);
		}
	}

	/**
	 * Custom ClassLoader for OSGi compatibility on Java 21
	 */
	private static class OSGiClassLoader extends URLClassLoader {

		static {
			registerAsParallelCapable();
		}

		public OSGiClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			// Special handling for Java 21 module system
			if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")
					|| name.startsWith("sun.") || name.startsWith("com.sun.")) {
				return super.loadClass(name, resolve);
			}

			// For OSGi-related classes, try to load from this classloader first
			synchronized (getClassLoadingLock(name)) {
				// First, check if already loaded
				Class<?> c = findLoadedClass(name);
				if (c != null) {
					if (resolve) {
						resolveClass(c);
					}
					return c;
				}

				try {
					// Try to find in this classloader
					c = findClass(name);
					if (resolve) {
						resolveClass(c);
					}
					return c;
				} catch (ClassNotFoundException e) {
					// Delegate to parent
					return super.loadClass(name, resolve);
				}
			}
		}

		@Override
		public URL getResource(String name) {
			// For OSGi, we need to handle resource loading differently
			URL url = findResource(name);
			if (url == null) {
				url = super.getResource(name);
			}
			return url;
		}
	}

	/**
	 * Cleanup OSGi platform after tests
	 */
	public void cleanup() throws Exception {
		if (platformLauncher != null) {
			try {
				ClassLoader loader = platformLauncher.getClass().getClassLoader();
				Class<?> platformClass = loader.loadClass("org.eclipse.birt.core.framework.Platform");

				Method shutdownMethod = platformClass.getMethod("shutdown");
				shutdownMethod.invoke(null);

				System.out.println("OSGi platform shutdown");
			} catch (Exception e) {
				System.err.println("Error shutting down OSGi platform: " + e.getMessage());
			} finally {
				platformLauncher = null;
				osgiClassLoader = null;
			}
		}
	}

	// Helper method for tests that need direct access to OSGi services
	protected Object getOSGiService(String serviceClassName) throws Exception {
		initOSGiPlatform();

		// Get bundle context from launcher
		Method getBundleContextMethod = platformLauncher.getClass().getMethod("getBundleContext");
		Object bundleContext = getBundleContextMethod.invoke(platformLauncher);

		// Get service reference
		Class<?> bcClass = bundleContext.getClass();
		Method getServiceReferenceMethod = bcClass.getMethod("getServiceReference", String.class);
		Object serviceRef = getServiceReferenceMethod.invoke(bundleContext, serviceClassName);

		if (serviceRef == null) {
			throw new RuntimeException("Service not found: " + serviceClassName);
		}

		// Get service instance
		Method getServiceMethod = bcClass.getMethod("getService", serviceRef.getClass());
		return getServiceMethod.invoke(bundleContext, serviceRef);
	}

	@After
	public void tearDown() throws Exception {
		cleanup();
	}

	public void initBirtClassLoader() {
	}

	@Override
	public String getRuntimePath() {
		return "./target/birt-runtime-osgi";
	}
}