/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.util.StackThreadLocal;
import glassbox.util.SimpleObserverProtocol;
import glassbox.util.timing.Clock;
import glassbox.util.timing.SimpleJavaClock;

import java.io.Serializable;
import java.util.*;

import javax.security.auth.Subject;

public class DefaultResponseFactory implements ResponseFactory {
    private ThreadLocal applicationHolder = new ThreadLocal();
    
    private Clock clock = new SimpleJavaClock();
    protected StackThreadLocal responseStack = new StackThreadLocal();
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#setApplication(java.io.Serializable)
     */
    public void setApplication(Serializable application) {
        applicationHolder.set(application);
    }
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#getApplication()
     */
    public Serializable getApplication() {
        return (Serializable)applicationHolder.get();        
    }
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#getResponse(java.io.Serializable)
     */
    public Response getResponse(Serializable key) {
        Response parent = getLastResponse();
        
        DefaultResponse response = new DefaultResponse(this, key);
        response.setParent(parent);
        return response;
    }
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#getLastResponse()
     */
    public Response getLastResponse() {
        return (Response)responseStack.peek();
    }

    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#startResponse(java.io.Serializable)
     */
    public Response startResponse(Serializable key) {
        Response response = getResponse(key);
        response.start();
        return response;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#setClock(glassbox.util.timing.Clock)
     */
    public void setClock(Clock clock) {
        this.clock = clock;        
    }
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#getClock()
     */
    public Clock getClock() {
        return clock;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#addListener(glassbox.response.ResponseListener)
     */
    public void addListener(ResponseListener listener) {
        addObserver(listener);        
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.ResponseFactory#setListeners(java.util.List)
     */ 
    public void setListeners(List listeners) {
        removeAllObservers();
        for (Iterator it=listeners.iterator(); it.hasNext();) {
            ResponseListener listener = (ResponseListener)it.next();
            addListener(listener);
        }
    }
    
    public void push(Response response) {
        responseStack.push(response);
    }

    public boolean remove(Response response) {
        return responseStack.remove(response);
    }
    
    private static aspect ResponseFactoryObserver extends SimpleObserverProtocol {
        declare parents: ResponseFactory implements Subject;
        declare parents: ResponseListener implements Observer;
    
        after(Response response) returning: startedResponse(response) {
            ResponseFactory factory = response.getFactory();
            Iterator iter = factory.getObservers().iterator();
            while ( iter.hasNext() ) {
                ResponseListener listener = (ResponseListener)iter.next();
                listener.startedResponse(response);
            }            
            
            factory.push(response);            
        }
        
        after(Response response) returning: completedResponse(response) {
            ResponseFactory factory = response.getFactory();
            
            // a missing response means that it was a summary recording: we didn't have it on the stack
            factory.remove(response);
            
            Iterator iter = factory.getObservers().iterator();
            while ( iter.hasNext() ) {
                ResponseListener listener = (ResponseListener)iter.next();
                listener.finishedResponse(response);
            }            
        }
        
        declare error: call(* ResponseFactory.push(..)) && !within(ResponseFactoryObserver) /*&& !within(TestCase+)*/:
            "don't call push or remove on response factory: only the observer should do this"; 
    }
}
