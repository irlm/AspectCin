/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

import java.util.Vector;

/**
 * This class implements the OMG class : ExceptionList. It is used
 * when a client wants to make a remote call in order to specify what
 * kind of exception could be raised by the remote application. The
 * exception list does not contain exceptions byt exceptions
 * typecodes.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:47 $
 */
public class ExceptionList
    extends org.omg.CORBA.ExceptionList
{
    /**
     * Exceptions list
     */
    private Vector m_list;

    /**
     * Constructor
     */
    public ExceptionList()
    {
        m_list = new Vector();
    }

    /**
     * Return the list size
     */
    public int count()
    {
        return m_list.size();
    }

    /**
     * Add an item into the list
     */
    public void add( org.omg.CORBA.TypeCode exc )
    {
        m_list.addElement( exc );
    }

    /**
     * Return an item
     */
    public org.omg.CORBA.TypeCode item( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds( "ExceptionList index out of bounds" );
        }
        return ( org.omg.CORBA.TypeCode ) ( m_list.elementAt( index ) );
    }

    /**
     * Remove an item
     */
    public void remove( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds( "ExceptionList index out of bounds" );
        }
        m_list.removeElementAt( index );
    }
}

