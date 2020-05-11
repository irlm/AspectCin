/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.simulator.resource.remote.MockJaxmCall;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;

public class MockServletFailingRemoteCall extends MockDelayingServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MockJaxmCall jaxmCall = new MockJaxmCall();
        jaxmCall.shouldFail = true;
        try {
            jaxmCall.setDelay(getDelay()); // nanosec, so 1sec delay here, ignore standard delay settings.
            jaxmCall.call(null, "http://unreliable.org/GetMeIfYouCant");
            throw new ServletException("bad service");
        } catch (SOAPException e) {
            // expected
        }
    }
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    
    private static final long serialVersionUID = 1;
}
