/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import java.util.*;

import org.jmock.core.Invocation;

// to make these percflow, we'd have to weave concrete implementations into the library... so we do something equivalent 
public abstract aspect VirtualMockAspect {
    private static List mocks = new LinkedList();
    protected static boolean enabled = false;
    
    private VirtualStaticMock delegate;
    
    public VirtualMockAspect() {
        mocks.add(this);
    }
    
    public abstract pointcut mockPoint();

    public void setDelegate(VirtualStaticMock delegate) {
        this.delegate = delegate;
    }
    
    protected void resetState() {
        delegate = null;
    }
    
    Object around() : mockPoint() && if(enabled) {
        if (delegate == null) {
            return proceed();
        }
        
        Invocation jpInvocation = new JpInvocation(thisJoinPoint) {
            public Object doProceed() {
                return proceed();
            }
        };
        try {
            return delegate.doInvocation(jpInvocation);
        } catch (final Throwable t) {
            Thrower.throwException(t);
            return null;
        }
    }    
    
    // somewhat inefficient: create one aspect instance per mock for ALL test runs.... would be better to limit by declaring scope
    private static aspect CflowTracking {        
        before() : VirtualMockObjectTestCase.topLevelRun() {
            for (Iterator it=mocks.iterator(); it.hasNext();) {
                VirtualMockAspect virtualMockAspect = (VirtualMockAspect)it.next();
                virtualMockAspect.resetState();
            }
            enabled = true;
        }
        
        after() : VirtualMockObjectTestCase.topLevelRun() {
            enabled = false;
        }
    }
    
    
}
