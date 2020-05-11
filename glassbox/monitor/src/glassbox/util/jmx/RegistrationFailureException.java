/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.jmx;

public class RegistrationFailureException extends RuntimeException {

    public RegistrationFailureException(String msg, Throwable t) {
        super(msg, t);
    }

    public RegistrationFailureException(String msg) {
        super(msg);
    }
    
    private static final long serialVersionUID = 1;
}
