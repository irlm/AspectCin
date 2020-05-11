/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test.ajmock;

import org.jmock.Mock;
import org.jmock.builder.*;
import org.jmock.core.*;

public class VirtualMock extends Mock {

    public VirtualMock(String pointcutExprStr) {
        this(pointcutExprStr, new LIFOInvocationDispatcher());
    }

    public VirtualMock(VirtualStaticMock sMock) {
        super(sMock);
    }

    public VirtualMock(String pointcutExprStr, InvocationDispatcher invocationDispatcher) {
        super(new VirtualDynamicMock(pointcutExprStr, invocationDispatcher));
    }

    // have to copy/paste this to override what's a private method in jmock
    public NameMatchBuilder expects( InvocationMatcher expectation ) {
        NameMatchBuilder builder = addNewInvocationMocker();
        builder.match(expectation);
        return builder;
    }

    protected NameMatchBuilder addNewInvocationMocker() {
        InvocationMocker mocker = new InvocationMocker(new InvocationMockerDescriber());
        addInvokable(mocker);

        return new JpInvocationMockerBuilder( mocker, this );
    }

}