/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception typically indicates an administrative
 * mismatch. For example, a server may have made an attempt to
 * register itself with an implementation repository under a name
 * that is already in use, or is unknown to the
 * repository. OBJ_ADAPTER is also raised by the POA to indicate
 * problems with application-supplied servant managers.
 */
public class OBJ_ADAPTER
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public OBJ_ADAPTER()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public OBJ_ADAPTER( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public OBJ_ADAPTER( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public OBJ_ADAPTER( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
