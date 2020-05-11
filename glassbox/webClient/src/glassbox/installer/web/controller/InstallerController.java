/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.web.controller;

import glassbox.installer.GlassboxInstaller;
import glassbox.installer.GlassboxInstallerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class InstallerController implements Controller, ServletContextAware {

	private static final Log log = LogFactory.getLog(InstallerController.class);
	private ServletContext context;
    private GlassboxInstallerFactory factory = GlassboxInstallerFactory.getInstance();
    
    static final String MISSING_START_SCRIPT = "Default start script not found.  Please specify location of start script";

    public ModelAndView handleRequest(
			HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (request.getMethod().equalsIgnoreCase("GET")) 
			return handleGET(request, response);
		else if (request.getMethod().equalsIgnoreCase("POST"))
			return handlePOST(request, response);
		else 
			return handleGET(request, response); //TODO where is a generic error page?
	}
    
    public ModelAndView handleGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
		if (true) outputDebugging(request);

		return new ModelAndView("configureInstaller");
		
    }
    
    public ModelAndView handlePOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    	GlassboxInstaller installer = factory.getInstaller();
		
		// reset installer
		installer.setCustomScriptToWrap(null);
		installer.setCustomLibDirectory(null);

		String customScript = request.getParameter("customScript");
		if (customScript == null) 
			customScript = "";
		else
			log.info("Custom Launch Script: "+customScript);

		String customLibDir = request.getParameter("customLibDir");
		if (customLibDir == null) customLibDir = "";
		else
			log.info("Custom Lib Dir: "+customLibDir);
		

		try {
    		if (!customScript.equals("")) {
    			installer.setCustomScriptToWrap(new File(customScript));
    		}
    		if (!customLibDir.equals("")) {
    			installer.setCustomLibDirectory(new File(customLibDir));
    		}

    		// opportunity for custom post parameters to be used (see Tomcat installer)
    		installer.customParameters(request.getParameterMap());
    		
    		log.info("Launcher Glassbox Installer("+installer+")...");
    		installer.install();

//    		return new ModelAndView("configureInstaller"); // return the read-view
    		return new ModelAndView("installerResults");    		
    		
    	} catch (RuntimeException ex) {
    		log.error("Error installing", ex);
    		
    		// reset installer
    		installer.reset();
    		
    		// help populate form fields for user
    		request.setAttribute("error", ex.getMessage());
    		request.setAttribute("customScript", customScript);
    		request.setAttribute("customLibDir", customLibDir);
    		
    		// redraw the GET resource
    		return new ModelAndView("configureInstaller"); // return the read-view
    	}
    }
	
    private void outputDebugging(HttpServletRequest request) {
		System.out.println("=============== Environment info =============");
		System.out.println("");		
		System.out.println("=============== System Properties");
		Properties properties = System.getProperties();
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			
			System.out.println(name+"="+ properties.getProperty(name));
		}
		System.out.println("");		
		System.out.println("=============== Servlet Properties");
		System.out.println("getServerInfo()="+context.getServerInfo());
//		System.out.println("getServerInfo()="+context.getServerIfo());
//		System.out.println("getContextPath()="+context.getContextPath());
		System.out.println("getRealPath(\"/\")="+context.getRealPath("/"));
		System.out.println("getRealPath(\"/glassbox\")="+context.getRealPath("/glassbox"));
		try {
			System.out.println("getResource(\"/\")="+context.getResource("/").toExternalForm());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("");
		System.out.println("ClassLoaders:");
		ClassLoader cl = this.getClass().getClassLoader();
		while(cl != null) {
			System.out.println(cl.getClass().getName());
			cl = cl.getParent();
		}
		System.out.println("==============================================");
	}

	public void setServletContext(ServletContext context) {
		this.context = context;
	}
    
    // This method is for testing only
    public void setGlassboxInstallerFactory(GlassboxInstallerFactory factory) {
    	this.factory = factory;
    }
}
