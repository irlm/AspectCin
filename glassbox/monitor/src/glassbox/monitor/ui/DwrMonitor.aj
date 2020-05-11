/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.ui;

import glassbox.response.Response;

import java.lang.reflect.Method;

// this version only supports DWR 1.x: in DWR 2.x there are ajaxFilters which let us see the methods being executed...
public aspect DwrMonitor extends MvcFrameworkMonitor {
    private static final String REQUEST_TYPE = "AjaxRequest"; 
    private pointcut inExecQuery() : 
        (within(uk.ltd.getahead.dwr.impl.ExecuteQuery) || within(uk.ltd.getahead.dwr.ExecuteQuery));
    
    public pointcut dwrQuery(Method method, Object receiver, Object[] params) : 
        inExecQuery() && withincode(* execute(..)) && 
        call(* Method.invoke(..)) && args(receiver, params) && target(method);

    protected pointcut monitorEnd() : dwrQuery(*, *, *);
    

    
    public DwrMonitor() {
        super(REQUEST_TYPE);
    }
    
    // this could be reused as "reflection method"
    before(Method method, Object receiver, Object[] params) : dwrQuery(method, receiver, params) {
        Response response = begin(getMethodDescriptor(method.getDeclaringClass().getName(), method.getName()));
        response.set(Response.PARAMETERS, params);
    }    

    // we can't use the type here: it is likely to not be visible or not the same class in the monitor as in an app...
//    after(String scriptName) returning (Object creator) : getCreator(scriptName) {
//        logDebug("Calling "+scriptName+" with creator "+creator.toString());
//        Response response = responseFactory.getLastResponse();
//        boolean newRequest = true;
//        if (response!=null && response.getKey() instanceof OperationDescriptionImpl) {
//            OperationDescriptionImpl key = (OperationDescriptionImpl)response.getKey();
//            if (REQUEST_TYPE.equals(key.getOperationType())) {
//                // there's an already a operation in progress
//                response.complete();
//            
//                response = responseFactory.getLastResponse();            
//                newRequest = !(scriptName==response.getKey() || (scriptName!=null && scriptName.equals(key.getKey())));
//                if (newRequest) {
//                    response.complete();
//                }
//            }
//        }
//        if (newRequest) {
//            begin();
//        }            
//    }    
}
