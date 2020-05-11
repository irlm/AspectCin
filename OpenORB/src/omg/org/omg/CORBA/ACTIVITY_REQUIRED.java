/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The ACTIVITY_REQUIRED system exception may be raised on any method for
 * which an Activity context is required. It indicates that an Activity context
 * was necessary to perform the invoked operation, but one was not found
 * associated with the calling thread.
 *
 * @since CORBA 3.0
 */
public class ACTIVITY_REQUIRED
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public ACTIVITY_REQUIRED()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public ACTIVITY_REQUIRED( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public ACTIVITY_REQUIRED( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public ACTIVITY_REQUIRED( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
