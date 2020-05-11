/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.util.timing.Clock;

import java.io.Serializable;
import java.util.List;

public interface ResponseFactory {

    void setApplication(Serializable application);

    Serializable getApplication();

    Response getResponse(Serializable key);

    Response getLastResponse();

    Response startResponse(Serializable key);

    void setClock(Clock clock);

    Clock getClock();

    void addListener(ResponseListener listener);

    /** DI-friendly method to add listeners to a factory */
    void setListeners(List listeners);
    
    void push(Response response);
    
    boolean remove(Response response);
    
    public pointcut startedResponse(Response response) : execution(* start(..)) && this(response);
    public pointcut completedResponse(Response response) : execution(* complete(..)) && this(response);       
}
