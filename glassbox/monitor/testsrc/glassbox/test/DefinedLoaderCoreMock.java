/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import java.lang.reflect.*;

import org.jmock.core.*;

public class DefinedLoaderCoreMock extends AbstractDynamicMock implements InvocationHandler {
    protected Object proxy;
    
    public DefinedLoaderCoreMock(Class mockedType, String name, InvocationDispatcher invocationDispatcher, ClassLoader loader) {
        super(mockedType, name, invocationDispatcher);
        
        this.proxy = Proxy.newProxyInstance(loader,
                new Class[]{mockedType},
                this);
    }

    public DefinedLoaderCoreMock(Class mockedType, String name, ClassLoader loader) {
        this(mockedType, name, new LIFOInvocationDispatcher(), loader);
    }

    public Object proxy() {
        return this.proxy;
    }

    public Object invoke( Object invokedProxy, Method method, Object[] args )
        throws Throwable
    {
        Invocation invocation = new Invocation(invokedProxy, method, args);
        return mockInvocation(invocation);
    }
}