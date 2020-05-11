/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import junit.framework.Assert;

import org.jmock.core.matcher.InvokedRecorder;

/**
 * 
 * Extends jMock to test for matching at least n times.
 * 
 * @author Ron Bodkin
 *
 */
public class InvokeAtLeastCountMatcher extends InvokedRecorder {
    private int minExpectedCount;

    public InvokeAtLeastCountMatcher(int minExpectedCount) {
        this.minExpectedCount = minExpectedCount;
    }

    public void verifyHasBeenInvokedAtLeast( int minExpectedCount ) {
        Assert.assertTrue("expected method was not invoked the expected number of times: expected at least " +
                minExpectedCount + " times, was invoked " + getInvocationCount() + " times",
                          getInvocationCount() >= minExpectedCount );
    }
    
    public void verify() {
        verifyHasBeenInvokedAtLeast(minExpectedCount);
    }

    public boolean hasDescription() {
        return true;
    }

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("expected at least ").append(minExpectedCount).append(" times, invoked ").append(getInvocationCount()).append(
                " times");
    }

}
