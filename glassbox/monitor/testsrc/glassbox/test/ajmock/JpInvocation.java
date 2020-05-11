/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test.ajmock;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.AdviceSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.jmock.core.Formatting;
import org.jmock.core.Invocation;

public abstract class JpInvocation extends Invocation {
    public JpInvocation(JoinPoint joinPoint) {
        // how will jMock handle null for this?
        // because of that, we use joinPoint as this instead!
        // super(joinPoint.getThis(), getMethod(joinPoint), joinPoint.getArgs());
        super(joinPoint, getMethod(joinPoint), joinPoint.getArgs()); 
    }
    
    public static Method getMethod(JoinPoint joinPoint) {
        Signature sig = joinPoint.getSignature();
        if (sig instanceof MethodSignature) {
            sig.getDeclaringType();
            return ((MethodSignature)sig).getMethod();
        } else if (sig instanceof AdviceSignature) {
            return ((AdviceSignature)sig).getAdvice();
        } else {
            return null; // no provision yet for field access virtual mocks
        }
    }
    
    public StringBuffer describeTo( StringBuffer buffer ) {
        buffer.append(((JoinPoint)invokedObject).getThis().toString()).append(".").append(invokedObject.toString());
        Formatting.join(parameterValues, buffer, "(", ")");
        return buffer;
    }
    
    public abstract Object doProceed();    
}