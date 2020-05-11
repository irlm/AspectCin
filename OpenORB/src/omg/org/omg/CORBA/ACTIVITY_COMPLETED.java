/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The ACTIVITY_COMPLETED system exception may be raised on any method for
 * which Activity context is accessed. It indicates that the Activity
 * context in which the method call was made has been completed due to a
 * timeout of either the Activity itself or a transaction that encompasses
 * the Activity, or that the Activity completed in a manner other than that
 * originally requested.
 *
 * @since CORBA 3.0
 */
public class ACTIVITY_COMPLETED
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public ACTIVITY_COMPLETED()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public ACTIVITY_COMPLETED( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public ACTIVITY_COMPLETED( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public ACTIVITY_COMPLETED( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
