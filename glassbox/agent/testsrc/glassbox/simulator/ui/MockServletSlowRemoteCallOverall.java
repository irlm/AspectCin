/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;
import glassbox.simulator.resource.remote.MockJaxmCall;

public class MockServletSlowRemoteCallOverall extends MockDelayingServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        MockJaxmCall call1 = new MockJaxmCall();
        call1.setDelay(getDelay()/3); 
        try {
            call1.call(null, "http://services.tester.org/SlowRemoteCall");
        } catch (Exception e) {
            throw new ServletException("bad service", e);
        }
        
        MockJaxmCall call2 = new MockJaxmCall();
        call2.setDelay(getDelay()/3);
        try {
            call2.call(null, "http://services.tester.org/YetAnotherCall");
        } catch (Exception e) {
            throw new ServletException("bad service", e);
        }
        
        MockJaxmCall call3 = new MockJaxmCall();
        call3.setDelay(getDelay()/2);
        try {
            call3.call(null, "http://creditco.org/CheckCredit");
        } catch (Exception e) {
            throw new ServletException("bad service", e);
        }
        
        
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private static final long serialVersionUID = 1;
}
