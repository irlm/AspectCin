/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception is raised if an operation implementation throws a
 * non-CORBA exception (such as an exception specific to the
 * implementation's programming language), or if an operation
 * raises a user exception that does not appear in the
 * operation's raises expression. UNKNOWN is also raised if the
 * server returns a system exception that is unknown to the
 * client. (This can happen if the server uses a later version of
 * CORBA than the client and new system exceptions have been added
 * to the later version.)
 */
public class UNKNOWN
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public UNKNOWN()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public UNKNOWN( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public UNKNOWN( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public UNKNOWN( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
