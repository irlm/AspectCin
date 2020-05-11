/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import org.jmock.core.Invocation;
import org.jmock.core.stub.CustomStub;


public class DelayStub extends CustomStub {
    private Runnable runnable;
    private Object retVal;
    
    public DelayStub(long delay) {
        this(delay, null);
    }
    
    public DelayStub(long delay, Object retVal) {
        super("delay");
        this.runnable = new DelayingRunnable(delay);
        this.retVal = retVal;
    }

    public Object invoke( Invocation invocation ) throws Throwable {
         runnable.run();
    	 return retVal;
     }
}