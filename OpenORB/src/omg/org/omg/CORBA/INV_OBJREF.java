/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception indicates that an object reference is internally
 * malformed. For example, the repository ID may have incorrect
 * syntax or the addressing information may be invalid. This
 * exception is raised by <b>ORB::string_to_object</b> if the passed
 * string does not decode correctly.  An ORB may choose to detect
 * calls via nil references (but is not obliged to do detect
 * them). INV_OBJREF is used to indicate this.
 */
public class INV_OBJREF
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public INV_OBJREF()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public INV_OBJREF( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public INV_OBJREF( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public INV_OBJREF( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
