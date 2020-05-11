/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.concurrent;

import glassbox.util.logging.api.LogManagement;

import java.lang.reflect.Constructor;


public class ConcurrentFactory {

    public static IAtomicBoolean makeAtomicBoolean(boolean startValue) {
        try {
            return new Java5AtomicBoolean(true);
        } catch (NoClassDefFoundError e) {
            return new BackportedAtomicBoolean(true);
        }
    }

    public static IConcurrentMap makeConcurrentMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        try {
            return new Java5ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
        } catch (NoClassDefFoundError e) {
            try {
                return (IConcurrentMap) getFullConstructor().newInstance(new Object[] {
                        new Integer(initialCapacity), new Float(loadFactor), new Integer(concurrencyLevel)});
            } catch (Throwable t) {
                LogManagement.getLogger().error("Can't create concurrent map. Please reinstall Glassbox or contact support.", t);
                IllegalStateException exc = new IllegalStateException();
                exc.initCause(t);
                throw exc;
            }
        }
    }
    
    public static IConcurrentMap makeConcurrentMap(int sz) {
        try {
            return new Java5ConcurrentHashMap(sz);
        } catch (NoClassDefFoundError e) {
            try {
                return (IConcurrentMap) getConstructor().newInstance(new Object[] { new Integer(sz) });
            } catch (Throwable t) {
                LogManagement.getLogger().error("Can't create concurrent map. Please reinstall Glassbox or contact support.", t);
                IllegalStateException exc = new IllegalStateException();
                exc.initCause(t);
                throw exc;
            }
        }
    }

    private static Constructor getFullConstructor() throws Exception {
        if (backportConcurrentMapFullConstructor == null) {
            Class clazz = Class.forName("glassbox.util.concurrent.BackportedConcurrentHashMap", false,
                    Thread.currentThread().getContextClassLoader());
            backportConcurrentMapFullConstructor = clazz.getConstructor(new Class[]{Integer.TYPE, Float.TYPE, Integer.TYPE});
        }
        return backportConcurrentMapFullConstructor;
    }
    
    private static Constructor getConstructor() throws Exception {
        if (backportConcurrentMapConstructor == null) {
            Class clazz = Class.forName("glassbox.util.concurrent.BackportedConcurrentHashMap", false,
                    Thread.currentThread().getContextClassLoader());
            backportConcurrentMapConstructor = clazz.getConstructor(new Class[]{Integer.TYPE});
        }
        return backportConcurrentMapConstructor;
    }

    private static Constructor backportConcurrentMapConstructor;
    private static Constructor backportConcurrentMapFullConstructor;
}
