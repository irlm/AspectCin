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

public aspect JaxmMonitor extends AbstractMonitor {
    /** Reflective call to invoke Web service through JAXM - a low level API but used by the PetStore */ 
    public pointcut jaxmCall(Object soapConnection, Object msg, Object endPoint) : 
        within(javax.xml.soap.SOAPConnection+) && execution(public * javax.xml.soap.SOAPConnection.call*(..)) && target(soapConnection) && args(msg, endPoint);
        
    protected pointcut monitorPoint(Object endPoint) : jaxmCall(*, *, endPoint);
    
    protected Serializable getKey(Object endPoint) {
        return endPoint.toString();
    }
    
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
        
}
