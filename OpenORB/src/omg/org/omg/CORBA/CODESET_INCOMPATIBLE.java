/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception is raised whenever meaningful communication is not
 * possible between client and server native code sets.
 *
 * @since  CORBA 2.3
 */
public class CODESET_INCOMPATIBLE
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public CODESET_INCOMPATIBLE()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public CODESET_INCOMPATIBLE( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public CODESET_INCOMPATIBLE( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public CODESET_INCOMPATIBLE( String orb_reason, int minor,
          org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
