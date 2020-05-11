/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;

public class DefaultResponse implements Response {

    private long startTime = Clock.UNDEFINED_TIME;
    private long endTime = Clock.UNDEFINED_TIME;
    private long duration = Clock.UNDEFINED_TIME;
    private Map context = new HashMap();
    
    private Serializable key;
    private transient ResponseFactory factory;
    private Response parent;
    
    private static final String LAYER = "response.layer";
    
    protected DefaultResponse(ResponseFactory factory, Serializable key) {
        this.factory = factory;
        this.key = key;
    }

    /* (non-Javadoc)
     * @see glassbox.response.Response#getKey()
     */
    public Serializable getKey() {
        return key;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#start()
     */
    public Response start() {
        setStart(getTime());
        return this;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#start()
     */
    public Response start(long time) {
        setStart(time);
        return this;
    }    
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#complete()
     */
    public Response complete() {
        setEnd(getTime());
        return this;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#complete(long)
     */
    public Response complete(long duration) {
        if (startTime == Clock.UNDEFINED_TIME) {
            endTime = getTime();
            startTime = endTime - duration;
        } else {
            endTime = startTime+duration;
        }
        this.duration = duration;
        return this;
    }
    /* (non-Javadoc)
     * @see glassbox.response.Response#update()
     */
    public Response update() {
        setDuration(getTime() - startTime);
        return this;
    }
    /* (non-Javadoc)
     * @see glassbox.response.Response#fail()
     */
    public Response fail() {
        set(Response.FAILURE_DATA, Boolean.TRUE);
        return complete();
    }

    public void setStart(long time) {
        startTime = time;
    }
    /* (non-Javadoc)
     * @see glassbox.response.Response#getStart()
     */
    public long getStart() {
        return startTime;
    }
    
    public void setEnd(long time) {
        endTime = time;
        duration = endTime - startTime;
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#getEnd()
     */
    public long getEnd() {
        return endTime;
    }
    
    protected void setDuration(long duration) {
        this.duration = duration;
    }
    /* (non-Javadoc)
     * @see glassbox.response.Response#getDuration()
     */
    public long getDuration() {
        return duration;
    }

    /* (non-Javadoc)
     * @see glassbox.response.Response#set(java.lang.String, java.lang.Object)
     */
    public void set(String key, Object value) {
        context.put(key, value);
    }

    /* (non-Javadoc)
     * @see glassbox.response.Response#getStatus()
     */
    public int getStatus() {
        if (startTime==Clock.UNDEFINED_TIME) {
            if (duration==Clock.UNDEFINED_TIME) {
                return NOT_STARTED;
            } else {
                return COMPLETED;
            }
        } else if (endTime==Clock.UNDEFINED_TIME) {
            return IN_PROGRESS;
        } else if (get(FAILURE_DATA) != null) {
            return FAILED;
        }
        return COMPLETED;
    }

    /* (non-Javadoc)
     * @see glassbox.response.Response#get(java.io.Serializable)
     */
    public Object get(Serializable key) {
        return context.get(key);
    }
    
    public void setLayer(Serializable layer) {
        context.put(LAYER, layer);
    }
    public Serializable getLayer() {
        return (Serializable)context.get(LAYER);
    }
    
    protected long getTime() { 
        return factory.getClock().getTime();
    }
    
    /* (non-Javadoc)
     * @see glassbox.response.Response#getFactory()
     */
    public ResponseFactory getFactory() {
        return factory;        
    }    

    public Serializable getApplication() {
        return getFactory().getApplication();
    }

    public void setParent(Response parent) {
        this.parent = parent;
    }

    public Response getParent() {
        return parent;
    }
    
    public String toString() {
        return "Response: "+key+", "+get(LAYER)+", "+TimeConversion.formatTime(startTime)+" to "+TimeConversion.formatTime(endTime);
    }
}
