/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util;

public class ConfigurationException extends RuntimeException {

	public ConfigurationException() {
	}

	public ConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConfigurationException(String arg0) {
		super(arg0);
	}

	public ConfigurationException(Throwable arg0) {
		super(arg0);
	}

    private static final long serialVersionUID = 1L;
}
