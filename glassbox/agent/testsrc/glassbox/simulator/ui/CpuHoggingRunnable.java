/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.test.DelayingRunnable;
import glassbox.util.timing.ClockManager;

public class CpuHoggingRunnable extends DelayingRunnable {

    public CpuHoggingRunnable() {
        super();
    }
    
    public CpuHoggingRunnable(long delay) {
        super(delay);
    }
    
    public void run() {
        /* Just gobble some CPU time */
        long endTime = getDelay() + ClockManager.getTime();
        double x;
        while ((x = ClockManager.getTime()) < (double)endTime) { 
            x += Math.sin(Math.random()*100/Math.PI);
        }
    } 

}
