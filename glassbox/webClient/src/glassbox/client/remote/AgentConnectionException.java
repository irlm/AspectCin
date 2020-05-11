/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import org.springframework.core.NestedRuntimeException;

public class AgentConnectionException extends NestedRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

    private String URL = null;
    
    public AgentConnectionException(String message) {
        super(message);
    }
    
    public AgentConnectionException(String message, Throwable cause, String URL) {
        super(message, cause);
        this.URL = URL;
    }
    
	public AgentConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        URL = url;
    }

}
