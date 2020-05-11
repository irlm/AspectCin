/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.monitor;

import glassbox.response.Response;
import glassbox.response.ResponseFactory;
import glassbox.track.api.*;
import glassbox.util.logging.api.LogManagement;
import glassbox.monitor.AbstractMonitorControl.RuntimeControl;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint.StaticPart;

public abstract class AbstractMonitorClass {
    
    //  monitoring is disabled until after configuration is complete
    protected static boolean allDisabled = true; 
    protected static ResponseFactory responseFactory;
    protected static OperationFactory operationFactory;

    public boolean isEnabled() {
        return RuntimeControl.aspectOf(this).isEnabled();
    }
    public void setEnabled(boolean enabled) {
        RuntimeControl.aspectOf(this).setEnabled(enabled);
    }
    public static void setThisThreadEnabled(boolean enabled) {
//        System.err.println("setEnabled "+enabled+", "+Thread.currentThread());
        RuntimeControl.requestEnabled.set(enabled ? Boolean.TRUE : Boolean.FALSE);
    }
    public static boolean isThisThreadEnabled() {
        return RuntimeControl.requestEnabled.get() == Boolean.TRUE;
    }

    //TODO: inject this instead? we will want a default value and not to configure all monitors!
    protected FailureDetectionStrategy failureDetectionStrategy = new DefaultFailureDetectionStrategy();       
    
    public FailureDetectionStrategy getFailureDetectionStrategy() {
        return failureDetectionStrategy;
    }
    
    public void setFailureDetectionStrategy(FailureDetectionStrategy failureDetectionStrategy) {
        this.failureDetectionStrategy = failureDetectionStrategy;
    }
    
    /** areAllDisabled isn't valid for JavaBeans */
    public static boolean getAllDisabled() {
        return allDisabled;
    }

    public static void setAllDisabled(boolean newAllDisabled) {
        allDisabled = newAllDisabled;
    }

    /**
     * sets the value globally...
     */
    public static void setResponseFactory(ResponseFactory factory) {
        responseFactory = factory;
    }
    public static ResponseFactory getResponseFactory() {
        return responseFactory;
    }
    
    public static void setOperationFactory(OperationFactory factory) {
        operationFactory = factory;
    }
    public static OperationFactory getOperationFactory() {
        return operationFactory;
    }
    
    protected void endException(Throwable t, Object sourceLocation) {
        Response response = getValidResponse(sourceLocation);
        if (response != null) {
            endException(response, t);
        }
    }
    
    protected void endNormally(Object sourceLocation) {
        Response response = getValidResponse(sourceLocation);
        if (response != null) {
            response.complete();
        }
    }

    Response getValidResponse(Object sourceLocation) {
        if (responseFactory == null) {
            logError("null factory for "+getClass().getClassLoader());
            return null;
        }
        
        Response response = responseFactory.getLastResponse();
        if (response == null) {
            logMismatch("no responses left", sourceLocation);
            return null;
        }
        String responseClass = (String)response.get("monitor.class");
        if (!getClass().getName().equals(responseClass)) {
            logMismatch("trying to pop response from '"+responseClass+"'", sourceLocation);
            return null;
        }
        
        return response;
    }
    
    protected void logMismatch(String cause, Object sourceLocation) {        
        logError("Monitoring problem: mismatched monitor calls at "+sourceLocation+": "+cause+
                " in monitor '"+getClass().getName()+"' on "+Thread.currentThread());
    }
    
    public String getLayer() {
        return Response.OTHER;
    }

    protected Serializable getKey(Object identifier) {
        return (Serializable)identifier;
    }

    // these helpers should move to operationFactory too...
    protected Response begin(Serializable key) {
        return begin(key, getLayer());
    }
    
    protected Response begin(Serializable key, String layer) {
        Response response = createResponse(key, layer);
        response.start();
        return response;
    }

    protected Response createResponse(Serializable key, String layer) {
        Response response = responseFactory.getResponse(key);
        response.setLayer(layer);
        response.set("monitor.class", getClass().getName());
        return response;
    }
    
    protected Response begin(Serializable key, Integer priority) {
        Response response = createResponse(key, priority);         
        response.start();
        return response;
    }
    
    protected Response createResponse(Serializable key, Integer priority) {
        Response response = createResponse(key, getLayer());
        response.set(Response.OPERATION_PRIORITY, priority);
        return response;
    }        
    
    protected Response begin(Serializable key, Integer priority, Request request) {
        Response response = createResponse(key, priority);         
        response.set(Response.REQUEST, request);
        response.start();
        return response;
    }        

    protected Response begin(Serializable key, String layer, Serializable resourceKey) {
        Response response = createResponse(key, layer);
        response.set(Response.RESOURCE_KEY, resourceKey);
        response.start();
        return response;
    }

    protected void recordException(Response response, Throwable t) {
        FailureDescription description = failureDetectionStrategy.getFailureDescription(t);
        if (description != null) {
            if (description.getSeverity() >= FailureDetectionStrategy.FAILURE) {
                response.set(Response.FAILURE_DATA, description);
            } else if (description.getSeverity() >= FailureDetectionStrategy.FAILURE) {
                response.set(Response.EXCEPTION_WARNING, description);
            }
        }
    }
    
    protected void endException(Response response, Throwable t) {
        recordException(response, t);
        response.complete();
    }
    
    //optimization to reduce aspect bytecode size
    protected void logError(String msg, Throwable throwable) {
        LogManagement.getLogger(getClass()).error(msg, throwable);
    }
    
    protected void logError(String msg) {
        LogManagement.getLogger(getClass()).error(msg);
    }
    
    protected void logInfo(String msg) {
        LogManagement.getLogger(getClass()).info(msg);
    }
    
    protected void logDebug(String msg) {
        LogManagement.getLogger(getClass()).debug(msg);
    }
}
