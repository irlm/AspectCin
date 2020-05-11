/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This exception indicates a persistent storage failure, for
 * example, failure to establish a database connection or corruption
 * of a database.
 */
public class PERSIST_STORE extends org.omg.CORBA.SystemException
{
   /**
    * Default constructor.
    */
    public PERSIST_STORE()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

   /**
    * Constructor with a string reason.
    * @param orb_reason the exception description
    */
    public PERSIST_STORE( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

   /**
    * Constructor with minor code and completion status.
    *
    * @param minor exception minor code (refer OMG PSS spec. 99-07-07,
    * Chapter 8 Minor Codes, p 67)
    * @param completed completed exception member
    */
    public PERSIST_STORE( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

   /**
    * Full constructor with fields initialization.
    *
    * @param orb_reason exception description
    * @param minor minor code exception (refer OMG PSS spec. 99-07-07,
    * Chapter 8 Minor Codes, p 67)
    * @param completed completed exception member
    */
    public PERSIST_STORE( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
