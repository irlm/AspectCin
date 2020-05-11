/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.omg.CORBA.portable.ServantObject;

import org.omg.PortableServer.Servant;

import org.openorb.orb.adapter.TargetInfo;

import org.openorb.util.ExceptionTool;

/**
 * The DispatchState class keeps information about the current state of a servant.
 * The information is the poa to which the servant is connected, the object id,
 * the servant itself, the aom entry for the servant, and the POACurrent.
 *
 * @author Michael Rumpf
 */
class DispatchState
    extends ServantObject
    implements TargetInfo
{
    private POA m_poa;
    private AOMEntry m_aom_entry;
    private Servant m_local_servant;
    private byte [] m_object_id;
    private CurrentImpl m_poa_current;

    private Object m_cookie;
    private String m_operation;
    private Thread m_work_thread;
    private boolean m_canceled;

    /**
     * Constructor.
     *
     * @param poa
     * @param aom_entry
     * @param poaCurrent
     */
    DispatchState( POA poa, AOMEntry aom_entry, CurrentImpl poaCurrent )
    {
        m_poa = poa;
        m_local_servant = null;
        m_object_id = aom_entry.getObjectId();
        m_aom_entry = aom_entry;
        m_poa_current = poaCurrent;

        // The member of org.omg.CORBA.portable.ServantObject
        servant = aom_entry.getServant();
    }

    /**
     * Constructor.
     *
     * @param poa
     * @param servant
     * @param object_id
     * @param poaCurrent
     */
    DispatchState( POA poa, java.lang.Object servant, byte [] object_id, CurrentImpl poaCurrent )
    {
        m_poa = poa;
        m_local_servant = null;
        m_object_id = object_id;
        m_poa_current = poaCurrent;

        // The member of org.omg.CORBA.portable.ServantObject
        this.servant = servant;
    }

    /**
     * Constructor.
     *
     * @param poa
     * @param servant
     * @param local_servant
     * @param object_id
     * @param poaCurrent
     */
    DispatchState( POA poa, java.lang.Object servant, Servant local_servant,
            byte [] object_id, CurrentImpl poaCurrent )
    {
        m_poa = poa;
        m_local_servant = local_servant;
        m_object_id = object_id;
        m_poa_current = poaCurrent;

        // The member of org.omg.CORBA.portable.ServantObject
        this.servant = servant;
    }

    /**
     * Get the repository id associated with the servant.
     *
     * @return The IDL repository id of the object the servant is incarnating.
     */
    public String getRepositoryID()
    {
        return getServant()._all_interfaces( m_poa, m_object_id ) [ 0 ];
    }

    /**
     * Return the id of the POA.
     *
     * @return The adapter id byte array.
     */
    public byte[] getAdapterID()
    {
        return m_poa.getAid();
    }

    /**
     * Return the object id.
     *
     * @return The object id byte array.
     */
    public byte[] getObjectID()
    {
        if ( m_aom_entry != null )
        {
            return m_aom_entry.getObjectId();
        }
        return m_object_id;
    }

    /**
     * Return the servant.
     *
     * @return Either the servant or the local servant (???).
     */
    public Servant getServant()
    {
        return ( Servant ) ( m_local_servant != null ? m_local_servant : servant );
    }

    /**
     * Return the cookie.
     *
     * @return The cookie object.
     */
    public Object getCookie()
    {
        return m_cookie;
    }

    /**
     * Set the cookie object.
     *
     * @param cookie The cookie object.
     */
    public void setCookie( Object cookie )
    {
        m_cookie = cookie;
    }

    /**
     * Return the operation name.
     *
     * @return The cookie object.
     */
    public String getOperation()
    {
        return m_operation;
    }

    /**
     * Set the operation name.
     *
     * @param operation The operation name.
     */
    public void setOperation( String operation )
    {
        m_operation = operation;
    }

    /**
     * Return the worker thread.
     *
     * @return The thread object.
     */
    public Thread getWorkThread()
    {
        return m_work_thread;
    }

    /**
     * Set the worker thread for this state.
     *
     * @param thread The thread object.
     */
    public void setWorkThread( Thread thread )
    {
        m_work_thread = thread;
    }

    /**
     * Return the POA.
     *
     * @return The POA instance.
     */
    public POA getPoa()
    {
        return m_poa;
    }

    /**
     * Return true if the operation has been canceled.
     *
     * @return True if the operation has been canceled, false otherwise.
     */
    public boolean isCanceled()
    {
        return m_canceled;
    }

    /**
     * Cancel the operation.
     */
    public void cancel()
    {
        m_canceled = true;
    }

    /**
     * Perform a _is_a operation on the servant.
     *
     * @param id A repository id of another object.
     * @return True if the servants incarnate the same IDL type, false otherwise.
     */
    public boolean targetIsA( String id )
    {
        m_poa_current.push( this );

        try
        {
            return getServant()._is_a( id );
        }
        finally
        {
            try
            {
                m_poa_current.pop();
            }
            catch ( final org.omg.PortableServer.CurrentPackage.NoContext ex )
            {
                m_poa.getLogger().error( "No Context available.", ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        "No Context available (" + ex + ")" ), ex );
            }
        }
    }
}

