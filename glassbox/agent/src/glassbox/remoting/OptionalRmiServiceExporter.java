/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.remoting;

import glassbox.config.OptionalBean;

import java.net.MalformedURLException;
import java.rmi.*;

import org.springframework.remoting.rmi.RmiServiceExporter;

public class OptionalRmiServiceExporter extends RmiServiceExporter implements OptionalBean {

    // boilerplate code to provide a hook for advising
    public void afterPropertiesSet() throws RemoteException {
        super.afterPropertiesSet();
    }

    // boilerplate code to provide a hook for advising
    public void destroy() throws RemoteException {
        super.destroy();
    }

    public void onError(Exception e) {
        // using RemoteException.detail is both backwards compatible with JDK 1.3 and also avoids an apparent JDK bug whereby there's no cause for MalformedURLException 
        if (e instanceof RemoteException) {
            Throwable c1 = ((RemoteException)e).detail;
            if (c1 instanceof UnmarshalException) {
                Throwable c2 = ((RemoteException)c1).detail;
                if (c2 instanceof MalformedURLException) {
                    logWarn("Unable to register rmi service. This is normally caused by running a remote server where there are spaces in the classpath, "+
                            "due to an RMI bug. To allow remote connections to this server, please add the following Java options to your startup script:\n"+
                            "-Djava.rmi.server.useCodebaseOnly=true -Djava.rmi.server.codebase=http://myserver:myport\nFor example\n:-Djava.rmi.server.useCodebaseOnly=true -Djava.rmi.server.codebase=http://localhost:8080");
                    logDebug("Root cause", e);
                    return;
                }
            }
        }
        defaultOnError(e);
    }
}
