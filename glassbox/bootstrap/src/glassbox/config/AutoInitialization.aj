/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

public aspect AutoInitialization extends GlassboxInitializer {
    private static boolean enabled = false;
    
    // for any Web app and most any container, loading in a subclass of Servlet should do... 
    // we will NOT initialize while executing methods on an MBeanServer, to prevent calling back on JMX during initialization
    public pointcut startRunning() :
        staticinitialization(glassbox.monitor.MonitoredType+) && if(initializer==null) && if(enabled);
    //&& !cflow(execution(* MBeanServer.*(..))) 
    
    before() : startRunning() {
        start(true);
    }

    // we don't support shutting down, then restarting with autoinitialization
//    after() : stopRunning() {
//        shutDown();
//    }

    public static void setEnabled(boolean newEnabled) {
        enabled = newEnabled;
    }
}
