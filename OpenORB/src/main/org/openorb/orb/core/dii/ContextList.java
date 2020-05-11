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
 * This class implements the OMG class : ContextList.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 * @see  org.omg.CORBA.Context
 */
public class ContextList
    extends org.omg.CORBA.ContextList
{
    /**
     * Contexts m_list
     */
    private Vector m_list;

    /**
     * Constructor
     */
    public ContextList()
    {
        m_list = new Vector();
    }

    /**
     * Return number of contexts into the m_list
     */
    public int count()
    {
        return m_list.size();
    }

    /**
     * Add a context into the m_list
     */
    public void add( String ctx )
    {
        m_list.addElement( ctx );
    }

    /**
     * Return an item
     */
    public String item( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds();
        }
        return ( ( String ) ( m_list.elementAt( index ) ) );
    }

    /**
     * Remove an item
     */
    public void remove( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds();
        }
        m_list.removeElementAt( index );
    }
}

