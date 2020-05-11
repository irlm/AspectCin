/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package test.ltw13;

import org.aspectj.lang.ProceedingJoinPoint;
public aspect TestAspect {
    
//    public pointcut methodCall() : execution(* *.*(..)) && !within(aop..*) && !within(java..*);
  
    public pointcut methodCall() : execution(* Sample.target(..));
  
    before() : methodCall() {
	System.out.println("---> BEFORE: " + thisJoinPointStaticPart.getSignature().getName());
    }

    //void around() : methodCall() {
//	/System.out.println("---> Around: " + thisJoinPointStaticPart.getSignature().getName());
//	long startTime = System.currentTimeMillis();
//	try {
//	    proceed();
//	} finally {
//	    long total = (System.currentTimeMillis() - startTime);
//	    System.out.println("---> Total time in " + thisJoinPointStaticPart.getSignature().getName() + " : " + total + " nS");
//	}
    //   } 
}
