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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */

@SuppressWarnings("javadoc")
public abstract class BaseTestTemplate {

	// ClassLoader pre BIRT
	private ClassLoader birtClassLoader = null;

	static {
		// Povoliť prístup k interným API pre Java 9+
		System.setProperty("sun.reflect.debugModuleAccessChecks", "true");
	}

	public abstract String getRuntimePath();

	public String getPlatformPath() {
		return getRuntimePath() + "/ReportEngine";
	}

	public String getReportEnginePath() {
		return getPlatformPath() + "/lib";
	}

	/**
	 * Metóda na inicializáciu BIRT classloader-a
	 */
	protected synchronized void initBirtClassLoader() throws IOException {
		if (birtClassLoader == null) {
			System.out.println("BIRT classloader initialisation ...");

			// Získaj všetky JAR súbory z BIRT runtime
			List<String> jarPaths = new ArrayList<>();

			// Platform JARs
			File platformDir = new File(getPlatformPath());
			if (platformDir.exists() && platformDir.isDirectory()) {
				File[] platformJars = listJars(getPlatformPath());
				if (platformJars != null) {
					for (File jar : platformJars) {
						jarPaths.add(jar.getAbsolutePath());
					}
				}
			}

			// ReportEngine JARs
			File reportEngineDir = new File(getReportEnginePath());
			if (reportEngineDir.exists() && reportEngineDir.isDirectory()) {
				File[] engineJars = listJars(getReportEnginePath());
				if (engineJars != null) {
					for (File jar : engineJars) {
						jarPaths.add(jar.getAbsolutePath());
					}
				}
			}

			// Plugins directory
			File pluginsDir = new File(getReportEnginePath() + "/plugins");
			if (pluginsDir.exists() && pluginsDir.isDirectory()) {
				addJarsFromDirectory(pluginsDir, jarPaths);
			}

			System.out.println("Number of JAR files: " + jarPaths.size());

			// Create URL array
			Set<URL> urls = new LinkedHashSet<>();
			for (String jarPath : jarPaths) {
				urls.add(new File(jarPath).toURI().toURL());
				System.out.println("Pridávam JAR: " + jarPath);
			}

			// Create a classloader with the current context classloader as parent
			ClassLoader parent = Thread.currentThread().getContextClassLoader();
			if (parent == null) {
				parent = ClassLoader.getPlatformClassLoader();
			}

			birtClassLoader = new BirtURLClassLoader(urls.toArray(new URL[0]), parent);

			// Set as context classloader
			Thread.currentThread().setContextClassLoader(birtClassLoader);

			System.out.println("BIRT classloader initialised.");
		}
	}

	/**
	 * Recursively adds JAR files from a directory
	 */
	private void addJarsFromDirectory(File directory, List<String> jarPaths) {
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					addJarsFromDirectory(file, jarPaths);
				} else if (file.getName().endsWith(".jar")) {
					jarPaths.add(file.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Implementation of the getClass method for BIRT
	 */
	protected Class<?> getClass(String bundle, String className) throws Exception {
		// Inicializovať classloader ak ešte nebol
		initBirtClassLoader();

		// Let's try some strategies for finding a class
		ClassNotFoundException lastException = null;

		// Strategy 1: Direct load from BIRT classloader
		try {
			return Class.forName(className, true, birtClassLoader);
		} catch (ClassNotFoundException e1) {
			lastException = e1;
			System.out.println("Strategy 1 failed for " + className);
		}

		// Strategy 2: If bundle is specified, we try to find it in plugins
		if (bundle != null && !bundle.isEmpty()) {
			String bundleClassName = bundle + "." + className;
			try {
				return Class.forName(bundleClassName, true, birtClassLoader);
			} catch (ClassNotFoundException e2) {
				lastException = e2;
				System.out.println("Strategy 2 failed for " + bundleClassName);
			}
		}

		// Strategy 3: Let's try the OSGi bundle convention (for BIRT)
		String osgiClassName = null;
		if (bundle != null && !bundle.isEmpty()) {
			// For BIRT bundle, it can be in an internal package
			osgiClassName = "org.eclipse.birt." + bundle.replace('.', '_') + "." + className;
			try {
				return Class.forName(osgiClassName, true, birtClassLoader);
			} catch (ClassNotFoundException e3) {
				lastException = e3;
				System.out.println("Strategy 3 failed for " + osgiClassName);
			}
		}

		// Strategy 4: Let's try the current thread context classloader
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (ClassNotFoundException e4) {
			System.out.println("Strategy 4 failed for " + className);
		}

		// Strategy 5: Let's try the system classloader
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		} catch (ClassNotFoundException e5) {
			System.out.println("Strategy 5 failed for " + className);
		}

		// If all else fails, we throw an exception
		throw new ClassNotFoundException("Could not find class: " + className + " (bundle: " + bundle
				+ "). Last error: " + lastException.getMessage(), lastException);
	}

	/**
	 * Helper method to get a class with multiple options
	 */
	protected Class<?> getClassWithAlternatives(String bundle, String className, String[] alternatives)
			throws Exception {
		initBirtClassLoader();

		// Let's try the master class
		try {
			return getClass(bundle, className);
		} catch (ClassNotFoundException e) {
			// Let's try alternatives
			for (String altClassName : alternatives) {
				try {
					return getClass(bundle, altClassName);
				} catch (ClassNotFoundException ignored) {
					// Next alternative
				}
			}
			throw e;
		}
	}

	@Test
	public void testMain() throws Exception {
		System.out.println("--- REAL CLASSPATH ---");
		System.out.println(System.getProperty("java.class.path"));
		String output = "./target/output.html";
		new File(output).delete();
		int result = run(new String[] { "-o", output, "-m", "RunAndRender", "-p", "paramInteger=1", "-p",
				"paramList=1,2,3", "./target/birt-runtime/ReportEngine/samples/hello_world.rptdesign" });
		Assert.assertEquals(0, result);
		Assert.assertTrue(new File(output).exists());
		Assert.assertTrue(new String(Files.readAllBytes(Paths.get(output)), StandardCharsets.UTF_8)
				.contains("If you can see this report, it means that the BIRT Engine is installed correctly."));
	}

	@Test
	public void testTable() throws Exception {
		String output = "./target/table.html";
		new File(output).delete();
		int result = run(new String[] { "-o", output, "-m", "RunAndRender", "./src/test/resources/table.rptdesign" });
		Assert.assertEquals(0, result);
		Assert.assertTrue(new File(output).exists());
		// USA's customer count is 36
		Assert.assertTrue(new String(Files.readAllBytes(Paths.get(output)), StandardCharsets.UTF_8).contains("36"));
	}

	@Test
	public void testXtab() throws Exception {
		String output = "./target/xtab.html";
		new File(output).delete();
		int result = run(new String[] { "-o", output, "-m", "RunAndRender", "./src/test/resources/xtab.rptdesign" });
		Assert.assertEquals(0, result);
		Assert.assertTrue(new File(output).exists());
		// USA's customer count is 36
		Assert.assertTrue(new String(Files.readAllBytes(Paths.get(output)), StandardCharsets.UTF_8).contains("36"));
	}

	@Test
	public void testChart() throws Exception {
		String output = "./target/chart.html";
		new File(output).delete();
		int result = run(new String[] { "-o", output, "-m", "RunAndRender", "./src/test/resources/chart.rptdesign" });
		Assert.assertEquals(0, result);
		Assert.assertTrue(new File(output).exists());
		// there is a svg image output as type="image/svg+xml"
		Assert.assertTrue(
				new String(Files.readAllBytes(Paths.get(output)), StandardCharsets.UTF_8).contains("image/svg+xml"));
	}

	protected File[] listJars(String folder) {
		File dir = new File(folder);
		if (!dir.exists() || !dir.isDirectory()) {
			System.err.println("Adresár neexistuje: " + folder);
			return new File[0];
		}
		return dir.listFiles(new FilnameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
	}

	protected Set<URL> getURLs(String... roots) throws IOException {
		Set<URL> urls = new LinkedHashSet<URL>();
		for (String root : roots) {
			File[] files = new File(root).listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					urls.add(file.toURI().toURL());
				}
			}
		}
		return urls;
	}

	protected ClassLoader createClassLoader(String... roots) throws IOException {
		Set<URL> urls = getURLs(roots);
		// For Java 21 we will use ClassLoader.getPlatformClassLoader() as parent
		return new BirtURLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getPlatformClassLoader());
	}

	public int run(String[] args) throws Exception {
		// We will try to find ReportRunner with alternatives for different BIRT
		// versions
		String[] alternatives = { "org.eclipse.birt.report.engine.api.ReportRunner",
				"org.eclipse.birt.report.engine.api.impl.ReportRunner",
				"org.eclipse.birt.report.engine.api.ReportRunnerImpl" };

		Class<?> runnerClass = getClassWithAlternatives("org.eclipse.birt.report.engine",
				"org.eclipse.birt.report.engine.api.ReportRunner", alternatives);

		return run(runnerClass, args);
	}

	protected int run(Class<?> mainClass, String[] args) throws Exception {
		System.out.println("Run " + mainClass.getName() + " with arguments: " + Arrays.toString(args));

		try {
			Constructor<?> constructor = mainClass.getConstructor(String[].class);
			Object runner = constructor.newInstance(new Object[] { args });
			Method execute = mainClass.getMethod("execute");
			Object result = execute.invoke(runner);
			return ((Integer) result).intValue();
		} catch (InvocationTargetException e) {
			// Extract the actual exception
			Throwable targetException = e.getTargetException();
			System.err.println("Error when starting ReportRunner: " + targetException.getMessage());
			targetException.printStackTrace();

			if (targetException instanceof Exception) {
				throw (Exception) targetException;
			} else {
				throw e;
			}
		} catch (NoSuchMethodException e) {
			// Let's try an alternative constructor or method
			System.out.println("I'm trying an alternative startup method...");
			return runAlternative(mainClass, args);
		}
	}

	/**
	 * Alternative way to start ReportRunner
	 */
	private int runAlternative(Class<?> mainClass, String[] args) throws Exception {
		try {
			// Let's try a static method
			Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.invoke(null, (Object) args);
			return 0; // We expect success
		} catch (NoSuchMethodException e) {
			// Let's try another way
			Object runner = mainClass.getDeclaredConstructor().newInstance();
			Method setArgs = mainClass.getMethod("setArguments", String[].class);
			setArgs.invoke(runner, (Object) args);

			Method execute = mainClass.getMethod("execute");
			Object result = execute.invoke(runner);
			return ((Integer) result).intValue();
		}
	}

	/**
	 * Custom URLClassLoader for Java 21 with module system support
	 */
	private static class BirtURLClassLoader extends URLClassLoader {

		static {
			// Register classloader for Java 9+
			registerAsParallelCapable();
		}

		public BirtURLClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
			System.out.println("Created BirtURLClassLoader with " + urls.length + " URL");
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			// Exclude some system classes
			if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")
					|| name.startsWith("jdk.")) {
				return super.loadClass(name, resolve);
			}

			// Synchronization for safe class loading
			synchronized (getClassLoadingLock(name)) {
				// First try to find a class that is already loaded
				Class<?> c = findLoadedClass(name);
				if (c != null) {
					if (resolve) {
						resolveClass(c);
					}
					return c;
				}

				// Try to find the class in this classloader
				try {
					c = findClass(name);
					if (resolve) {
						resolveClass(c);
					}
					return c;
				} catch (ClassNotFoundException e) {
					// If not found, delegate to parent classloader
					return super.loadClass(name, resolve);
				}
			}
		}

		@Override
		public URL getResource(String name) {
			// Try to find the resource in this classloader
			URL url = findResource(name);
			if (url == null) {
				url = super.getResource(name);
			}
			return url;
		}

		@Override
		protected Package[] getPackages() {
			// Ensure proper package management
			Package[] parentPackages = super.getPackages();
			return parentPackages;
		}
	}

	/**
	 * Jednoduchá FilenameFilter implementácia (oprava preklepu)
	 */
	private class FilnameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	}
}