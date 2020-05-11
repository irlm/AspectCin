/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.web.controller;

import glassbox.client.helper.ConnectionHelper;
import glassbox.client.web.session.SessionData;
import glassbox.installer.GlassboxInstallerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public abstract class BaseController implements Controller {

	private static final Log log = LogFactory.getLog(BaseController.class);
    public static final String EMPTY_STRING = "";
    public static final boolean forceTestMode = Boolean.getBoolean("glassbox.force.no.install");
    
    String installURI = "Install.form";
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //Set the session data, by getting it.
        getSessionData(request, response);
        if(!forceTestMode && !GlassboxInstallerFactory.getInstance().getInstaller().isInstalled()) {
        	ModelAndView mav = new ModelAndView(new RedirectView(installURI, true));
//            try {
//                GlassboxInstaller installer = GlassboxInstallerFactory.getInstance().getInstaller(context);
//                GlassboxInstallerData installData = installer.getInstallerData();
//                log.debug("Adding the installer object to the attribute request for platform: " + installData.getTargetSystem());
//                request.setAttribute("installerData", installData);
//                if(new InstallValidationHelper().detectPartialInstall(installData, context)) {
//                	request.setAttribute("PARTIAL_INSTALL", "true");
//                }
//
//                if(installData.getTargetSystem().equals(UnknownInstallerData.GENERIC_SYSTEM)) {
//                	mav = new ModelAndView("generic_install");
//                } else if(installData.isExe()) {
//                	mav = new ModelAndView("exe_install");
//                } else if(installData instanceof JBossInstallerData && isJBoss400(installData, context)) {
//                	mav = new ModelAndView("jboss400_install");
//                }
//            } catch (Exception e) {
//                log.warn("Error Installing From InitializeInstallAction", e);
//            }         
        	return mav;
        } else { 	
        	return handleClientRequest(request, response);
        }
    }
    
//    private boolean isJBoss400(GlassboxInstallerData data, ServletContext context) {
//    	boolean found = false;
//    	String jbossVersion = null;
//    	String baseHome = data.findBaseHome(context);
//    	String lib = baseHome + File.separator + "lib";
//    	File libDir = new File(lib);
//    	String[] libDirFiles = libDir.list();
//    	for(int i=0; i<libDirFiles.length; i++) {
//    		File libDirFile = new File(lib + File.separator + libDirFiles[i]);
//    		if(libDirFile.getName().indexOf("jboss") >= 0) {
//    			JarInputStream jis = null;
//    			try {
//					jis = new JarInputStream(new FileInputStream(libDirFile));
//					jbossVersion = (String) jis.getManifest().getMainAttributes().getValue("Specification-Version");
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					try {
//						jis.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				break;
//    		}
//    	}
//    	if(jbossVersion != null && jbossVersion.equals("4.0.0")) {
//    		found = true;
//    	}
//    	return found;
//    }

    public abstract ModelAndView handleClientRequest(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception;
 
    protected SessionData getSessionData(HttpServletRequest request, HttpServletResponse response) {
        SessionData session = (SessionData) request.getSession().getAttribute(SessionData.CLIENT_SESSION_KEY);
        if (session == null) {
            session = new SessionData();
            initializeSession(session, request, response);
            request.getSession().setAttribute(SessionData.CLIENT_SESSION_KEY, session);
        }
        return session;
    }
    
    protected void initializeSession(SessionData session, HttpServletRequest request, HttpServletResponse response) {
        ConnectionHelper connection = (ConnectionHelper)getApplicationContext(request).getBean("connectionHelper");
        connection.getConnections(request.getSession());
    }
        

    public static String trimNull(String str) {
        return (str == null) ? EMPTY_STRING : str;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
    
    protected ApplicationContext getApplicationContext(HttpServletRequest request) {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
        return applicationContext;
    }

	public void setInstallViewName(String installViewName) {
		this.installURI = installViewName;
	}
    
}
