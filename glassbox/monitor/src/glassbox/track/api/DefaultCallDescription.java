/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class DefaultCallDescription implements CallDescription {
    private ThreadState threadState;
    private Serializable callKey;
    private Serializable resourceKey;
    private List slowestSingleEvents = new ArrayList(0);
    private int callType;
    
    public DefaultCallDescription() {
    }         
    
    public DefaultCallDescription(Serializable resourceKey, Serializable key, int callType) {
        this.resourceKey = resourceKey;
        this.callKey = key;
        this.callType = callType;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.CallDescription#getCallKey()
     */
    public Serializable getCallKey() {
        return callKey;
    }
    
    public List getSlowestSingleEvents() {
        return slowestSingleEvents;        
    }
    
//    public void setSlowestSingleEvents(List slowestSingleEvents) {
//        this.slowestSingleEvents = slowestSingleEvents;        
//    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.CallDescription#description()
     */
    public String getSummary() {
        if (callType == DATABASE_CONNECTION) {
            return "connection to database "+resourceKey;
        }
        return callKey.toString();
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.CallDescription#callType()
     */
    public int callType() {
        return callType;
    }
    
    public void setCallKey(Serializable key) {
        this.callKey = key;
    }
    public ThreadState getThreadState() {
        return threadState;
    }
    public void setThreadState(ThreadState threadState) {
        this.threadState = threadState;
    }
    
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof DefaultCallDescription)) {
            return false;
        }
        DefaultCallDescription otherDesc = (DefaultCallDescription)other;
        if (callType != otherDesc.callType) {
            return false;
        }
        if (getThreadState() == null) {
            if (otherDesc.getThreadState() != null) {
                return false;
            }
        } else {
            if (!getThreadState().equals(otherDesc.getThreadState())) {
                return false;
            }
        }
        if (getCallKey() == null) {
            if (otherDesc.getCallKey() != null) {
                return false;
            }
        } else if (!getCallKey().equals(otherDesc.getCallKey())) {
            return false;
        }
        if (getResourceKey() == null) {
            if (otherDesc.getResourceKey() != null) {
                return false;
            }
            return true;
        }
       return getResourceKey().equals(otherDesc.getResourceKey());        
    }
    
    public int hashCode() {
        int code = (getCallKey()==null ? 0x12399034 : getCallKey().hashCode()*274) + callType;
        code *= (getResourceKey()==null ? 0xf32ca155 : getResourceKey().hashCode());
        code += (getThreadState() == null ? 0xbdce987 : getThreadState().hashCode()+0xac23de77);
        return code;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.CallDescription#getResourceKey()
     */
    public Serializable getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(Serializable databaseKey) {
        this.resourceKey = databaseKey;
    }
    
    private static final long serialVersionUID = 2;
}
