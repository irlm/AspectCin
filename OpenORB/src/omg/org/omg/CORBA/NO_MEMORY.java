/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The ORB run time has run out of memory.
 */
public class NO_MEMORY
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public NO_MEMORY()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public NO_MEMORY( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public NO_MEMORY( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public NO_MEMORY( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
