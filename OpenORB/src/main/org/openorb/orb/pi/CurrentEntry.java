/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableInterceptor.CurrentOperations;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * Slot table for PICurrent.
 *
 * @author  Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/09/10 16:56:29 $
 */
class CurrentEntry implements CurrentOperations, Cloneable
{
    /** The ORB object used by this table. */
    private final ORB m_orb;

    /** The number of slots stored in this table. */
    private final int m_slots;

    /** The slot table. */
    private Any [] m_table;

    /** The invocation context object. */
    private Object m_invocation_ctx;


    /**
     * Constructor.
     */
    CurrentEntry( final int slots, final ORB orb )
    {
        m_slots = slots;
        m_orb = orb;
    }

    /**
     * Copy constructor.
     */
    CurrentEntry( final CurrentEntry from )
    {
        m_slots = from.m_slots;

        if ( from.m_table != null )
        {
            m_table = ( Any[] ) from.m_table.clone();
        }
        m_orb = from.m_orb;
    }

    /**
     * Return the slot data for the specified slot.
     *
     * @param id The slot id.
     * @return The data attached to the slot.
     */
    public Any get_slot( final int id ) throws InvalidSlot
    {
        if ( id < 0 || id >= m_slots )
        {
            throw new InvalidSlot();
        }
        if ( m_table == null )
        {
            m_table = new Any[ m_slots ];
        }
        if ( m_table[ id ] == null )
        {
            m_table[ id ] = m_orb.create_any();
            m_table[ id ].type ( m_orb.get_primitive_tc( TCKind.tk_null ) );
        }

        return m_table[ id ];
    }

    /**
     * Set the slot data of the specified slot.
     * @param id The slot id.
     * @param data The data to attach to the slot.
     */
    public void set_slot( final  int id, final Any data ) throws InvalidSlot
    {
        if ( id < 0 || id >= m_slots )
        {
            throw new InvalidSlot();
        }
        if ( m_table == null )
        {
            m_table = new Any[ m_slots ];
        }
        m_table[ id ] = data;
    }

    /**
     * Return the invocation context.
     * @return The invocation context object.
     */
    public Object get_invocation_ctx()
    {
        return m_invocation_ctx;
    }

    /**
     * Set the invocation context.
     * @param invocation_ctx The invocation context object.
     */
    public void set_invocation_ctx( final Object invocation_ctx )
    {
        m_invocation_ctx = invocation_ctx;
    }

    /**
     * Return the slot table size.
     * @return The size of the slot table.
     */
    public int get_table_size()
    {
        return m_slots;
    }

    /**
     * Return the slot table.
     * @return The slot table.
     */
    public Any[] get_table()
    {
        return m_table;
    }
}

