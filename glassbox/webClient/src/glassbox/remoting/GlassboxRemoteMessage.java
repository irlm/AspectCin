/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.remoting;

import java.io.Serializable;

public class GlassboxRemoteMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String[] to = null;	
	protected String message = null;	
	protected String subject = null;
		
	public GlassboxRemoteMessage() {}
	
	public GlassboxRemoteMessage(String[] to, String subject, String message) {
		super();
		this.to = to;
		this.message = message;
		this.subject = subject;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String[] getTo() {
		return to;
	}
	
	public void setTo(String[] to) {
		this.to = to;
	}
}
