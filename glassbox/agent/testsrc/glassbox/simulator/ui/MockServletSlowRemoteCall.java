/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.test.DelayingRunnable;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockServletSlowRemoteCall extends MockDelayingServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            new RemoteService(runnable).getBalance("123-45-6789", "note: don't use SSN in future");
        } catch (Exception e) {
            throw new ServletException("bad service", e);
        }
    }
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private static final long serialVersionUID = 1;
}

class RemoteService implements Remote {
    private Runnable runnable;
    
    public RemoteService(Runnable delayer) {
        this.runnable = delayer;
    }
    
    public double getBalance(String id, String qualifier) throws RemoteException {
        runnable.run();
        
        return 0.;
    }
}


