/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.ftp;

import glassbox.test.DelayingRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.MessagingException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class MockJakartaFtpClient extends FTPClient {

    protected DelayingRunnable delayer = new DelayingRunnable();
    protected boolean shouldFail = false;
    
     public void setDelay(long delay) {
         delayer.setDelay(delay);
     }

     public void delay() {
         delayer.run();
     }
    
     public boolean isShouldFail() {
         return shouldFail;
     }

     public void setShouldFail(boolean shouldFail) {
         this.shouldFail = shouldFail;
     } 
    
     public boolean deleteFile() throws IOException {
         return deleteFile(null);
     }
     
    public boolean deleteFile(String arg0) throws IOException {
        if(shouldFail) throw new IOException();
        delay();
        return true;
    }

   
    public FTPFile[] listFiles() throws IOException {
        if(shouldFail) throw new IOException();
        delay();
        return null;
    }

    public boolean retrieveFile() throws IOException {
        return retrieveFile(null, null);
    }
    
    public boolean retrieveFile(String arg0, OutputStream arg1) throws IOException {
        if(shouldFail) throw new IOException();
        delay();
        return true;
    }

    public boolean storeFile() throws IOException {
        return storeFile(null, null);
    }
    
    public boolean storeFile(String arg0, InputStream arg1) throws IOException {
        if(shouldFail) throw new IOException();
        delay();
        return true;
    }
}
