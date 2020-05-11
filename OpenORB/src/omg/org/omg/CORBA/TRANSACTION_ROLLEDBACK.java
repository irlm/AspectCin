/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The TRANSACTION_ROLLEDBACK exception indicates that the
 * transaction associated with the request has already been rolled
 * back or marked to roll back. Thus, the requested operation either
 * could not be performed or was not performed because further
 * computation on behalf of the transaction would be fruitless.
 */
public class TRANSACTION_ROLLEDBACK
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public TRANSACTION_ROLLEDBACK()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public TRANSACTION_ROLLEDBACK( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_ROLLEDBACK( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public TRANSACTION_ROLLEDBACK( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
