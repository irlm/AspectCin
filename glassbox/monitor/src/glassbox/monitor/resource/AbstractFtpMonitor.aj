/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;


import glassbox.monitor.AbstractMonitor;
import glassbox.response.Response;

import java.io.Serializable;

public abstract aspect AbstractFtpMonitor extends AbstractMonitor {

    public abstract pointcut fileTransfer(String fileName);
    
    protected pointcut monitorPoint(Object fileName) :
        fileTransfer(fileName);
    
    protected Serializable getKey(Object fileName) {
        return getFileTransferProtocol()+fileName;
    }
    
    protected String getConnectionKey(Object host, String port) {
        return getConnectionProtocol()+host+":"+port;
    }               
    
    protected abstract String getConnectionProtocol();
    protected abstract String getFileTransferProtocol();    

    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
}
