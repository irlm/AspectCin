/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * REBIND is raised when the current effective RebindPolicy has a
 * value of NO_REBIND or NO_RECONNECT and an invocation on a bound
 * object reference results in a LocateReply message with status
 * OBJECT_FORWARD or a Reply message with status
 * LOCATION_FORWARD. This exception is also raised if the current
 * effective RebindPolicy has a value of NO_RECONNECT and a
 * connection must be re-opened. The invocation can be retried once
 * the effective RebindPolicy is changed to TRANSPARENT or binding
 * is re-established through an invocation of
 * CORBA::Object::validate_connection.
 *
 * @since  CORBA 3.0
 */
public class REBIND
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public REBIND()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public REBIND( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public REBIND( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public REBIND( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
