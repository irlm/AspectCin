/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.web.controller;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import glassbox.agent.control.api.GlassboxService;
import glassbox.client.remote.AgentConnectionException;
import glassbox.client.remote.LocalAgentClient;
import glassbox.config.GlassboxInitializer;
import glassbox.installer.web.helper.InstallValidationHelper;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class VerifyController implements Controller {
    private static final Log log = LogFactory.getLog(VerifyController.class);
    private InstallValidationHelper validationHelper;
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
    	throws ServletException, IOException {
        ModelAndView mav = null;
        try {
        	log.debug("Testing the connectivity....");
        	InstallValidationHelper helper = new InstallValidationHelper();
        	String status = helper.getErrorStatus();
        	int statusCode = Integer.parseInt(status.substring(0, 1));
        	
        	if (statusCode == 0) {
        	    String connectionStatus = helper.testConnection(null);
        	    if(connectionStatus != null && !connectionStatus.equals("SUCCESS")) {
                    statusCode = 2;
                    status = "2" + connectionStatus;
                    log.error("Verify Controller install status: "+status);
        	    }
        	}        	    
            if (statusCode != 0) {
                log.error("Verify Controller install status: "+status);
            }
	        switch(statusCode) {
	        case 0:
	        	mav = new ModelAndView("install_success");
	        	break;
	        case 1:
		        request.setAttribute("ERROR", status.substring(1));
		        request.setAttribute("PARAM", "-D"+GlassboxInitializer.CONFIG_DIR_PROPERTY);
	        	mav = new ModelAndView("startup_param_missing");
	        	break;
	        case 2:
		        request.setAttribute("ERROR", status.substring(status.indexOf(InstallValidationHelper.PORT_DELIM) + 1));
	        	request.setAttribute("PORT", status.substring(1, status.indexOf(InstallValidationHelper.PORT_DELIM)));
	        	mav = new ModelAndView("connection_failed");
	        	break;
	        case 3:
		        request.setAttribute("ERROR", status.substring(1));
	        	mav = new ModelAndView("javaagent_missing");
	        	break;
	        case 4:
		        request.setAttribute("ERROR", status.substring(1));
	        	mav = new ModelAndView("bootclasspath_entries_missing");
	        	break;
	        case 5:
		        request.setAttribute("ERROR", status.substring(1));
	        	mav = new ModelAndView("agent_missing");
	        	break;
	        case 6:
		        request.setAttribute("ERROR", status.substring(1));
	        	mav = new ModelAndView("monitor_missing");
	        	break;
	        case 7:
	        	request.setAttribute("ERROR", status.substring(1));
		        request.setAttribute("PARAM", "-Daspectwerkz.classloader.preprocessor");
	        	mav = new ModelAndView("startup_param_missing");
	        	break;
	        default:
	        	mav = new ModelAndView("unknown_error");
	        }
        } catch (Exception e) {
            log.error("Error in Verifying Installation", e);
        }   
        
        return mav;
    }

}
