/*
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 */
package org.aspectj.ext.ltw13;

import org.aspectj.weaver.loadtime.Aj;
import org.aspectj.weaver.loadtime.ClassPreProcessor;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassPreProcessorAdapter implements org.codehaus.aspectwerkz.hook.ClassPreProcessor {

    /**
     * Concrete preprocessor.
     */
    private static ClassPreProcessor s_preProcessor;
    private boolean verbose = Boolean.getBoolean("aj.weaving.verbose");

    private ThreadLocal recursionDepth = new ThreadLocal() {
        public Object initialValue() {
            return new int[] { 0 };
        }
    };

    static {
        try {
            s_preProcessor = new Aj();
            s_preProcessor.initialize();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("could not initialize preprocessor due to: " + e.toString());
        }
    }

    public void initialize() {
        ;
    }

    public byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader) {
        // skip bootCL
        if (classLoader == null) {
            return bytes;
        }

        // skip AJ weaver well know stuff to avoid circularity
        if (className != null) {
            String slashed = className.replace('.', '/');
            if (slashed.startsWith("org/aspectj/weaver/")
                || slashed.startsWith("org/aspectj/bridge/")
                || slashed.startsWith("org/aspectj/util/")
                || slashed.startsWith("org/aspectj/apache/bcel")
                || slashed.startsWith("org/aspectj/lang/")
            ) {
                if (verbose) cantWeave("AspectJ classes", className, classLoader);
                return bytes;
            }
        }
        int[] depth = (int[])recursionDepth.get();        
        depth[0]++;
        try {
            if (depth[0] == 1) {
                synchronized(classLoader) {
                    return s_preProcessor.preProcess(className, bytes, classLoader);
                }
            } else {
                if (verbose) cantWeave("Recursively loaded", className, classLoader);  
                return bytes;
            }
        } finally {
            depth[0]--;            
        }
    }
    
    private void cantWeave(String msg, String className, ClassLoader classLoader) {
        System.err.println("["+classLoader.getClass().getName()+"@"+System.identityHashCode(classLoader)+
                " info not weaving " + className + ": "+msg);
    }

}
