/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitoredType;
import glassbox.response.Response;

import java.io.Serializable;
import java.lang.reflect.Method;

public aspect CommonsHttpMonitor extends AbstractMonitor {

    public static interface IURI extends MonitoredType {
        String getScheme();
        String getHost();
        String getName();
        int getPort();
    }
    declare parents: org.apache.commons.httpclient.URI implements IURI;
    
    protected final String URI_METHOD_NAME = "getURI";
    protected final String NAME_METHOD_NAME= "getName";
    
    protected pointcut executeOnHttp(Object httpMethod) :
        within(org.apache.commons.httpclient.HttpMethod+) && execution(* org.apache.commons.httpclient.HttpMethod.execute(..)) && target(httpMethod);

    protected pointcut executeOnMethod() :
        within(org.apache.commons.httpclient.HttpClient+) && execution(* org.apache.commons.httpclient.HttpClient.executeMethod(..));
        
    protected pointcut topLevelExecuteOnMethod() :
        executeOnMethod() && !cflowbelow(executeOnMethod());
        
    protected pointcut executeOnMethod1(Object httpMethod) :
        topLevelExecuteOnMethod() && args(httpMethod, ..) && !args(org.apache.commons.httpclient.HostConfiguration, ..);

    protected pointcut executeOnMethod2(Object httpMethod) :
        topLevelExecuteOnMethod() && args(org.apache.commons.httpclient.HostConfiguration, httpMethod, ..);
  
    before(Object httpMethod) : executeOnHttp(httpMethod) {
        begin(getKey(httpMethod));
    }

    before(Object httpMethod) : executeOnMethod1(httpMethod) {
        begin(getKey(httpMethod));
    }

    before(Object httpMethod) : executeOnMethod2(httpMethod) {
        begin(getKey(httpMethod));
    }
    
    protected pointcut monitorEnd() : executeOnMethod() || executeOnHttp(*);

    protected Serializable getKey(Object httpMethod) {
        String hostName = null;
        String name = null;
        String protocol = null;
        int port = -1;
        try {
            Method getUriMethod = httpMethod.getClass().getMethod(URI_METHOD_NAME, null);
            if(getUriMethod != null) {
                IURI uri = (IURI)getUriMethod.invoke(httpMethod, null);
                protocol = uri.getScheme();  
                hostName = uri.getHost();
                port = uri.getPort();
            }
            Method getNameMethod = httpMethod.getClass().getMethod(NAME_METHOD_NAME, null);
            if(getNameMethod != null) {
               name = (String)getNameMethod.invoke(httpMethod, null);
            }
        } catch (Exception e) {
            hostName = "Undefined URI";
            name = "Undefined Name";
            protocol = "HTTP";
            logError("Problem in finding information for HTTP request on "+httpMethod, e);
        }
        int defaultPort = ("https".compareToIgnoreCase(protocol)==0? 443 : 80) ;
        String key = protocol.toUpperCase() + " " + name + "://"+ hostName;
        if (port>0 && defaultPort != port) {
            key += ":" + port;
        }
        return key;
    }
    
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }    
}
