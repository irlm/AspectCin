/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

// define slow or failing?
public class DefaultRequest implements Request {
    private CallDescription aCall;

    private String requestString;
    private String description;
    private Object parameters;
    private long elapsedTime = Clock.UNDEFINED_TIME;
    private long lastTime = Clock.UNDEFINED_TIME;

    private static final String NO_PARAMETERS = "(no parameters)";
    private static final int MAX_STRING_LENGTH = 800;
    private static final int MAXPRINT = 40;
    private static final int MAX_PARAMETER_LENGTH = 80; // limits how much data we report for slow queries

    private static final long serialVersionUID = 1;
    
    public DefaultRequest(CallDescription aCall, String requestString, Object parameters, String description,
            long elapsedTime) {
        this(aCall, requestString, parameters);
        this.elapsedTime = elapsedTime;
        setDescription(description);
    }
    public DefaultRequest(CallDescription aCall, String requestString, Object parameters) {
        this.requestString = requestString;
        this.parameters = standardize(parameters);
        this.aCall = aCall;
    }
    public Request copy() {
        DefaultRequest result = new DefaultRequest(getCall(), getRequestString(), parameters);
        result.setDescription(getDescription());
        result.setElapsedTime(elapsedTime);
        result.setLastTime(lastTime);
        return result;
    }

    // public DefaultResponse(UsageTrackingInfo elapsedUsage, CallDescription aCall, String requestString, Object
    // parameters, String description) {
    // this(elapsedUsage, aCall, requestString, parameters, description, Clock.UNDEFINED_TIME);
    // }

    // public DefaultResponse(UsageTrackingInfo elapsedUsage, CallDescription aCall, Object parameters) {
    // this(elapsedUsage.getEventTime(), aCall, parameters);
    // }
    //    
    // public DefaultResponse(long elapsedTime, CallDescription aCall, Object parameters) {
    // this(aCall, parameters);
    // this.elapsedTime = elapsedTime;
    // }
    //    
    // public DefaultResponse(CallDescription aCall, Object parameters) {
    // this.parameters = parameters;
    // this.aCall = aCall;
    // }
    //
    public String getDescription() {
        if (description == null) {
            description = makeDefaultDescription();
        }
        return description;// + "; elapsed time = " + TimeConversion.convertNanosToMillis(elapsedTime) + " ms";
    }

    public void setDescription(String description) {
        this.description = trim(description, MAX_STRING_LENGTH);
    }

    public Object standardize(Object parameters) {
        if (parameters == null) {
            return null;
        } else if (parameters.getClass().getClassLoader()==null) {
            if (parameters instanceof Object[]) {
                return ((Object[])parameters).clone();
            } else {
                // could use reflection for any Cloneable
                return parameters;
            }
        } else if (parameters instanceof Map) {
            return new HashMap((Map)parameters);
        } else if (parameters instanceof List) {
            return new ArrayList((List) parameters);
        } else {
            throw new IllegalStateException("invalid parameters");
        }
    }
        
    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.api.Request#getParameterString()
     */
    public String getParameterString() {
        String paramDesc = "";
        if (parameters == null) {
            paramDesc = "";
        } else if (parameters instanceof String) {
            paramDesc = (String) parameters;
        } else if (parameters instanceof Map) {
            paramDesc = describeParameterMap((Map) parameters);
        } else if (parameters instanceof List) {
            paramDesc = describeParameterList((List) parameters);
        } else if (parameters instanceof Object[]) {
            paramDesc = describeParameterArray((Object[]) parameters);
        } else {
            throw new IllegalStateException("invalid parameters");
        }
        return trim(paramDesc, MAX_STRING_LENGTH);
    }

    protected String describeParameterMap(Map parameterMap) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator paramIt = parameterMap.entrySet().iterator(); paramIt.hasNext();) {
            Entry entry = (Entry) paramIt.next();
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(entry.getKey().toString());
            buffer.append("=");
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (i > 0) {
                    buffer.append(", ");
                }
                appendParam(buffer, value);
            }
        }
        if (buffer.length()==0) {
            return NO_PARAMETERS;
        }
        return buffer.toString();
    }

    protected String describeParameterList(List parameterList) {
        if (parameterList.isEmpty()) {
            return NO_PARAMETERS;
        }

        StringBuffer buffer = new StringBuffer();
        for (Iterator it = parameterList.iterator(); it.hasNext();) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }

            Object param = it.next();
            appendParam(buffer, param);
        }
        return buffer.toString();
    }
    
    protected String describeParameterArray(Object[] parameterArray) {
        if (parameterArray.length == 0) {
            return NO_PARAMETERS;
        }

        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<parameterArray.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }

            appendParam(buffer, parameterArray[i]);
        }
        return buffer.toString();
    }
    

    private void appendParam(StringBuffer buffer, Object param) {
        String paramStr = param == null ? "(null)" : param.toString();
        if (paramStr.length() > MAX_PARAMETER_LENGTH) {
            buffer.append(paramStr.substring(0, MAX_PARAMETER_LENGTH));
            buffer.append("...");
        } else if (paramStr.length() == 0) {
            buffer.append("(none)");
        } else {
            buffer.append(paramStr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.api.Request#getRequestString()
     */
    public String getRequestString() {
        return requestString;
    }

    /**
     * @return the elapsedTime
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * @param elapsedTime
     *            the elapsedTime to set
     */
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * @return the last time the request was updated (the end time if completed)
     */
    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    private String makeDefaultDescription() {
        StringBuffer desc = new StringBuffer();
        desc.append(requestString);
        String paramStr = getParameterString();
        if (paramStr != null && paramStr.length() > 0) {
            desc.append(", parameters =");
            desc.append(trim(paramStr, MAX_STRING_LENGTH));
        }
        return desc.toString();
    }

    public String toString() {
        String paramStr = trim(getParameterString(), MAXPRINT);
        return super.toString() + "request: " + requestString + "(" + TimeConversion.formatTime(elapsedTime) + ", "
                + paramStr + ")";
    }

    protected String trim(String str, int maxlen) {
        if (str == null) {
            str = "";
        } else if (str.length() > maxlen) {
            str = str.substring(0, maxlen - 3) + "...";
        }
        return str;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        DefaultRequest otherRequest = (DefaultRequest) other;
        long elapsedDelta = otherRequest.getElapsedTime() - getElapsedTime();
        // sort descending...
        if (elapsedDelta > 0) {
            return 1;
        } else if (elapsedDelta < 0) {
            return -1;
        } else {
            // we want a total ordering...
            long accumDelta = otherRequest.getLastTime() - getLastTime();
            if (accumDelta > 0) {
                return 1;
            } else if (accumDelta < 0) {
                return -1;
            } else {
                return makeDefaultDescription().compareTo(otherRequest.makeDefaultDescription());
            }
        }
    }

    /**
     * @return the call
     */
    public CallDescription getCall() {
        return aCall;
    }

    /**
     * @param call the call to set
     */
    public void setCall(CallDescription aCall) {
        this.aCall = aCall;
    }
    /* (non-Javadoc)
     * @see glassbox.track.api.Request#cloneParameters()
     */
    public void cloneParameters() {
        if (parameters instanceof Map) {
            parameters = Collections.unmodifiableMap(new HashMap((Map)parameters));             
        } else if (parameters instanceof List) {
            parameters = Collections.unmodifiableList(new ArrayList((List)parameters));
        } else if (parameters instanceof SortedSet) {
            parameters = Collections.unmodifiableSortedSet(new TreeSet((SortedSet)parameters));
        } else if (parameters instanceof Object[]) {
            parameters = shallowCopy((Object[])parameters);
        } else if (parameters instanceof Cloneable) {
            try {
                Method cloneMethod = parameters.getClass().getMethod("clone", null);
                parameters = cloneMethod.invoke(parameters, null);
            } catch (Exception e) {
                logWarn("Can't clone "+parameters.getClass()+": "+e.getMessage()+", "+e.getClass());
            }
        }
    }
    
    private Object[] shallowCopy(Object[] array) {
        Object[] copy = new Object[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);        
        return copy;
    }
    
    public boolean equals(Object otherObj) {
        if (!(otherObj instanceof DefaultRequest)) {
            return false;
        }
        
        return compareTo(otherObj)==0;
    }
        
    public int hashCode() {
        return ((int)(getElapsedTime() * 0x8f3a4 + getLastTime())) ^ makeDefaultDescription().hashCode();
    }
}
