/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import edu.emory.mathcs.util.WeakIdentityHashMap;
import glassbox.response.Response;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;

public aspect JndiMonitor implements NameResolver {

    transient private Map nameBindings = Collections.synchronizedMap(new WeakIdentityHashMap()); 
    
    public pointcut contextLookup(Object name) : within(Context+) && execution(* Context.lookup*(*)) && args(name);
    
    public pointcut contextList(Object name) : within(Context+) && execution(* Context.list*(*)) && args(name, ..);

    public pointcut dirSearch(Object name) : 
        within(DirContext+) && execution(NamingEnumeration DirContext.search(..)) && args(name, ..);
    
    public pointcut monitorPoint(Object name) : contextLookup(name) || contextList(name);
    
    after(Object name) returning (Object value) : contextLookup(name) {
        nameBindings.put(value, name);
    }
    
    public Object getName(Object object) {
        return nameBindings.get(object);
    }
    
    public Serializable getKey(Object name) {
        return "jndi://"+name.toString();
    }
    
    public String getLayer() {
        return Response.RESOURCE_NAMING;
    }

}
