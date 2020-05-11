/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * An operation may raise this exception if a client invokes the
 * operation but the passed context does not contain the context
 * values required by the operation.
 */
public class BAD_CONTEXT
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public BAD_CONTEXT()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public BAD_CONTEXT( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public BAD_CONTEXT( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public BAD_CONTEXT( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
