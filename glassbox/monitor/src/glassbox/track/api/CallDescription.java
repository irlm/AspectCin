/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;

/**
 * Describes a single type of call, e.g., queries with this prepared statement. 
 * Does not include the parameters.
 * Often used with CallList to describe instances of problematic calls.   
 * 
 * @author Ron Bodkin
 *
 */
public interface CallDescription {

    public static final int DATABASE_STATEMENT = 1;
    public static final int DATABASE_CONNECTION = 2;
    public static final int REMOTE_CALL = 3;
    public static final int OPERATION_PROCESSING = 4;
    public static final int DISPATCH = 5;

    Serializable getCallKey();

    String getSummary();

    /**
     * 
     * @return type of call, e.g., DATABASE_STATEMENT or DATABASE_CONNECTION
     */
    int callType();

    Serializable getResourceKey();

}