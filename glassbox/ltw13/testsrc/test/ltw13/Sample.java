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

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Sample {

    public static void main(String args[]) throws Throwable {
        new Sample().target();
    }

    public void target() {
        System.out.println("Sample.target");
    }
    
}
