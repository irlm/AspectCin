/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.track.api;

import java.util.Iterator;
import java.util.Map.Entry;


aspect EnforceContracts {
    
    after() returning (PerfStats stats): execution(* StatisticsRegistry.getPerfStats(..)) {
        if (stats.getType() == StatisticsTypeImpl.RemoteCall) {
            if (stats.getKey() instanceof String) {
                String key = (String)stats.getKey();
                if (key.indexOf(":") == -1) {
                    throw new IllegalArgumentException("Remote key must have resource indicator prefixed by at least one colon (:) in "+stats.getKey());
                }
            } else if (!(stats.getKey() instanceof OperationDescription)) {
                throw new IllegalArgumentException("Invalid remote key: "+stats.getKey());
            }
        }
    }
    
    private static final long serialVersionUID = 1;
}	
