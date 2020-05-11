/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

/**
 * This class is the implementation of the NVList OMG class. It manages a list of NamedValue.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:46:22 $
 * @see  org.omg.CORBA.NamedValue
 */
public class NVList
    extends org.omg.CORBA.NVList
{
    /**
     * NamedValue list
     */
    private java.util.Vector m_list;

    /**
     * Reference to the ORB
     */
    private org.omg.CORBA.ORB m_orb;

    /**
     * Constructor
     */
    public NVList( org.omg.CORBA.ORB orb )
    {
        m_list = new java.util.Vector();
        m_orb = orb;
    }

    /**
     * Return the nv list size
     */
    public int count()
    {
        return m_list.size();
    }

    /**
     * Add a named value into the list by supplying a flag.
     */
    public org.omg.CORBA.NamedValue add( int flags )
    {
        return add_value( "", m_orb.create_any(), flags );
    }

    /**
     * Add a named value into the list by supplying a flag and a name.
     */
    public org.omg.CORBA.NamedValue add_item( String item_name, int flags )
    {
        return add_value( item_name, m_orb.create_any(), flags );
    }

    /**
     * Add a named value into the list by supplying a flag, a name and a value.
     */
    public org.omg.CORBA.NamedValue add_value( String item_name, org.omg.CORBA.Any val, int flags )
    {
        m_list.addElement( new NamedValue( item_name, val, flags ) );

        return ( org.omg.CORBA.NamedValue ) ( m_list.lastElement() );
    }

    /**
     * Add a named value into the list
     */
    public void add( org.omg.CORBA.NamedValue nv )
    {
        m_list.addElement( nv );
    }

    /**
     * Return a nv list item
     */
    public org.omg.CORBA.NamedValue item( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds();
        }
        return ( NamedValue ) ( m_list.elementAt( index ) );
    }

    /**
     * Remove an item from the nv list
     */
    public void remove( int index ) throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds();
        }
        m_list.removeElementAt( index );
    }

    /**
     *  Insert a NamedValue to a specified index
     */
    public void insert( String item_name, org.omg.CORBA.Any val, int flags, int index )
          throws org.omg.CORBA.Bounds
    {
        if ( index > m_list.size() )
        {
            throw new org.omg.CORBA.Bounds();
        }
        m_list.insertElementAt( new NamedValue( item_name, val, flags ), index );
    }
}

