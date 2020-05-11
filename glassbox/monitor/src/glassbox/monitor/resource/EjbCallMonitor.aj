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
import glassbox.track.api.CallDescription;
import glassbox.track.api.OperationDescription;

import java.rmi.Remote;

import org.aspectj.lang.JoinPoint.StaticPart;


/** monitors calls of EJB operations from outside the EJB container */
public aspect EjbCallMonitor extends AbstractMonitor {
    private static final Class ejbRemoteClass = findClassIfPresent("javax.ejb.EJBObject", EjbCallMonitor.class.getClassLoader());
    private static final Class ejbHomeClass = findClassIfPresent("javax.ejb.EJBHome", EjbCallMonitor.class.getClassLoader());

    private NameResolver nameResolver = new NullNameResolver();
    // we don't track local interfaces yet

    public pointcut ejbRemoteMethod() : 
        within(javax.ejb.EJBObject+) && !within(javax.ejb.EJBHome+) && RemoteCallMonitor.remoteExecution();

    public pointcut ejbHomeMethod() : 
        within(javax.ejb.EJBHome+) && RemoteCallMonitor.remoteExecution();
    
    protected pointcut monitorEnd() : (ejbHomeMethod() || ejbRemoteMethod()) && this(Object);
    
    before(Remote ejb) : ejbRemoteMethod() && this(ejb) {
        Class interfaze = ejbRemoteClass;
        if (interfaze == null) {
            interfaze = findClassIfPresent("javax.ejb.EJBObject", ejb.getClass().getClassLoader());
        }
        begin("Remote EJB", ejb, interfaze, thisJoinPointStaticPart, thisJoinPoint.getArgs());
    }
    
    before(Remote ejb) : ejbHomeMethod() && this(ejb) {
        Class interfaze = ejbHomeClass;
        if (interfaze == null) {
            interfaze = findClassIfPresent("javax.ejb.EJBHome", ejb.getClass().getClassLoader());
        }
        begin("EJB Home", ejb, interfaze, thisJoinPointStaticPart, thisJoinPoint.getArgs());
    }
    
    // we don't know the ejb context ... we could track it for all EJB's in the system
    private void begin(String typeName, Remote ejb, Class type, StaticPart staticPart, Object[] argz) {
        CallDescription aCall = makeEjbCall(typeName, ejb, type, staticPart.getSignature().getName());
        //XXX refactor so this is NOT an operation
        Response response = begin((OperationDescription)aCall, ServletRequestMonitor.JSP_PRIORITY);
        response.set(Response.PARAMETERS, argz);
    }
    
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
    
    // we could use EJBObject and even EJB local implementations
    protected CallDescription makeEjbCall(String typeName, Object ejb, Class interfaze, String methodName) {
        Object name = nameResolver.getName(ejb);

        if (name == null) {
            name = getMostDerivedInterface(ejb, interfaze).getName();
        }

        return operationFactory.makeRemoteOperation(typeName, name.toString(), methodName);
    }
    
    // better to add ejb.jar and call getEJBMetaData().getRemoteInterfaceClass(),
    public Class getMostDerivedInterface(Object ejb, Class interfaze) {
        Class[] implementedInterfaces = ejb.getClass().getInterfaces();
        Class least = interfaze;
        for (int i=0; i<implementedInterfaces.length; i++) {
            if (least.isAssignableFrom(implementedInterfaces[i])) {
                least = implementedInterfaces[i];
            }
        }
        return least;
    }
    
    public boolean implementsClass(Object ejb, Class clazz) {
        // assumes class is visible system-wide...
        return clazz != null && clazz.isAssignableFrom(ejb.getClass());
    }
    
    static Class findClassIfPresent(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (Throwable t) {
            return null;
        }
    }

    public NameResolver getNameResolver() {
        return nameResolver;
    }

    public void setNameResolver(NameResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

}
