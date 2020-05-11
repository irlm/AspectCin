/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.Interceptor;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;

import org.openorb.orb.config.ORBLoader;

import org.openorb.util.ExceptionTool;

/**
 * This class implements the "ORBInitInfo" interface for
 * PortableInterceptors.
 *
 * @author  Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/10 21:46:23 $
 */
public class OpenORBInitInfo
    extends org.openorb.orb.core.LoggableLocalObject
    implements org.omg.PortableInterceptor.ORBInitInfo,
               org.openorb.orb.pi.FeatureInitInfo,
               org.openorb.orb.pi.ORBInitInfo, Disposable
{
    private org.openorb.orb.core.ORB m_orb;

    private Logger m_logger;

    /**
     * The ORB arguments
     */
    private String [] m_args;

    private ORBInitializer [] m_orb_inits;

    private FeatureInitializer [] m_feature_inits;

    private ORBLoader m_loader;

    // dynamic state

    private int m_slots = 0;

    private ArrayList m_client_interceptors = new ArrayList();

    private ArrayList m_server_interceptors = new ArrayList();

    private ArrayList m_ior_interceptors = new ArrayList();

    private CodecFactoryManager m_codec_manager;

    private int m_phase = PHASE_CREATED;

    private static final int PHASE_CREATED = 0;
    private static final int PHASE_PRE_INIT = 1;
    private static final int PHASE_POST_INIT = 3;
    private static final int PHASE_COMPLETE = 4;

    //========================================================================
    // constructor
    //========================================================================

    /**
     * Set information
     */
    public OpenORBInitInfo( String [] args, org.openorb.orb.core.ORB orb,
        ORBInitializer [] orbInits, FeatureInitializer [] featureInits )
    {
        m_args = args;
        m_orb = orb;
        m_orb_inits = orbInits;
        m_feature_inits = featureInits;

        m_logger = m_orb.getLogger();

        try
        {
            m_loader = ( ORBLoader ) m_orb.getFeature( "ORBLoader" );
        }
        catch ( Exception ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Couldn't get ORBLoader feature.", ex );
            }
            throw new CascadingRuntimeException(
                "Could not resolve ORBLoader.", ex );
        }
    }

    //========================================================================
    // ORBInitInfo
    //========================================================================

    /**
     * Call pre-init on the orb initializers
     */
    public void pre_init()
    {
        if ( m_phase != PHASE_CREATED )
        {
            throw new org.omg.CORBA.INITIALIZE(
                "Call to pre_init not allowed at this time" );
        }
        m_phase = PHASE_PRE_INIT;

        // load codec factory / codec factory manager
        m_codec_manager = new CodecFactoryManagerImpl();

        m_orb.addInitialReference( "CodecFactory", ( org.omg.CORBA.Object )
            m_codec_manager );

        m_orb.setFeature( "CodecFactoryManager", m_codec_manager );

        // call pre_inits
        if ( m_orb_inits != null )
        {
            for ( int i = 0; i < m_orb_inits.length; i++ )
            {
                m_orb_inits[ i ].pre_init( this );
            }
        }
    }

    /**
     * Calls init on feature initializers and post init on orb initializers.
     * During the post_init phase, calls can be made on references returned
     * from resolve_initial_reference, however client interceptors will not
     * be used, the PICurrent will not have any active slots, and IORs cannot
     * be constructed.
     */
    public void post_init()
    {
        if ( m_phase != PHASE_PRE_INIT )
        {
            throw new org.omg.CORBA.INITIALIZE(
                "Call to post_init not allowed at this time" );
        }
        m_phase = PHASE_POST_INIT;

        // call feature inits
        if ( m_feature_inits != null )
        {
            for ( int i = 0; i < m_feature_inits.length; ++i )
            {
                m_feature_inits[ i ].init( this, this );
            }
        }
        CurrentImpl pi_current = new CurrentImpl( m_orb );

        m_orb.addInitialReference( "PICurrent", pi_current );

        m_orb.setFeature( "PICurrent", pi_current );

        if ( m_orb_inits != null )
        {
            for ( int i = 0; i < m_orb_inits.length; i++ )
            {
                m_orb_inits[ i ].post_init( this );
            }
        }
        // complete the initialization process.
        pi_current.set_slots( m_slots );


        create_client_interceptor_manager( pi_current );

        create_server_interceptor_manager( pi_current );

        create_ior_interceptor_manager();

        m_phase = PHASE_COMPLETE;
    }

    private void create_client_interceptor_manager( CurrentImpl pi_current )
    {
        if ( !m_loader.getBooleanProperty( "openorb.client.enable", true ) )
        {
            return;
        }
        if ( m_client_interceptors.isEmpty() )
        {
            return;
        }
        ClientRequestInterceptor [] list
              = new ClientRequestInterceptor[ m_client_interceptors.size() ];

        m_client_interceptors.toArray( list );

        ClientManager manager = null;

        Object [] args = new Object[ 2 ];

        args[ 0 ] = list;

        args[ 1 ] = pi_current;

        try
        {
            manager = ( ClientManager ) m_loader.constructClass(
                "openorb.pi.ClientManagerClass",
                "org.openorb.orb.pi.SimpleClientManager", args );
        }
        catch ( final Exception ex )
        {
            getLogger().error(
                "Error constructing class openorb.pi.ClientManagerClass.",
                ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                  "Error constructing class openorb.pi.ClientManagerClass ("
                  + ex + ")" ), ex );
        }

        if ( manager != null )
        {
            m_orb.setFeature( "ClientInterceptorManager", manager );
        }
    }

    private void create_server_interceptor_manager( CurrentImpl pi_current )
    {
        if ( !m_loader.getBooleanProperty( "openorb.server.enable", true ) )
        {
            return;
        }
        if ( m_server_interceptors.isEmpty() )
        {
            return;
        }
        ServerRequestInterceptor [] list
              = new ServerRequestInterceptor[ m_server_interceptors.size() ];

        m_server_interceptors.toArray( list );

        ServerManager manager = null;

        Object [] args = new Object[ 2 ];

        args[ 0 ] = list;

        args[ 1 ] = pi_current;

        try
        {
            manager = ( ServerManager ) m_loader.constructClass(
                "openorb.pi.ServerManagerClass",
                "org.openorb.orb.pi.SimpleServerManager", args );
        }
        catch ( final Exception ex )
        {
            getLogger().error(
                "Error constructing class openorb.pi.ServerManagerClass.",
                ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                  "Error constructing class openorb.pi.ServerManagerClass ("
                  + ex + ")" ), ex );
        }

        if ( manager != null )
        {
            m_orb.setFeature( "ServerInterceptorManager", manager );
        }
    }

    private void create_ior_interceptor_manager()
    {
        if ( !m_loader.getBooleanProperty( "openorb.server.enable", true ) )
        {
            return;
        }
        IORInterceptor [] list
              = new IORInterceptor[ m_ior_interceptors.size() ];

        m_ior_interceptors.toArray( list );

        IORManager manager = null;

        Object [] args = new Object[ 1 ];

        args[ 0 ] = list;

        try
        {
            manager = ( IORManager ) m_loader.constructClass(
                "org.openorb.PI.IORManagerClass",
                "org.openorb.orb.pi.SimpleIORManager", args );
        }
        catch ( final Exception ex )
        {
            getLogger().error(
                "Error constructing class org.openorb.PI.IORManagerClass.",
                ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                  "Error constructing class org.openorb.PI.IORManagerClass ("
                  + ex + ")" ), ex );
        }

        if ( manager != null )
        {
            m_orb.setFeature( "IORInterceptorManager", manager );
        }
    }

    // Feature init info interface

    /**
     * Return an orb instance.
     */
    public org.openorb.orb.core.ORB orb()
    {
        return m_orb;
    }

    /**
     * Return a reference to the orb loader.
     * This is a shortcut to orb().getLoader().
     */
    public org.openorb.orb.config.ORBLoader getLoader()
    {
        return m_orb.getLoader();
    }

    /**
     * Set a feature.
     */
    public void setFeature( String feature, java.lang.Object reference )
    {
        m_orb.setFeature( feature, reference );
    }

    /**
     * Get feature.
     */
    public java.lang.Object getFeature( String feature )
    {
        return m_orb.getFeature( feature );
    }

    // org.omg.PortableInterceptor.ORBInitInfo interface.

    /**
     * Return ORB arguments
     */
    public String[] arguments()
    {
        return m_args;
    }

    /**
     * Return ORB ID.
     */
    public String orb_id()
    {
        return "OpenORB";
    }

    /**
     * Return the codec factory
     */
    public org.omg.IOP.CodecFactory codec_factory()
    {
        return ( org.omg.IOP.CodecFactory ) m_codec_manager;
    }

    /**
     * Register a new initial reference.
     */
    public void register_initial_reference( String id,
                                            org.omg.CORBA.Object obj )
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName
    {
        try
        {
            resolve_initial_references( id );
        }
        catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName ex )
        {
            // The reference was not found, so we can register it
            m_orb.addInitialReference( id, obj );
            return;
        }
        // The reference is already registered
        throw new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName();
    }

    /**
     * Resolve an initial reference
     */
    public org.omg.CORBA.Object resolve_initial_references(
        String id )
        throws InvalidName
    {
        try
        {
            return m_orb.resolve_initial_references( id );
        }
        catch ( final org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            throw ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName )
                    ExceptionTool.initCause(
                    new InvalidName(), ex );
        }
    }

    /**
     * This operation is used to add a new client request interceptor
     */
    public void add_client_request_interceptor(
        ClientRequestInterceptor interceptor )
        throws DuplicateName
    {
        check_duplicate( m_client_interceptors, interceptor.name() );

        m_client_interceptors.add( interceptor );
    }

    /**
     * This operation is used to add a new server request interceptor
     */
    public void add_server_request_interceptor(
        org.omg.PortableInterceptor.ServerRequestInterceptor interceptor )
        throws DuplicateName
    {
        check_duplicate( m_server_interceptors, interceptor.name() );

        m_server_interceptors.add( interceptor );
    }

    /**
     * This operation is used to add a new IOR interceptor
     */
    public void add_ior_interceptor(
        org.omg.PortableInterceptor.IORInterceptor interceptor )
        throws DuplicateName
    {
        check_duplicate( m_ior_interceptors, interceptor.name() );

        m_ior_interceptors.add( interceptor );
    }

    /**
     * Return a slot id.
     */
    public int allocate_slot_id()
    {
        return m_slots++;
    }

    /**
     * This operation is used to register a policy factory
     */
    public void register_policy_factory( int type,
        org.omg.PortableInterceptor.PolicyFactory policy_factory )
    {
        org.openorb.orb.policy.PolicyFactoryManager policyFactoryManager
            = ( org.openorb.orb.policy.PolicyFactoryManager )
            m_orb.getFeature( "PolicyFactoryManager" );

        policyFactoryManager.add_policy_factory( type, policy_factory );
    }

    /**
     * Check if an interceptor is already stored into a list
     */
    private void check_duplicate( ArrayList list, String name )
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
    {
        if ( name.length() == 0 )
        {
            return;
        }
        Iterator itt = list.iterator();

        while ( itt.hasNext() )
        {
            if ( name.equals( ( ( Interceptor ) itt.next() ).name() ) )
            {
                throw new DuplicateName();
            }
        }
    }

    //=====================================================================
    // Disposable
    //=====================================================================

   /**
    * Called by the ORB Initilizer during shutdown enabling cleanup of
    * pluggable extensions.
    */
    public void dispose()
    {
        final Set list = new HashSet();

        for ( int i = 0; i < m_orb_inits.length; i++ )
        {
            final Object object = m_orb_inits[i];
            if ( !list.contains( object ) && ( object instanceof Disposable ) )
            {
                try
                {
                    ( ( Disposable ) object ).dispose();
                }
                catch ( final Throwable e )
                {
                    final String warn =
                        "Ignoring error during initializer disposal.";
                    getLogger().warn( warn, e );
                }
                finally
                {
                    list.add( object );
                }
            }
        }

        for ( int i = 0; i < m_feature_inits.length; i++ )
        {
            final Object object = m_feature_inits[i];
            if ( !list.contains( object ) && ( object instanceof Disposable ) )
            {
                try
                {
                    ( ( Disposable ) object ).dispose();
                }
                catch ( final Throwable e )
                {
                    final String warn =
                        "Ignoring error during feature disposal.";
                    getLogger().warn( warn, e );
                }
                finally
                {
                    list.add( object );
                }
            }
        }
    }
}

