/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;

import org.omg.CORBA.ORBPackage.InvalidName;

import org.omg.PortableServer.CurrentPackage.NoContext;

import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import org.openorb.orb.core.MinorCodes;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;
import org.openorb.util.JREVersion;
import org.openorb.util.RepoIDHelper;

/**
 * This is the implementation of the Delegate interface.
 * It provides the methods of the PortableServer Delegate interface.
 */
class DelegateImpl
    implements org.omg.PortableServer.portable.Delegate
{
    /** The delegate's ORB instance. */
    private ORB         m_orb;

    /** The POA's static thread table. */
    private CurrentImpl m_curr;

    /** The RootPOA instance. */
    private POA         m_rootPOA;

    /** The logger instance of this class. */
    private Logger      m_logger;

    /**
     * Constructor.
     * @param orb The ORB instance.
     */
    public DelegateImpl( ORB orb )
    {
        this.m_orb = orb;
    }

    /**
     * Initialize the instance.
     */
    public void init()
    {
        try
        {
            m_curr = ( CurrentImpl )
                    m_orb.resolve_initial_references( "POACurrent" );
            m_rootPOA = ( POA )
                    m_orb.resolve_initial_references( "RootPOA" );
        }
        catch ( final InvalidName ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug( "Unable to initialize POA delegate.", ex );
            }
            throw ExceptionTool.initCause( new INITIALIZE(
                    "Unable to initialize POA delegate (" + ex + ")" ), ex );
        }
        catch ( final ClassCastException ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug( "Unable to initialize POA delegate.", ex );
            }
            throw ExceptionTool.initCause( new INITIALIZE(
                    "Unable to initialize POA delegate (" + ex + ")" ), ex );
        }
    }

    /**
     * Return the ORB instance.
     * @param self The servant for which to return the ORB (not used here).
     */
    public ORB orb( Servant self )
    {
        return m_orb;
    }

    /**
     * Return an object reference for the servant. This method implicitly
     * activates the object.
     * @param self The servant to create an object reference for.
     * @return An object reference for the specified servant.
     */
    public org.omg.CORBA.Object this_object( Servant self )
    {
        POA poa = null;
        byte [] object_id = null;
        try
        {
            DispatchState state = m_curr.peek();
            if ( state.getServant() == self )
            {
                poa = state.getPoa();
                object_id = state.getObjectID();
            }
        }
        catch ( NoContext ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug( "No context available.", ex );
            }
        }

        if ( poa == null )
        {
            // we are outside the context of a request. Try and find the ID
            poa = self._default_POA();
            if ( poa == null )
            {
                throw new OBJ_ADAPTER();
            }
            // this will only work for the appropriate policies.
            try
            {
                object_id = poa.servant_to_id( self );
            }
            catch ( final ServantNotActive ex )
            {
                if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                      && Trace.isMedium() )
                {
                    getLogger().debug( "The servant is not active yet.", ex );
                }
                throw ExceptionTool.initCause( new OBJ_ADAPTER(), ex );
            }
            catch ( final WrongPolicy ex )
            {
                if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                      && Trace.isMedium() )
                {
                    getLogger().debug(
                            "The servant has a wrong policy.", ex );
                }
                throw ExceptionTool.initCause( new OBJ_ADAPTER(), ex );
            }
        }

        // create a new reference. All the inappropriate poas have been
        // filtered out in the above. If in the context of a request then any
        // policy set is valid.
        String [] ids = self._all_interfaces( poa, object_id );
        return poa.create_reference_with_id( object_id, ids[ 0 ] );
    }

    /**
     * Return the POA with which this servant is registered.
     * @param self The servant for which to return the POA.
     * @return The POA associated with the servant.
     */
    public POA poa( Servant self )
    {
        try
        {
            DispatchState state = m_curr.peek();
            if ( state.getServant() == self )
            {
                return state.getPoa();
            }
            Trace.signalIllegalCondition( getLogger(),
                    "No Context, i.e. state.getServant() != self" );
        }
        catch ( NoContext ex )
        {
            // This exception is thrown whenever a deactivated servant tries
            // to access the POA with which it was registered before.
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug(
                        "No context available for servant " + self, ex );
            }
        }
        throw new OBJ_ADAPTER( "No Context" );
    }

    /**
     * Return the object_id assigned to the servant.
     *
     * @param self The object id of the specified servant.
     */
    public byte[] object_id( Servant self )
    {
        try
        {
            DispatchState state = m_curr.peek();
            if ( state.getServant() == self )
            {
                return state.getObjectID();
            }
            Trace.signalIllegalCondition(
                getLogger(), "No Context, i.e. state.getServant() != self" );
        }
        catch ( NoContext ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug( "No context available.", ex );
            }
        }
        throw new OBJ_ADAPTER( "No Context" );
    }

    /**
     * Return the default POA of the servant, i.e. the RootPOA.
     *
     * @param self The default POA of the specified servant.
     */
    public POA default_POA( Servant self )
    {
        return m_rootPOA;
    }

    /**
     * Check whether the servant is of the given type.
     * @param self The servant to check.
     * @param id The type id against which the servant's type id is checked.
     */
    public boolean is_a( Servant self, String id )
    {
        // compare to Object
        Object test = RepoIDHelper.createIsATest( id );
        if ( test.equals( "IDL:omg.org/CORBA/Object:1.0" ) )
        {
            return true;
        }
        // compare IDs
        String [] ids =
            self._all_interfaces( poa( self ), object_id( self ) );
        for ( int i = 0; i < ids.length; ++i )
        {
            if ( test.equals( ids[ i ] ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the servant exists.
     * @param self The servant to check for existence.
     * @return false, as in this case the servant always exists.
     */
    public boolean non_existent( Servant self )
    {
        return false;
    }

    /**
     * Retrieve an interface description for this servant.
     * This method calls get_interface_def() and narrows the returned
     * object to the type InterfaceDef.
     * @param self The servant for which to retrieve an interface description.
     * @return The interface description of the servant.
     */
    public InterfaceDef get_interface( Servant self )
    {
        try
        {
            return InterfaceDefHelper.narrow( get_interface_def( self ) );
        }
        catch ( final BAD_PARAM ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug(
                        "Could not narrow obj to type InterfaceDef.", ex );
            }
            throw ExceptionTool.initCause( new INTF_REPOS(
                    MinorCodes.INF_REPOS_TYPE,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
    }

    /**
     * Retrieve an interface description for this servant as CORBA Object.
     * @param self The servant for which to retrieve an interface description.
     * @return The interface description of the servant as CORBA Object.
     */
    public org.omg.CORBA.Object get_interface_def( Servant self )
    {
        String repository_id =
            self._all_interfaces( poa( self ), object_id( self ) ) [ 0 ];
        org.omg.CORBA.Object obj;
        try
        {
            obj = m_orb.resolve_initial_references( "InterfaceRepository" );
        }
        catch ( final InvalidName ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug(
                        "Could not resolve InterfaceRepository.", ex );
            }
            throw ExceptionTool.initCause( new INTF_REPOS(
                    MinorCodes.INF_REPOS_FIND,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        if ( obj._non_existent() )
        {
            throw new INTF_REPOS( MinorCodes.INF_REPOS_FIND,
                    CompletionStatus.COMPLETED_NO );
        }
        Repository rep;
        try
        {
            rep = RepositoryHelper.narrow( obj );
        }
        catch ( final BAD_PARAM ex )
        {
            if ( !JREVersion.V1_4 && getLogger().isDebugEnabled()
                  && Trace.isMedium() )
            {
                getLogger().debug(
                        "Could not narrow obj to type Repository.", ex );
            }
            throw ExceptionTool.initCause( new INTF_REPOS(
                    MinorCodes.INF_REPOS_FIND,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        org.omg.CORBA.Object interface_def = rep.lookup_id( repository_id );
        if ( interface_def == null )
        {
            throw new INTF_REPOS( MinorCodes.INF_REPOS_LOOKUP,
                    CompletionStatus.COMPLETED_NO );
        }
        return interface_def;
    }

    /**
     * Return the logger of this class.
     */
    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton )
                    m_orb ).getLogger();
        }
        return m_logger;
    }
}

