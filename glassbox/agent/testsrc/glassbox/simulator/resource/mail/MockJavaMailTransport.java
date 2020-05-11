/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.mail;

import java.util.Properties;

import glassbox.test.DelayingRunnable;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

public class MockJavaMailTransport extends javax.mail.Transport  {

   protected DelayingRunnable delayer = new DelayingRunnable();
   protected boolean shouldFail = false;
   
    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }

    public void delay() {
        delayer.run();
    }
      
    public MockJavaMailTransport() {
        super(Session.getDefaultInstance(new Properties()), new URLName("mail:help@glassbox.com"));
    }
    
    public MockJavaMailTransport(Session arg0, URLName arg1) {
        super(arg0, arg1);
    }

    public void send() throws MessagingException {
        sendMessage(null, null);
    }
    
    public void sendMessage() throws MessagingException {
        sendMessage(null, null);
    }
    
    public void sendMessage(Message arg0, Address[] arg1) throws MessagingException {
        if(shouldFail) throw new MessagingException();
        delay();
    }

    public boolean isShouldFail() {
        return shouldFail;
    }

    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    } 
}
