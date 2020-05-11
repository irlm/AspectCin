/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.web.controller;

import glassbox.client.helper.DisplayHelper;
import glassbox.client.helper.OperationHelper;
import glassbox.client.pojo.OperationAnalysisData;
import glassbox.client.pojo.OperationData;
import glassbox.client.remote.AgentConnectionException;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.servlet.ModelAndView;

public class OperationDetailController extends BaseController {
    
    private static final Log log = LogFactory.getLog(OperationDetailController.class);
    
    public ModelAndView handleClientRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("handling the client controller...");	
        HashMap velocityMap = new HashMap();
        String operationId = request.getParameter("operationId");
        if(!isEmpty(operationId)) {               
            OperationHelper helper = (OperationHelper)getApplicationContext(request).getBean("operationHelper");
            OperationData data = helper.getOperation(operationId);
            // I ran into a null value for data... presumably it can be null if the request doesn't return in time...
            if (data != null) {
                data.setViewed("");
                try {
                    OperationAnalysisData analysis = helper.getOperationAnalysis(data);
                    DisplayHelper display = analysis.getDisplayHelper(data);            
                    velocityMap.put("display", display);
                } catch (AgentConnectionException exc) {
                    log.error("Can't get data from agent", exc);
                    //no recovery - just rethrow
                    throw exc;//return new ModelAndView("error", 
                }
            } else {
                return reset();
            }
            return new ModelAndView("details", velocityMap);
        }                
        return reset();
    }
  
    protected ModelAndView reset() {
        return new ModelAndView("resetDetails", null);
    }
    
}
