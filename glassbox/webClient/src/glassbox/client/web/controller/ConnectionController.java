/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

public class ConnectionController extends BaseController {
    private static final Log log = LogFactory.getLog(ConnectionController.class);
    
    public ModelAndView handleClientRequest(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
        log.debug("handling the menu bar controller...");           
        return new ModelAndView("connection_panel");
    }
    
}
