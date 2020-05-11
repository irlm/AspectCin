/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.debug;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.resource.JdbcMonitor;

public aspect TraceAdvice {

    public static final boolean ENABLED = Boolean.getBoolean(TraceAdvice.class.getName()+".enabled");
    
    pointcut monitorExec(AbstractMonitor monitor) : adviceexecution() && within(AbstractMonitor+) &&!this(JdbcMonitor) && this(monitor) && if(ENABLED);
    
    before(AbstractMonitor monitor) : monitorExec(monitor) {
        System.err.println(monitor.getClass()+" entering "+thisJoinPointStaticPart.getSignature()+" at "+thisJoinPointStaticPart.getSourceLocation());
    }
    after(AbstractMonitor monitor) returning: monitorExec(monitor) {
        System.err.println(monitor.getClass()+" returning "+thisJoinPointStaticPart.getSignature()+" at "+thisJoinPointStaticPart.getSourceLocation());
    }
    after(AbstractMonitor monitor) throwing (Throwable t) : monitorExec(monitor) {
        System.err.println(monitor.getClass()+" throwing at "+thisJoinPointStaticPart.getSignature()+" at "+thisJoinPointStaticPart.getSourceLocation());
        t.printStackTrace();
    }
}
