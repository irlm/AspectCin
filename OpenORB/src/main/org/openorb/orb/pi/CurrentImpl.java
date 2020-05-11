/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import java.util.Map;
import java.util.WeakHashMap;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentOperations;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * Implementation of PICurrent.
 *
 * @author  Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/09/10 16:56:08 $
 */
public class CurrentImpl extends LocalObject implements Current
{
    private final ORB m_orb;

    private ThreadLocal m_curr = new ThreadLocal();
    private Map m_stored_ctxts;
    private int m_slots;
    private boolean m_slots_set;

    /**
     * Creates new CurrentImpl.
     */
    CurrentImpl( final ORB orb )
    {
        m_orb = orb;
    }

    /**
     * Called at the end of the post init phase. Sets the slot count.
     */
    void set_slots( final int slots )
    {
        if ( m_slots_set )
        {
            throw new BAD_INV_ORDER();
        }
        // create a new current stack. The old one's slot tables will be the
        // wrong size.
        m_curr = new ThreadLocal();

        m_slots_set = true;

        m_slots = slots;
    }

    /**
     * Create a new slot table for use on server side.
     */
    public CurrentOperations create()
    {
        return new CurrentEntry( m_slots, m_orb );
    }

    /**
     * Copy a slot table.
     */
    public CurrentOperations copy( final CurrentOperations from )
    {
        if ( from == null )
        {
            return null;
        }
        if ( !( from instanceof CurrentEntry ) )
        {
            throw new IllegalArgumentException();
        }
        final CurrentEntry fr = ( CurrentEntry ) from;

        if ( fr.get_table() == null )
        {
            return null;
        }
        return new CurrentEntry( fr );
    }

    /**
     * Set a previously created or retrieved table.
     */
    public void set( final CurrentOperations value )
    {
        if ( value != null && !( value instanceof CurrentEntry ) )
        {
            throw new IllegalArgumentException();
        }
        m_curr.set( value );
    }

    /**
     * Remove the current table.
     */
    public CurrentOperations remove()
    {
        final CurrentEntry ret = ( CurrentEntry ) m_curr.get();

        if ( ret != null )
        {
            m_curr.set( null );
        }
        return ret;
    }

    /**
     * get the current table.
     */
    public CurrentOperations get()
    {
        return ( CurrentOperations ) m_curr.get();
    }

    public void set_slot( final int id, final Any data ) throws InvalidSlot
    {
        get_p().set_slot( id, data );
    }

    public Any get_slot( final int id ) throws InvalidSlot
    {
        return get_p().get_slot( id );
    }

    public Object get_invocation_ctx()
    {
        final CurrentEntry ret = ( CurrentEntry ) m_curr.get();

        if ( ret == null )
        {
            return null;
        }
        return ret.get_invocation_ctx();
    }

    public void set_invocation_ctx( final Object invocation_ctx )
    {
        get_p().set_invocation_ctx( invocation_ctx );
    }

    public void store_invocation_ctx( final Object handle )
    {
        synchronized ( this )
        {
            if ( m_stored_ctxts == null )
            {
                m_stored_ctxts = new WeakHashMap();
            }

            final CurrentEntry curr = get_p();

            m_stored_ctxts.put( handle, curr.get_invocation_ctx() );
        }
    }

    public Object retrieve_invocation_ctx( final Object handle, final boolean delete )
    {
        synchronized ( this )
        {
            if ( m_stored_ctxts == null )
            {
                return null;
            }
            if ( delete )
            {
                return m_stored_ctxts.remove( handle );
            }
            else
            {
                return m_stored_ctxts.get( handle );
            }
        }
    }

    private CurrentEntry get_p()
    {
        CurrentEntry ret = ( CurrentEntry ) m_curr.get();

        if ( ret == null )
        {
            ret = new CurrentEntry( m_slots, m_orb );
            m_curr.set( ret );
        }

        return ret;
    }

    /**
     * Replace LocalObject _orb() method for get orb in server/client
     * manager.
     */
    public ORB _orb()
    {
        return m_orb;
    }
}

