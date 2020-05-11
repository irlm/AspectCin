/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Jul 4, 2005
 */
package glassbox.simulator.ui;


public class MockServletCpuHog extends MockDelayingServlet {
    public MockServletCpuHog() {
        runnable = new CpuHoggingRunnable(getDelay());
    }
    
    private static final long serialVersionUID = 1L;
}