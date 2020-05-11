/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception is raised if an ORB cannot convert the
 * representation of data as marshaled into its native
 * representation or vice-versa. For example, DATA_CONVERSION can be
 * raised if wide character codeset conversion fails, or if an ORB
 * cannot convert floating point values between different
 * representations.
 */
public class DATA_CONVERSION
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public DATA_CONVERSION()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public DATA_CONVERSION( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public DATA_CONVERSION( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public DATA_CONVERSION( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
