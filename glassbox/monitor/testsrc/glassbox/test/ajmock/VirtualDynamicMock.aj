/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.jmock.core.AbstractDynamicMock;
import org.jmock.core.DynamicMock;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationDispatcher;

import sun.misc.Unsafe;

public class VirtualDynamicMock extends AbstractDynamicMock {
    private PointcutExpression pointcutExpr;
    
    public VirtualDynamicMock(String pointcutExprStr, InvocationDispatcher invocationDispatcher) {
        super(null, "mock "+pointcutExprStr, invocationDispatcher);
        this.pointcutExpr = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().parsePointcutExpression(pointcutExprStr);
    }
    
    public Object proxy() {
        throw new RuntimeException("operation not supported");// XXX bad
    }
    
    public PointcutExpression getPointcutExpression() {
        return pointcutExpr;
    }
//  protected Object mockInvocation(Invocation invocation) {
//  if (failure != null) {
//  throw failure;
//  }
//  try {
//  Object result = invocationDispatcher.dispatch(invocation);
//  invocation.checkReturnTypeCompatibility(result);
//  return result;
//  } catch (AssertionFailedError error) {
//  failure = new DynamicMockError(this, invocation, invocationDispatcher, error.getMessage());
//  failure.fillInStackTrace();
//  throw failure;
//  }
//  }

    static aspect MockProcessing percflow(scope() && execution(void VirtualMockObjectTestCase+.test*(..))) {
        private List activeMocks = new ArrayList(5);

        after(VirtualDynamicMock vdMock) returning:  execution(VirtualDynamicMock.new(..)) && this(vdMock) {
            activeMocks.add(vdMock);
        }
        
        public VirtualDynamicMock getFirstMatch(JoinPoint joinPoint) {
            for (Iterator it = activeMocks.iterator(); it.hasNext();) {
                VirtualDynamicMock vdMock = (VirtualDynamicMock)it.next();                
                if (matches(vdMock.getPointcutExpression(), joinPoint)) {
                    return vdMock;
                }
            }
            return null;
        }

        pointcut scope();
        //: !within(glassbox.test..*) && within(glassbox..*) && 
            //!call(* glassbox.test..*(..)) && !call(* org.jmock..*(..)) && !call(* org.junit..*(..)) && !call(* org.springframework.test..*(..) )&& !target(DynamicMock+);

        //declare warning:   !within(glassbox.test..*) && within(glassbox..*) && 
        //!call(* glassbox.test..*(..)) && !call(* org.jmock..*(..)) && !call(* org.junit..*(..)) && !call(* org.springframework.test..*(..) )&&
        //call(* glassbox.config.ApplicationLifecycleAware.startUp()): "start up";
        
        Object around() : scope() && call(* *(..)) { // || execution(* *(..)) || adviceexecution() || call(new(..)) || execution(new(..))) {
            VirtualDynamicMock vdMock = getFirstMatch(thisJoinPoint);
            // not handled yet: >1 matches per join point... eventually might need precedence
            if (vdMock != null) {
                Invocation jpInvocation = new JpInvocation(thisJoinPoint) {
                    public Object doProceed() {
                        return proceed();
                    }
                };
                try {
                    return vdMock.mockInvocation(jpInvocation);
                } catch (final Throwable t) {
                    Thrower.throwException(t);
                    return null;
                }
            } else {
                return proceed();
            }
        }
        
        static boolean matches(PointcutExpression expr, JoinPoint jp) {
            FuzzyBoolean result;
            // TODO: replace with an OO means of handling this...
            if (JoinPoint.METHOD_CALL.equals(jp.getKind())) {
                try {
                    MethodSignature sig = (MethodSignature)jp.getSignature();
                    //work-around: force computation of declaringType
                    jp.getSignature().getDeclaringType();
                    // is getThis() right if static method?
                    // TODO: figure out how to get the Member making the call... maybe construct a stack trace?!
                    Class thisClass = jp.getThis()==null ? null : jp.getThis().getClass(); 
                    Class targetClass = jp.getTarget()==null ? null : jp.getTarget().getClass();
                    if (sig==null || sig.getMethod() == null) {
                        System.out.println("null sig/method "+jp);
                        return false;
                    }
                    result = FuzzyBoolean.NO;
//TODO
                    //result = expr.matchesMethodCall(sig.getMethod(), thisClass, targetClass, null);
                } catch (Throwable t) {
                    System.err.println("boom ");
                    t.printStackTrace();
                    return false;
                }
            } else {
                throw new IllegalArgumentException("only method call prototyped");
            }
            if (result == FuzzyBoolean.YES) {
                return true;
            } else if (result == FuzzyBoolean.NO) {
                return false;
            } else {
            	return false;
//TODO
                //return expr.matchesDynamically(jp.getThis(), jp.getTarget(), jp.getArgs());
            }
        }
    }    
}

