/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import org.jmock.core.*;

public class VirtualStaticMock extends AbstractDynamicMock {
    private VirtualMockAspect theAspect;
    
    public VirtualStaticMock(VirtualMockAspect theAspect) {
        this(theAspect, new LIFOInvocationDispatcher());
    }
    
    public VirtualStaticMock(VirtualMockAspect theAspect, InvocationDispatcher invocationDispatcher) {
        super(null, "mock "+theAspect.getClass().getName(), invocationDispatcher);
        this.theAspect = theAspect;
        theAspect.setDelegate(this);
    }
    
    public Object proxy() {
        throw new RuntimeException("operation not supported");// XXX bad
    }
    
    Object doInvocation(Invocation invocation) throws Throwable {
        return mockInvocation(invocation);
    }
}
