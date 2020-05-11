/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import glassbox.util.timing.api.TimeConversion;

public class DelayingRunnable implements Runnable {
    /** delay in nanoseconds */
    private long delay;
    
    public DelayingRunnable() {
        delay = 0L;
    }
    
    /** @param delay in nanoseconds */
    public DelayingRunnable(long delay) {
        this.delay = delay;
    }
    
    /** @param delay in nanoseconds */
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    public long getDelay() {
        return delay;
    }
    
    /**
     * Sleep for specified time.
     * 
     * @param delayTime in nanoseconds.
     */
    public static void sleep(long delayTime) throws InterruptedException {
        Thread.sleep(TimeConversion.convertNanosToMillis(delayTime));        
    }
    
    public void run() {
        try {
            sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }            
    }    
}