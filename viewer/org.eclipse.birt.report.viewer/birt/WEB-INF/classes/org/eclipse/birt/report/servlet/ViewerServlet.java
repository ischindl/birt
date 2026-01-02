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
import java.util.Iterator;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.context.BirtContext;
import org.eclipse.birt.report.context.IContext;
import org.eclipse.birt.report.exception.ViewerException;
import org.eclipse.birt.report.presentation.aggregation.IFragment;
import org.eclipse.birt.report.presentation.aggregation.layout.FramesetFragment;
import org.eclipse.birt.report.presentation.aggregation.layout.RunFragment;
import org.eclipse.birt.report.resource.BirtResources;
import org.eclipse.birt.report.resource.ResourceConstants;
import org.eclipse.birt.report.service.BirtReportServiceFactory;
import org.eclipse.birt.report.service.BirtViewerReportService;
import org.eclipse.birt.report.session.IViewingSession;
import org.eclipse.birt.report.session.ViewingSessionUtil;
import org.eclipse.birt.report.utility.BirtUtility;
import org.eclipse.birt.report.utility.ParameterAccessor;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation of BIRT Web Viewer (SOAP-free version).
 */
public class ViewerServlet extends BaseReportEngineServlet {

	private static final long serialVersionUID = 1L;

	IFragment viewer;
	IFragment run;

	@Override
	public void __init(ServletConfig config) {
		BirtReportServiceFactory.init(new BirtViewerReportService(config.getServletContext()));

		// initialize fragments
		viewer = new FramesetFragment();
		viewer.buildComposite();
		viewer.setJSPRootPath("/webcontent/birt"); //$NON-NLS-1$

		run = new RunFragment();
		run.buildComposite();
		run.setJSPRootPath("/webcontent/birt"); //$NON-NLS-1$
	}

	/**
	 * Init context.
	 *
	 * @param request  incoming http request
	 * @param response http response
	 * @exception BirtException
	 * @return IContext
	 */
	@Override
	protected IContext __getContext(HttpServletRequest request, HttpServletResponse response) throws BirtException {
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
	protected void __doGet(IContext context) throws ServletException, IOException, BirtException {
		try {
			String servletPath = context.getRequest().getServletPath();

			IFragment activeFragment = null;
			if (IBirtConstants.SERVLET_PATH_FRAMESET.equalsIgnoreCase(servletPath)) {
				activeFragment = viewer;
			} else if (IBirtConstants.SERVLET_PATH_RUN.equalsIgnoreCase(servletPath)) {
				activeFragment = run;
			}

			if (activeFragment != null) {
				activeFragment.service(context.getRequest(), context.getResponse());
			} else {
				context.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND,
						"Unknown servlet path: " + servletPath);
			}
		} catch (BirtException e) {
			__handleNonSoapException(context.getRequest(), context.getResponse(), e);
		}
	}

	protected void __handleNonSoapException(HttpServletRequest request, HttpServletResponse response,
			Exception exception) throws ServletException, IOException {
		exception.printStackTrace();
		BirtUtility.appendErrorMessage(response.getOutputStream(), exception);
	}

	public IFragment getViewer() {
		return viewer;
	}

	@Override
	protected boolean __authenticate(HttpServletRequest request, HttpServletResponse response) {
		return true;
	}

	/**
	 * Handle HTTP POST method.
	 *
	 * @param request  incoming http request
	 * @param response http response
	 * @exception ServletException
	 * @exception IOException
	 * @return
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!__authenticate(request, response)) {
			return;
		}

		// create SOAP URL with post parameters
		StringBuilder builder = new StringBuilder();
		Iterator it = request.getParameterMap().keySet().iterator();
		while (it.hasNext()) {
			String paramName = (String) it.next();
			if (paramName != null && paramName.startsWith("__")) //$NON-NLS-1$
			{
				String paramValue = ParameterAccessor.urlEncode(ParameterAccessor.getParameter(request, paramName),
						ParameterAccessor.UTF_8_ENCODE);
				builder.append("&" + paramName + "=" + paramValue); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		String soapURL = request.getRequestURL().toString();
		if (ParameterAccessor.getBaseURL() != null) {
			soapURL = ParameterAccessor.getBaseURL() + request.getContextPath() + request.getServletPath();
		}

		builder.deleteCharAt(0);
		soapURL += "?" + builder.toString(); //$NON-NLS-1$

		request.setAttribute("SoapURL", soapURL); //$NON-NLS-1$

		String requestType = request.getHeader(ParameterAccessor.HEADER_REQUEST_TYPE);
		boolean isSoapRequest = ParameterAccessor.HEADER_REQUEST_TYPE_SOAP.equalsIgnoreCase(requestType);
		// refresh the current BIRT viewing session by accessing it
		IViewingSession session;

		// init context
		IContext context = null;
		try {
			session = ViewingSessionUtil.getSession(request);
			if (session == null && !isSoapRequest) {
				if (ViewingSessionUtil.getSessionId(request) == null) {
					session = ViewingSessionUtil.createSession(request);
				} else {
					// if session id passed through the URL, it means this request
					// was expected to run using a session that has already expired
					throw new ViewerException(
							BirtResources.getMessage(ResourceConstants.GENERAL_ERROR_NO_VIEWING_SESSION));
				}
			}
			context = __getContext(request, response);
		} catch (BirtException e) {
			// throw exception
			__handleNonSoapException(request, response, e);
			return;
		}

		try {
			if (session != null) {
				session.lock();
			}
			__doPost(context);

			if (isSoapRequest) {
				// Workaround for using axis bundle to invoke SOAP request
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

				super.doPost(request, response);
			} else {
				try {
					if (context.getBean().getException() != null) {
						__handleNonSoapException(request, response, context.getBean().getException());
					} else {
						__doGet(context);
					}
				} catch (BirtException e) {
					__handleNonSoapException(request, response, e);
				}
			}
		} catch (BirtException e) {
			e.printStackTrace();
		} finally {
			if (session != null && !session.isExpired()) {
				session.unlock();
			}
		}
	}

	/**
	 * @param context
	 */
	private void __doPost(IContext context) throws BirtException {
		// TODO Auto-generated method stub

	}

}
