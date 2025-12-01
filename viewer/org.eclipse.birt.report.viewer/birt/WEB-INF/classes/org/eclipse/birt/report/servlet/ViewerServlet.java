/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 *     Refactored by ChatGPT - Removed SOAP dependency.
 ************************************************************************************/

package org.eclipse.birt.report.servlet;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.context.BirtContext;
import org.eclipse.birt.report.context.IContext;
import org.eclipse.birt.report.presentation.aggregation.IFragment;
import org.eclipse.birt.report.presentation.aggregation.layout.FramesetFragment;
import org.eclipse.birt.report.presentation.aggregation.layout.RunFragment;
import org.eclipse.birt.report.service.BirtReportServiceFactory;
import org.eclipse.birt.report.service.BirtViewerReportService;
import org.eclipse.birt.report.utility.BirtUtility;

/**
 * Servlet implementation of BIRT Web Viewer (SOAP-free version).
 */
public class ViewerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private IFragment viewer;
	private IFragment run;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			BirtReportServiceFactory.init(new BirtViewerReportService(config.getServletContext()));

			// initialize fragments
			viewer = new FramesetFragment();
			viewer.buildComposite();
			viewer.setJSPRootPath("/webcontent/birt"); //$NON-NLS-1$

			run = new RunFragment();
			run.buildComposite();
			run.setJSPRootPath("/webcontent/birt"); //$NON-NLS-1$

		} catch (Exception e) {
			throw new ServletException("Error initializing BIRT ViewerServlet", e);
		}
	}

	private IContext createContext(HttpServletRequest request, HttpServletResponse response) throws BirtException {
		BirtReportServiceFactory.getReportService().setContext(getServletContext(), null);
		return new BirtContext(request, response);
	}

	/**
	 * Local process http request with GET method.
	 *
	 * @param request  incoming http request
	 * @param response http response
	 * @exception ServletException
	 * @exception IOException
	 * @return
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			IContext context = createContext(request, response);
			String servletPath = request.getServletPath();

			IFragment activeFragment = null;
			if (IBirtConstants.SERVLET_PATH_FRAMESET.equalsIgnoreCase(servletPath)) {
				activeFragment = viewer;
			} else if (IBirtConstants.SERVLET_PATH_RUN.equalsIgnoreCase(servletPath)) {
				activeFragment = run;
			}

			if (activeFragment != null) {
				activeFragment.service(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown servlet path: " + servletPath);
			}
		} catch (BirtException e) {
			handleException(request, response, e);
		}
	}

	/**
	 * Locale process http request with POST method. Four different servlet paths
	 * are expected: "/frameset", "/navigation", "/toolbar", and "/run".
	 *
	 * @param request  incoming http request
	 * @param response http response
	 * @exception ServletException
	 * @exception IOException
	 * @return
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			IContext context = createContext(request, response);
			doGet(request, response); // Same behavior as GET
		} catch (BirtException e) {
			handleException(request, response, e);
		}
	}

	private void handleException(HttpServletRequest request, HttpServletResponse response, Exception e)
			throws IOException {
		e.printStackTrace();
		BirtUtility.appendErrorMessage(response.getOutputStream(), e);
	}
}
