/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.monitor.ui;


/** provides standard operation bindings for XML-defined aspects to override */
public abstract aspect TemplateOperationMonitor extends MvcFrameworkMonitor {
    protected pointcut classControllerExecTarget();   
    
    public TemplateOperationMonitor() {
        this.controllerType = getClass().getName()+" monitor";
    }
    
    protected pointcut classControllerExec(Object controller) :
        classControllerExecTarget() && target(controller);
    
    protected pointcut methodSignatureControllerExecTarget();   
    
    protected pointcut methodSignatureControllerExec(Object controller) :  
        methodSignatureControllerExecTarget() && execution(* *(..)) && target(controller);
}
