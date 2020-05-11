/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 30, 2005
 */
package glassbox.monitor.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * 
 * @author Ron Bodkin
 * @author Joseph Shoop
 */
public aspect SpringMvcRequestMonitor extends MvcFrameworkMonitor {
    
    public SpringMvcRequestMonitor() {
        super("org.springframework.web.servlet.mvc.Controller");
    }

    public pointcut springControllerExec() :
        execution(public ModelAndView *(HttpServletRequest, HttpServletResponse, ..));

    protected pointcut classControllerExec(Object controller) :
        within(Controller+) && springControllerExec() && execution(* handleRequest(..)) && this(controller) && !this(MultiActionController+);

    // this is a little more costly: we have to examine all methods in the system
    // to determine if they are possible multi-action controller methods
//    protected pointcut methodSignatureControllerExec(Object controller) :
//        springControllerExec() && this(controller) && !classControllerExec(*);

    // alternative approach that requires just weaving one method inside spring...
    protected pointcut methodNameControllerExec(Object controller, String methodName) :
        within(MultiActionController) && execution(* invokeNamedMethod(..)) && args(methodName, ..) && this(controller);

}
