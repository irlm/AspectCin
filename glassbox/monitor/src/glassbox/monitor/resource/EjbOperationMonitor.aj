/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.ui.ServletRequestMonitor;
import glassbox.response.Response;
import glassbox.track.api.OperationDescription;

import org.aspectj.lang.JoinPoint.StaticPart;

/** monitors execution of EJB operations in the EJB container */
public aspect EjbOperationMonitor extends AbstractMonitor {
    private static final Class ejbClass = EjbCallMonitor.findClassIfPresent("javax.ejb.EnterpriseBean", EjbOperationMonitor.class.getClassLoader());
    
    public pointcut ejbMethod(Object ejb) : 
        within(javax.ejb.EnterpriseBean+) && execution(public * *(..)) && this(ejb);

    protected pointcut monitorEnd() : ejbMethod(*);
    protected pointcut topLevelPoint() : monitorEnd();    
    
//    declare parents: javax.ejb.EjbContext implements IEjbContext;   
//    before() : within(javax.ejb.SessionBean+) && execution(* setSessionContext(*)) && args(context) {
//        setContext();
//    }    
//    setEntityContext(EntityContext ctx) 
    
    before(Object ejb) : ejbMethod(ejb) {
        Class interfaze = ejbClass;
        if (interfaze == null) {
            interfaze = EjbCallMonitor.findClassIfPresent("javax.ejb.EnterpriseBean", ejb.getClass().getClassLoader());
        }
        begin("Enterprise Java Bean", ejb, interfaze, thisJoinPointStaticPart, thisJoinPoint.getArgs());
    }
    
    // we don't know the ejb context ... we could track it for all EJB's in the system 
    private void begin(String typeName, Object ejb, Class type, StaticPart staticPart, Object[] argz) {
        OperationDescription operation = makeEjbOperation("Enterprise Java Bean", ejb, type, staticPart.getSignature().getName()); 
        Response response = begin(operation, ServletRequestMonitor.JSP_PRIORITY);
        response.set(Response.PARAMETERS, argz);
    }
    
    public String getLayer() {
        return Response.SERVICE_PROCESSOR;
    }
    
    protected OperationDescription makeEjbOperation(String typeName, Object ejb, Class interfaze, String methodName) {
        String ejbTypeName = EjbCallMonitor.aspectOf().getMostDerivedInterface(ejb, interfaze).getName();
//        if (ejbTypeName == null) {
//            ejbTypeName = ejb.getClass().getName(); // fall back...
//        }
        return operationFactory.makeRemoteOperation(typeName, ejbTypeName, methodName);
    }
    
    // better to add ejb.jar and call getEJBMetaData().getRemoteInterfaceClass(),    
}
