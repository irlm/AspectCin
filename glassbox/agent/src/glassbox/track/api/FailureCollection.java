/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.util.*;
import java.util.Map.Entry;

public class FailureCollection {
    private Map/*<FailureDescription,DefaultFailureStats>*/ worstFailures;
    private int sizeLimit;
    private static long serialVersionUID = 1L;
    
    public FailureCollection(int sizeLimit) {
        worstFailures = new HashMap((sizeLimit*3)/2);
        this.sizeLimit = sizeLimit-1;
    }
    
    public FailureCollection(FailureCollection collection) {
        worstFailures = new HashMap(collection.worstFailures);
        sizeLimit = collection.sizeLimit;
    }
    
    public synchronized FailureStats add(FailureDescription failureDescription, Request request) {
        FailureStats info = (FailureStats)worstFailures.get(failureDescription);
        if (info == null) {
            info = addFailure(failureDescription);
        } 
        info.recordInstance(request);
        return info;
    }

    protected DefaultFailureStats addFailure(FailureDescription failureDescription) {
        while (worstFailures.size() > sizeLimit) {            
            purgeLowPriority();
        }
        
        // the newest failure always makes it in...
        DefaultFailureStats stats = new DefaultFailureStats(failureDescription);
        worstFailures.put(failureDescription, stats);
        
        return stats;
    }
    
    protected void purgeLowPriority() {
        Iterator it = worstFailures.entrySet().iterator();
        if (!it.hasNext()) return;
        Entry entry = (Entry)it.next();
        FailureDescription min = (FailureDescription)entry.getKey();
        DefaultFailureStats minInfo = (DefaultFailureStats)entry.getValue();
        
        while (it.hasNext()) {
            entry = (Entry)it.next();
            FailureDescription failureDescription = (FailureDescription)entry.getKey();
            if (failureDescription.getSeverity() < min.getSeverity()) {
                min = failureDescription;
                minInfo = (DefaultFailureStats)entry.getValue();                    
            } else if (failureDescription.getSeverity() == min.getSeverity()) {                    
                DefaultFailureStats info = (DefaultFailureStats)entry.getValue();                    
                if (info.count < minInfo.count || (info.count == minInfo.count && info.lastSeen < minInfo.lastSeen)) {
                    min = failureDescription;
                    minInfo = info;
                }
            }
        }
        worstFailures.remove(min);
            
        while (worstFailures.size() > sizeLimit) {
            it = worstFailures.entrySet().iterator();
            it.next();
            it.remove();
        }
    }

    public synchronized List getList() {
        FailureStats stats[] = new FailureStats[worstFailures.size()];
        int i=0;
        for (Iterator it = worstFailures.values().iterator(); it.hasNext();) {            
            stats[i++] = (FailureStats)it.next(); 
        }
        
        Arrays.sort(stats);
        return Arrays.asList(stats);
    }
    
}
