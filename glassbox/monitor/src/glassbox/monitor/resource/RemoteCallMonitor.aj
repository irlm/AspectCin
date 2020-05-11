/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import glassbox.monitor.ui.MvcFrameworkMonitor;
import glassbox.monitor.ui.ServletRequestMonitor;
import glassbox.response.Response;
import glassbox.track.api.OperationDescription;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** 
 * Default handler for JAX-RPC, RMI, JAX-WS ...
 * EJB now has specialized support. 
 */
public aspect RemoteCallMonitor extends MvcFrameworkMonitor {

    private NameResolver nameResolver = new NullNameResolver();
    
    public RemoteCallMonitor() {
        super("Remote Service"); // should give an endpoint wherever possible with JNDI lookup, WebServiceContext, etc.
    }
    
    public pointcut remoteExecution() :
        within(Remote+) && execution(public * *(..) throws RemoteException);
    
    public pointcut endPoint() : 
        !within(javax.ejb.EJBObject+) && !within(javax.ejb.EJBHome+) && remoteExecution();

    protected pointcut monitorEnd() : endPoint();
    
    before(Remote remote) : endPoint() && this(remote) {        
        Response response = begin(getOperation(remote, thisJoinPointStaticPart.getSignature().getName()), 
                ServletRequestMonitor.JSP_PRIORITY);
        response.set(Response.PARAMETERS, thisJoinPoint.getArgs());
    }
    
    protected OperationDescription getOperation(Object remote, String methodName) {
        Object name = nameResolver.getName(remote);

        if (name == null) {
            name = remote.getClass().getName();
        }
        
        return operationFactory.makeRemoteOperation(controllerType, name.toString(), methodName); 
    }
        
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }

    public NameResolver getNameResolver() {
        return nameResolver;
    }

    public void setNameResolver(NameResolver nameResolver) {
        this.nameResolver = nameResolver;
    }
    
}
