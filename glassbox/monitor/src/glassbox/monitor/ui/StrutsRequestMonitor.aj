/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.ui;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.struts.action.*;
import org.apache.struts.actions.DispatchAction;

/**
 * 
 * @author Ron Bodkin
 */
public aspect StrutsRequestMonitor extends MvcFrameworkMonitor {

    public StrutsRequestMonitor() {
        super("org.apache.struts.action.Action");
    }
    
    /** 
     * Matches execution of any method defined on a Struts action, or any subclass, which has the right
     * signature for an action execute (or perform) method, including methods dispatched to in a DispatchAction
     * or template methods with the same signature.
     */ 
    public pointcut actionMethodExec() :
        execution(public ActionForward *(ActionMapping, ActionForm, ServletRequest+, ServletResponse+));

    protected pointcut dispatchActionMethodExec() :
        actionMethodExec() && within(DispatchAction+);
    
    /** 
     * Matches execution of an action execute (or perform) method for a Struts action. Supports the Struts 1.0 API (using the perform method)
     * as well as the Struts 1.1 API (using the execute method)
     */ 
    public pointcut rootActionExec() : 
        within(Action+) && !within(DispatchAction+) && actionMethodExec() && (execution(* Action.execute(..)) || execution(* Action.perform(..)));
    
    protected pointcut classControllerExec(Object controller) :
        rootActionExec() && this(controller);
    
    protected pointcut methodSignatureControllerExec(Object controller) :
        dispatchActionMethodExec() && this(controller);

    // alternative implementation that weaves just the Struts dispatch method...
//    protected pointcut methodNameControllerExec(Object controller, String methodName) :
//      execution(* DispatchAction.dispatchMethod(..)) && args(.., methodName) && this(controller);    

}
