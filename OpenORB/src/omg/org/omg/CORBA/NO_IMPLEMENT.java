/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception indicates that even though the operation that was
 * invoked exists (it has an IDL definition), no implementation for
 * that operation exists. NO_IMPLEMENT can, for example, be raised
 * by an ORB if a client asks for an object?s type definition
 * from the interface repository, but no interface repository is
 * provided by the ORB.
 */
public class NO_IMPLEMENT
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public NO_IMPLEMENT()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public NO_IMPLEMENT( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public NO_IMPLEMENT( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public NO_IMPLEMENT( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
