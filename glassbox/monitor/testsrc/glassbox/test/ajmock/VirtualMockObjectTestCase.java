/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.jmock.core.stub.CustomStub;

public abstract class VirtualMockObjectTestCase extends MockObjectTestCase {
    protected Stub doProceed() {
        return new CustomStub("proceedStub") {
            public Object invoke( Invocation invocation ) throws Throwable {
                return ((JpInvocation)invocation).doProceed();
            }
        };
    }
    
    protected VirtualMock dynamicVirtualMock(String pointcutExpr) {
        VirtualMock vMock = new VirtualMock(pointcutExpr);
        registerToVerify(vMock);
        return vMock;
    }

    protected VirtualMock virtualMock(VirtualMockAspect azpect) {
        VirtualMock vMock = new VirtualMock(new VirtualStaticMock(azpect));
        registerToVerify(vMock);
        return vMock;
    }

    // explicitly override so we can weave cflow tracking in...
    public void runBare() throws Throwable {
        super.runBare();
    }
    
    public pointcut virtualMockRun() :
        execution(void runBare()) && within(VirtualMockObjectTestCase+);
    
    public pointcut topLevelRun() : !cflowbelow(virtualMockRun()) && virtualMockRun();
    
}
