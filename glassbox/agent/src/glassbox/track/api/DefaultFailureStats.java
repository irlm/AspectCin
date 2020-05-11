/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.track.api;

import java.util.LinkedList;
import java.util.List;

public class DefaultFailureStats implements Comparable, FailureStats {
    protected FailureDescription description;
    protected int count;
    protected long lastSeen;
    public static final int MAX_REQUESTS = 5;
    protected LinkedList recentRequests = new LinkedList();

    public DefaultFailureStats(FailureDescription description) {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.FailureStats#addInstance(glassbox.track.api.FailureDescription)
     */
    public void recordInstance(Request failingRequest) {
        recentRequests.add(failingRequest);
        if (recentRequests.size() > MAX_REQUESTS) {
            recentRequests.removeLast();
        }
        increment();
    }
    
    protected void increment() {
        count++;
        lastSeen = System.currentTimeMillis();
    }
    
    public int compareTo(Object obj) {
        DefaultFailureStats fds = (DefaultFailureStats)obj;
        int sevDelta = (description.getSeverity() - fds.description.getSeverity());
        if (sevDelta != 0) {
            return sevDelta;
        }
        
        int countDelta = (count - fds.count);
        if (countDelta != 0) {
            return countDelta;
        }
        
        if (lastSeen < fds.lastSeen) {
            return -1;
        } else if (lastSeen == fds.lastSeen) {
            return 0;
        } else {
            return 1;
        }
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.ProblemStats#getCount()
     */
    /* (non-Javadoc)
     * @see glassbox.track.api.FailureStats#getCount()
     */
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.ProblemStats#getDescription()
     */
    /* (non-Javadoc)
     * @see glassbox.track.api.FailureStats#getDescription()
     */
    public FailureDescription getFailure() {
        return description;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.FailureStats#getCall()
     */
    public CallDescription getCall() {
        return description.getCall();
    }

    public List getRecentRequests() {
        return recentRequests;
    }

    // need to normalize to Glassbox-time if we are going to expose this one
//    public long lastSeen() {
//        return lastSeen;
//    }
    private static final long serialVersionUID = 1L;
}