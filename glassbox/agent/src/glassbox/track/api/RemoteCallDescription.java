/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class RemoteCallDescription implements CallDescription {
    private String fullKey;
    private String callKey;
    private String resKey;
    public static final String ROOT_KEY = "/";
    private static final long serialVersionUID = 1L;
    
    public RemoteCallDescription(String resKey, String callKey, String fullKey) {
        this.resKey = resKey;
        this.callKey = callKey;
        this.fullKey = fullKey;
    }
    
    /**
     * 
     * @param key must be an OperationDescriptionImpl or an http or rmi URL String
     * We don't yet handle JNDI or FILE URL's 
     */
    public RemoteCallDescription(Object key) {
        if (key instanceof OperationDescription) {
            OperationDescription operation = (OperationDescription)key; 
            resKey = operation.getOperationType();
            callKey = operation.getOperationName();
            fullKey = resKey+":"+callKey;
        } else if (key instanceof RemoteCallDescription) {
            RemoteCallDescription rcd = (RemoteCallDescription)key;
            resKey = rcd.resKey;
            callKey = rcd.callKey;
            fullKey = rcd.fullKey;            
        } else if (key instanceof String) {            
            fullKey = (String)key;
            int protocol = fullKey.indexOf(':');
            int pos = -1;
            if (protocol != -1) {
                pos = protocol+1;
                while (pos<fullKey.length() && fullKey.charAt(pos)=='/') {
                    pos++;
                }
                int epos = fullKey.indexOf('/', pos);
                if (epos == -1) {
                    // special-case logic for rmi... 
                    if (fullKey.indexOf("rmi")!=0) {
                        pos = fullKey.length();
                    } else {
                        pos = protocol;
                    }
                } else {
                    pos = epos;
                }
            }
            if (pos == -1) {
                resKey = "unknown";
                callKey = fullKey;
            } else if (pos >= fullKey.length()-1) {                
                resKey = fullKey.substring(0, pos);
                callKey = ROOT_KEY;
            } else {
                resKey = fullKey.substring(0, pos);
                callKey = fullKey.substring(pos+1);
            }
            while (callKey.length()>1 && (callKey.charAt(0)=='/' || callKey.charAt(0)==':')) {
                callKey = callKey.substring(1);
            }
        } else {
            throw new IllegalArgumentException("Invalid key for remote call "+key+(key==null ? "" : ", class = "+key.getClass().getName()));
        }
    }
    
    public int callType() {
        return CallDescription.REMOTE_CALL;
    }

    public String getSummary() {
        return fullKey;
    }

    public Serializable getCallKey() {
        return callKey;
    }

    public Serializable getResourceKey() {
        return resKey;
    }

}
