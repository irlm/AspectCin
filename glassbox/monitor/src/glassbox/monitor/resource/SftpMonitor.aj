/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

public aspect SftpMonitor extends AbstractFtpMonitor {
    public pointcut fileTransfer(String fileName) :
        within(com.sshtools.j2ssh.SftpClient+) && 
        (execution(* get(..)) || execution(* put(..))) && args(fileName, ..);
    
    public pointcut openConnection(String host, String port) :
        within(com.sshtools.j2ssh.SshClient+) && execution(* connect(..)) && args(host, port, ..);
    
    before(Object host, String port) : openConnection(host, port) {
        begin(getConnectionKey(host, port));
    }        
    
    protected pointcut monitorEnd() : openConnection(*, *);
        
    protected String getConnectionProtocol() {
        return "ssh:///";
    }
    
    protected String getFileTransferProtocol() {
        return "sftp:///";
    }
}
