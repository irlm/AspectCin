/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.remote;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import glassbox.test.DelayingRunnable;

public class MockJaxmCall extends javax.xml.soap.SOAPConnection {

    protected DelayingRunnable delayer = new DelayingRunnable();
    
    public MockJaxmCall() {}
     
    public SOAPMessage call(SOAPMessage message, Object b) throws SOAPException {
        if (shouldFail) {
            throw new SOAPException("can't connect");
        }
        delay();
        return null;
    }
    
    public void close() {
    }
    
    public boolean shouldFail = false;

    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }

    public void delay() {
        delayer.run();
    }
    
}
    