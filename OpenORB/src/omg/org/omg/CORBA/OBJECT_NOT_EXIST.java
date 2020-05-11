/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * The OBJECT_NOT_EXIST exception is raised whenever an invocation
 * on a deleted object was performed. It is an authoritative
 * ?hard? fault report. Anyone receiving it is allowed (even
 * expected) to delete all copies of this object reference and to
 * perform other appropriate ?final recovery? style
 * procedures.  Bridges forward this exception to clients, also
 * destroying any records they may hold (for example, proxy objects
 * used in reference translation). The clients could in turn purge
 * any of their own data structures.
 */
public class OBJECT_NOT_EXIST
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public OBJECT_NOT_EXIST()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public OBJECT_NOT_EXIST( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public OBJECT_NOT_EXIST( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public OBJECT_NOT_EXIST( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
