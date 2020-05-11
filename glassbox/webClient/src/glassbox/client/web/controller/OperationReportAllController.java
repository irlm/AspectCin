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
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

public class OperationReportAllController extends BaseController {
    
    private static final Log log = LogFactory.getLog(OperationReportAllController.class);
    
    public ModelAndView handleClientRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HashMap velocityMap = new HashMap();
        OperationHelper helper = (OperationHelper)getApplicationContext(request).getBean("operationHelper");
        Collection operationList = helper.getOperations(request.getSession());
        List displayList = new ArrayList(50);
        
        if (operationList != null) {
            int count=1;
            for (Iterator it=operationList.iterator(); it.hasNext(); count++) {
                OperationData data = (OperationData)it.next();
                OperationAnalysisData analysis = helper.getOperationAnalysis(data);
                try {
                    DisplayHelper display = analysis.getDisplayHelper(data);            
                    displayList.add(display);
                } catch (AgentConnectionException exc) {
                    log.error("Can't get data from agent", exc);
                    //no recovery - just rethrow
                    throw exc;//return new ModelAndView("error", 
                }                
            }
            velocityMap.put("displayList", displayList);
            return new ModelAndView("reportAll", velocityMap);
        } else {
            return reset();
        }
    }
  
    protected ModelAndView reset() {
        return new ModelAndView("resetDetails", null);
    }
    
}
