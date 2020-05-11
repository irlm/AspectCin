/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * A request or reply from the network is structurally invalid. This
 * error typically indicates a bug in either the client-side or
 * server-side run time. For example, if a reply from the server
 * indicates that the message contains 1000 bytes, but the actual
 * message is shorter or longer than 1000 bytes, the ORB raises this
 * exception. MARSHAL can also be caused by using the DII or DSI
 * incorrectly, for example, if the type of the actual parameters
 * sent does not agree with IDL signature of an operation.
 */
public class MARSHAL
    extends org.omg.CORBA.SystemException
{
    /**
     * Default constructor
     */
    public MARSHAL()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with reason string
     */
    public MARSHAL( String orb_reason )
    {
        super( orb_reason, 0, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public MARSHAL( int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( null, minor, completed );
    }

    /**
     * Full constructor with fields initialization
     * @param minor minor exception member
     * @param completed completed exception member
     */
    public MARSHAL( String orb_reason, int minor, org.omg.CORBA.CompletionStatus completed )
    {
        super( orb_reason, minor, completed );
    }
}
