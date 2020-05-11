/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * A parameter passed to a call is out of range or otherwise
 * considered illegal. An ORB may raise this exception if null
 * values or null pointers are passed to an operation (for language
 * mappings where the concept of a null pointers or null values
 * applies).  BAD_PARAM can also be raised as a result of client
 * generating requests with incorrect parameters using the DII.
 */
public class BAD_PARAM
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public BAD_PARAM()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public BAD_PARAM( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public BAD_PARAM( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public BAD_PARAM( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
