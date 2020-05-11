/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.apache.orb;

import org.apache.avalon.framework.CascadingThrowable;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;

/**
 * General exception thrown by the ORB implementation signalling
 * an internal error together with causal information.
 *
 * @author Stephen McConnell mcconnell@apache.org
 */
public class ORBRuntimeException
    extends SystemException
    implements CascadingThrowable
{

    private final Throwable m_cause;

    /**
     * Constructor for the ORBRuntimeException
     */
    public ORBRuntimeException()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = null;
    }

    /**
     * Constructor for the ORBRuntimeException
     * @param throwable the causal exception
     */
    public ORBRuntimeException( Throwable throwable )
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = throwable;
    }


    /**
     * Constructor for the ORBRuntimeException object
     * @param message exception message
     */
    public ORBRuntimeException( String message )
    {
        super( message, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = null;
    }


    /**
     * Constructor for the ORBRuntimeException object
     *
     * @param message exception message
     * @param throwable the causal exception
     */
    public ORBRuntimeException( String message, Throwable throwable )
    {
        super( message, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = throwable;
    }


    /**
     * Constructor for the ORBRuntimeException object
     * @param minor minor code
     * @param status completion status
     */
    public ORBRuntimeException( int minor, CompletionStatus status )
    {
        super( null, minor, status );
        m_cause = null;
    }


    /**
     * Constructor for the PersistenceException object
     *
     * @param minor minor code
     * @param status completion status
     * @param throwable causal exception
     */
    public ORBRuntimeException( int minor, CompletionStatus status,
                                Throwable throwable )
    {
        super( null, minor, status );
        m_cause = throwable;
    }


    /**
     * Constructor for the ORBRuntimeException object
     *
     * @param message exception message
     * @param minor minor code
     * @param status completion status
     */
    public ORBRuntimeException( String message, int minor,
                                CompletionStatus status )
    {
        super( message, minor, status );
        m_cause = null;
    }


    /**
     * Constructor for the ORBRuntimeException object
     * @param message exception message
     * @param minor minor code
     * @param status completion status
     * @param throwable causal exception
     */
    public ORBRuntimeException( String message, int minor,
                                CompletionStatus status,
                                Throwable throwable )
    {
        super( message, minor, status );
        m_cause = throwable;
    }


    /**
     * Gets the cause attribute of the ORBRuntimeException object
     * @return Throwable The cause value
     */
    public Throwable getCause()
    {
        return m_cause;
    }
}

