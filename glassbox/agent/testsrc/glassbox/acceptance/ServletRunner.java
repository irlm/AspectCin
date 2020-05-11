/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.acceptance;

import java.io.IOException;

import javax.servlet.ServletException;

import glassbox.test.MockServlet;


public class ServletRunner implements Runnable {

    private MockServlet servlet;
    private int iterations;

    public ServletRunner(MockServlet servlet, int iterations) {
        this.servlet = servlet;
        this.iterations = iterations;
    }
    
    public void run() {
        for (int i=0; i<iterations; i++) {
            try {
                servlet.forceDoGet();
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
