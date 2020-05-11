/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Dec 20, 2005
 */
package glassbox.simulator.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockServletHangForever extends MockServletCpuHog {
    public MockServletHangForever() {
        runnable = new Runnable() {   
            public void run() {
                try {
                    //sleep for a day.
                    Thread.sleep(1000*60*60*24);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
    }

    // the implementation below gets expanded down in a way that hides the delay
    // it'd be good to restore this...
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        super.doPost(request, response); 
//        
//        /* then sleep */
//        try {
//            //sleep for a day.
//            Thread.sleep(1000*60*60*24);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    } 
    private static final long serialVersionUID = 1L;
}
