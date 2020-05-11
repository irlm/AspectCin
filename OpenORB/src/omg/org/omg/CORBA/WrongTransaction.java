/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Deprecated WrongTransaction exception class
 *
 * @deprecated Use the WRONG_TRANSACTION system exception
 */
public final class WrongTransaction extends org.omg.CORBA.SystemException
{
    /**
     * Default Constructor
     */
    public WrongTransaction()
    {
        // Removed to avoid a deprecated warning during compilation
        super( ""/*WrongTransactionHelper.id()*/, 0, CompletionStatus.COMPLETED_NO );
    }

    /**
     * This constructor precises a reason.
     *
     * A default minor code ( 0 ), and a default completion
     * status ( by default COMPLETED_NO ).
     *
     * @param reason exception reason.
     */
    public WrongTransaction( String reason )
    {
        super( reason, 0, CompletionStatus.COMPLETED_NO );
    }

    /**
     * This constructor is the most complete. It precises a reason, a minor code
     * and a completion status.
     *
     * @param reason  Exception reason
     * @param minor  Exception minor code
     * @param completed Exception completion status
     */
    public WrongTransaction( String reason, int minor, CompletionStatus completed )
    {
        super( reason, minor, completed );
    }
}
