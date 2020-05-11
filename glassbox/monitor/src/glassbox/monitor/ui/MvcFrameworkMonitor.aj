/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.ui;

import glassbox.monitor.AbstractMonitor;
import glassbox.response.Response;
import glassbox.track.api.OperationDescription;

public abstract aspect MvcFrameworkMonitor extends AbstractMonitor {

    protected String controllerType;
    
    /**
     * Please convert to use the constructor that takes a controller type argument.
     * Will be removed in a future version.  
     * 
     * @deprecated
     */
    protected MvcFrameworkMonitor() {
    }
    
    protected MvcFrameworkMonitor(String controllerType) {
        this.controllerType = controllerType;
    }

    /** 
     * This defaults to no join points. If a concrete aspect overrides <code>classControllerExec</code> with a concrete definition,
     * then the monitor will track operations at matching join points based on the <em>class</em> of the controller object.
     */ 
    protected pointcut classControllerExec(Object controller); 

    /** 
     * This defaults to no join points. If a concrete monitor overrides <code>methodSignatureControllerExec</code> with a concrete definition,
     * then it will track operations at matching join points based on the runtime class of the executing controller instance and the method
     * signature at the join point.
     */ 
    protected pointcut methodSignatureControllerExec(Object controller);   

    /** 
     * This defaults to no join points. If a concrete monitor overrides <code>methodNameControllerExec</code> with a concrete definition,
     * then it will track operations at matching join points based on the methodName
     * @param methodName a String name of a method.
     */ 
    protected pointcut methodNameControllerExec(Object controller, String methodName);

    protected pointcut monitorEnd() : 
        classControllerExec(*)||methodSignatureControllerExec(*)||methodNameControllerExec(*,*);
    
    before(Object controller) : classControllerExec(controller) {
        begin(getClassDescriptor(controller.getClass().getName()));
    }
    
    before(Object controller) : methodSignatureControllerExec(controller) {
        begin(getMethodDescriptor(controller.getClass().getName(), thisJoinPointStaticPart.getSignature().getName()));
    }
    
    before(Object controller, String methodName) : methodNameControllerExec(controller, methodName) {
        begin(getMethodDescriptor(controller.getClass().getName(), methodName));
    }
    
    protected OperationDescription getClassDescriptor(String controllerName) {
        return operationFactory.makeOperation(controllerType, controllerName);
    }
    
    protected OperationDescription getMethodDescriptor(String controllerName, String methodName) {
        return operationFactory.makeOperation(controllerType, controllerName, methodName); 
    }
    
    public String getLayer() {
        return Response.UI_CONTROLLER;
    }
}
