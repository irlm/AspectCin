/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

public class ConfigurationData implements Serializable {

	
	protected Integer id = null;
	
	protected Timestamp lastUsed = null;
	
    protected String status = " Not so good...at";

    public String getStatus() {
        return status + System.currentTimeMillis();
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Timestamp lastUsed) {
		this.lastUsed = lastUsed;
	}
       
        
}
