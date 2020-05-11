/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.thread.context;

import glassbox.util.logging.api.LogManagement;

import java.lang.reflect.Field;

// this would be a shared global aspect but this function needs to apply before the monitor is installed and after...
// we can't even pass in the SavedContext impl because we need this before initializing

public aspect MonitorContextLoaderManagement {
    protected pointcut contextSavePoint() :
        execution(* LogManagement.getLogger(String));

    private static Field threadContextLoaderField = initField();
    private static final Object DONT_RESTORE_LOADER = MonitorContextLoaderManagement.class;
    
    private static Field initField() {
        try {
            Field field = Thread.class.getDeclaredField("contextClassLoader");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            cantAccessFieldWarning();
            return null;
        }
    }    

    private static void cantAccessFieldWarning() {
        System.err.println("Warning: can't restore context loader. This is only a problem on Oracle application servers.");
    }

    Object around() : contextSavePoint() {
        SavedContext context = new SavedContext();
        try {
            return proceed();
        } finally {
            context.restore();
        }
    }

    public static class SavedContext {
        private Object contextLoaderFieldVal = DONT_RESTORE_LOADER;
        private ClassLoader contextLoader;
        public SavedContext() {
            Thread thread = Thread.currentThread();
            if (threadContextLoaderField != null) {
                try {
                    contextLoaderFieldVal = threadContextLoaderField.get(thread);
                } catch (IllegalAccessException ae) {
                    cantAccessFieldWarning();
                    threadContextLoaderField = null;
                }
            }
            contextLoader = thread.getContextClassLoader();
        }
        
        public void restore() {
            Thread thread = Thread.currentThread();
            thread.setContextClassLoader(contextLoader);
            if (contextLoaderFieldVal != DONT_RESTORE_LOADER) {
                try {
                    threadContextLoaderField.set(thread, contextLoaderFieldVal);
                    //System.err.println("Reset loader to "+contextLoaderFieldVal);
                } catch (IllegalAccessException ae) {
                    cantAccessFieldWarning();
                    threadContextLoaderField = null;
                }
            }
        }
    }
}
