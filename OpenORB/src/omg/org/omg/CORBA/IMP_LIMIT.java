/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception indicates that an implementation limit was
 * exceeded in the ORB run time. For example, an ORB may reach the
 * maximum number of references it can hold simultaneously in an
 * address space, the size of a parameter may have exceeded the
 * allowed maximum, or an ORB may impose a maximum on the number of
 * clients or servers that can run simultaneously.
 */
public class IMP_LIMIT
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public IMP_LIMIT()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public IMP_LIMIT( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public IMP_LIMIT( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public IMP_LIMIT( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
