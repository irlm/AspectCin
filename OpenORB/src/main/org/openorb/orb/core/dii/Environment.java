/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

/**
 * This class implements the OMG class : Environment
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
public class Environment
    extends org.omg.CORBA.Environment
{
    /**
     * Reference to the exception
     */
    private Exception m_exception;

    /**
     * Constructor
     */
    public Environment()
    {
        m_exception = null;
    }

    /**
     * Set the exception
     */
    public void exception( Exception except )
    {
        m_exception = except;
    }

    /**
     * Return the exception
     */
    public Exception exception()
    {
        return m_exception;
    }

    /**
     * Initialize the exception
     */
    public void clear()
    {
        m_exception = null;
    }
}

