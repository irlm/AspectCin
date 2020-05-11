/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The TRANSACTION_REQUIRED exception indicates that the request
 * carried a null transaction context, but an active transaction is
 * required.
 */
public class TRANSACTION_REQUIRED
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public TRANSACTION_REQUIRED()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public TRANSACTION_REQUIRED( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_REQUIRED( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_REQUIRED( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
