/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.web.controller;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

public class ClientController extends BaseController {
    
    private static final Log log = LogFactory.getLog(ClientController.class);
    
    public ModelAndView handleClientRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("handling the client controller...");   
	
        return new ModelAndView("client");
    }
  
    
}
