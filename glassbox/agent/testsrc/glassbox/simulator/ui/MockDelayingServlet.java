/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.test.DelayingRunnable;
import glassbox.test.MockServlet;

public class MockDelayingServlet extends MockServlet {
    
    public MockDelayingServlet() {
        runnable = new DelayingRunnable();
    }
    
    public void setDelay(long delay) {
        ((DelayingRunnable)getRunnable()).setDelay(delay);
    }
    
    public long getDelay() {
        return shouldDelay() ? ((DelayingRunnable)getRunnable()).getDelay() : 0L;
    }
    
    private boolean shouldDelay() {
    	return Math.random() < 0.75;  // only delay 75% of the time-- to improve our test coverage
    }
    
    private static final long serialVersionUID = 1; 
}
