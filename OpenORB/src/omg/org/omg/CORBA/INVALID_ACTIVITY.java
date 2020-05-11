/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The INVALID_ACTIVITY system exception may be raised on the Activity
 * or Transaction services  resume methods if a transaction or Activity
 * is resumed in a context different to that from which it was suspended.
 * It is also raised when an attempted invocation is made that is
 * incompatible with the Activity s current state.
 *
 * @since CORBA 3.0
 */
public class INVALID_ACTIVITY
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public INVALID_ACTIVITY()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public INVALID_ACTIVITY( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public INVALID_ACTIVITY( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public INVALID_ACTIVITY( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
