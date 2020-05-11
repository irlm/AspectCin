/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.agent.api.ApiType;
import glassbox.config.extension.api.OperationPlugin;
import glassbox.config.extension.api.PluginRegistryLocator;

import java.io.Serializable;



/**
 * Immutable struct to describe an operation.
 * 
 * @author Ron Bodkin
 *
 */
public class OperationDescriptionImpl implements Serializable, ApiType, CallDescription, OperationDescription {
    private final String operationType; // servlet, spring, struts, custom, etc. ?
    private final String operationName;
    final private String shortName;
    private final String contextName;
    private final int callType;
    private OperationDescription parent;

    //PluginTracking:
    private String pluginKey;
    
    public void setPluginKey(String key) { pluginKey = key; }
    public String getPluginKey() { return pluginKey; }

    public OperationPlugin getPlugin() {
        return PluginRegistryLocator.getRegistry().lookupOperationPlugin(getPluginKey());
    }
    public void setPlugin(OperationPlugin plugin) {
        setPluginKey(plugin==null ? null : plugin.getKey());
    }
    
//    after(PluginHolder original, PluginHolder copy) returning : 
//        execution(PluginHolder+.new(PluginHolder+)) && this(copy) && args(original) {
//        copy.setPluginKey(original.getPluginKey());
//    }

    public OperationDescriptionImpl(String operationType, String operationName, String shortName, String contextName, boolean isRemote) {
        if (operationType == null || operationName == null) {
            throw new IllegalArgumentException("null arg(s) "+operationType+", "+operationName);
        }
        this.operationType = operationType;
        this.operationName = operationName;
        this.shortName = shortName;
        this.contextName = contextName;
        if (isRemote) {
            callType = REMOTE_CALL;
        } else {
            callType = OPERATION_PROCESSING;
        }               
    }
    
    public OperationDescriptionImpl(OperationDescriptionImpl copy) {
        operationType = copy.operationType;
        operationName = copy.operationName;
        shortName = copy.shortName;
        contextName = copy.contextName;
        parent = copy.parent;
        callType = copy.callType;
        pluginKey = copy.pluginKey;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getOperationType()
     */
    public String getOperationType() {
        return operationType;
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getOperationName()
     */
    public String getOperationName() {
        return operationName;
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getShortName()
     */
    public String getShortName() {
        return shortName;
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getContextName()
     */
    public String getContextName() {
        return contextName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 83;
        int result = 1;
        result = PRIME * result + ((contextName == null) ? 0 : contextName.hashCode());
        result = PRIME * result + ((operationName == null) ? 0 : operationName.hashCode());
        result = PRIME * result + ((operationType == null) ? 0 : operationType.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OperationDescriptionImpl other = (OperationDescriptionImpl) obj;
        if (contextName == null) {
            if (other.contextName != null)
                return false;
        } else if (!contextName.equals(other.contextName))
            return false;
        if (operationName == null) {
            if (other.operationName != null)
                return false;
        } else if (!operationName.equals(other.operationName))
            return false;
        if (operationType == null) {
            if (other.operationType != null)
                return false;
        } else if (!operationType.equals(other.operationType))
            return false;
        return true;
    }

    /** debug output string */
    public String toString() {
        return "operation(type "+operationType+"; name "+operationName+")"; 
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#callType()
     */
    public int callType() {
        return callType;
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getSummary()
     */
    public String getSummary() {
        return getOperationName().toString();
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getCallKey()
     */
    public Serializable getCallKey() {
        return getOperationName();
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getResourceKey()
     */
    public Serializable getResourceKey() {
        return getContextName();
    }

    private static final long serialVersionUID = 1;

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#getParent()
     */
    public OperationDescription getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.OperationDescription#setParent(glassbox.track.api.OperationDescriptionImpl)
     */
    public void setParent(OperationDescription parent) {
        this.parent = parent;
    }
}
