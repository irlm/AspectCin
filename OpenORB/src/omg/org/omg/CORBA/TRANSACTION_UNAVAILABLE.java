/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * TRANSACTION_UNAVAILABLE exception is raised by the ORB when it
 * cannot process a transaction service context because its
 * connection to the Transaction Service has been abnormally
 * terminated.
 *
 * @since  CORBA 3.0
 */
public class TRANSACTION_UNAVAILABLE
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public TRANSACTION_UNAVAILABLE()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public TRANSACTION_UNAVAILABLE( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_UNAVAILABLE( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_UNAVAILABLE( String orb_reason, int minor,
           org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
