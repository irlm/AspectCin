/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import java.io.File;

public aspect JakartaFtpMonitor extends AbstractFtpMonitor {
    
    public pointcut fileTransfer(String fileName) :
        within(org.apache.commons.net.ftp.FTPClient+) &&
        (execution(* store*File*(..)) ||
        execution(* org.apache.append*File*(..)) ||
        execution(* org.apache.retrieve*File*(..))) &&
        args(fileName, ..);
    
    public pointcut openConnection(Object host, int port) :
        within(org.apache.commons.net.SocketClient+) && execution(* connect(..)) && args(host, port, ..) &&
        within(org.apache.commons.net.ftp..*);
    
    before(Object host, int port) : openConnection(host, port) {
        begin(getConnectionKey(host, Integer.toString(port)));
    }        
    
    protected pointcut monitorEnd() : openConnection(*, *);
    
    protected String getConnectionProtocol() {
        return "ftp:///";
    }
    
    protected String getFileTransferProtocol() {
        return "ftp:///";
    }
    
}
