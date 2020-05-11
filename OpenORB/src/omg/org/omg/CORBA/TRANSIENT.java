/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * TRANSIENT indicates that the ORB attempted to reach an object and
 * failed. It is not an indication that an object does not
 * exist. Instead, it simply means that no further determination of
 * an object?s status was possible because it could not be
 * reached. This exception is raised if an attempt to establish a
 * connection fails, for example, because the server or the
 * implementation repository is down.
 */
public class TRANSIENT
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public TRANSIENT()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public TRANSIENT( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSIENT( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSIENT( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
