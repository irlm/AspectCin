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

public aspect EmailMonitor extends AbstractMonitor {
        
    public pointcut monitorPoint(Object message) :
        within(javax.mail.Transport+) && execution(* javax.mail.Transport.send*(..)) && args(message, ..);
    
    public Serializable getKey(Object message) {
        return "mail://";
    }
    
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
}
