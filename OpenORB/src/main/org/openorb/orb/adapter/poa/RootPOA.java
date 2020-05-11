/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.omg.CORBA.DomainManager;
import org.omg.CORBA.DomainManagerHelper;

import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import org.openorb.orb.net.ServerManager;

import org.openorb.util.ExceptionTool;

/**
 * This class provides methods to create the RootPOA object.
 */
final class RootPOA
    extends POA
{
    private static final String [] ROOT_POA_NAME = { "POA" };
    private static final String [] PERSIST_DOMAIN_POA_NAME = { "POAPDM" };
    private static final String [] DOMAIN_POA_NAME = { "POADM" };

    private POA m_domain_manager_poa;
    private POA m_persist_domain_manager_poa;

    /**
     * Constructor.
     *
     * @param orb ORB instance for which to create the RootPOA.
     * @param policies Set of policies for the RootPOA to create.
     */
    private RootPOA( org.omg.CORBA.ORB orb, org.omg.CORBA.Policy [] policies )
        throws InvalidPolicy
    {
        super( orb, null, ROOT_POA_NAME, null, policies );
    }

    /**
     * Call this function to create a new root poa.
     */
    static POA create_root_poa( ServerManager svrmgr, org.omg.CORBA.Policy [] policies )
    {
        try
        {
            org.omg.CORBA.Policy [] pols = new org.omg.CORBA.Policy [ policies.length + 1 ];
            System.arraycopy( policies, 0, pols, 0, policies.length );
            pols[ policies.length ] = getPolicyFactory().create_implicit_activation_policy(
                   ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION );

            org.omg.CORBA.ORB orb = svrmgr.orb();

            RootPOA root = new RootPOA( orb, pols );

            pols = new org.omg.CORBA.Policy [ policies.length + 4 ];
            pols[ policies.length ] = getPolicyFactory().create_id_assignment_policy(
                    IdAssignmentPolicyValue._USER_ID );
            pols[ policies.length + 1 ] = getPolicyFactory().create_lifespan_policy(
                    LifespanPolicyValue._TRANSIENT );
            pols[ policies.length + 2 ] = getPolicyFactory().create_servant_retention_policy(
                    ServantRetentionPolicyValue._NON_RETAIN );
            pols[ policies.length + 3 ] = getPolicyFactory().create_request_processing_policy(
                    RequestProcessingPolicyValue._USE_DEFAULT_SERVANT );

            root.m_domain_manager_poa = new DomainManagerPOA( orb, root,
                    DOMAIN_POA_NAME, null, pols );

            pols[ policies.length + 1 ] = getPolicyFactory().create_lifespan_policy(
                    LifespanPolicyValue._PERSISTENT );
            root.m_persist_domain_manager_poa = new DomainManagerPOA( orb, root,
                    PERSIST_DOMAIN_POA_NAME, root.m_domain_manager_poa.getAdapterManager(), pols );

            POADomainManagerImpl defl = new POADomainManagerImpl( root );

            try
            {
                root.m_domain_manager_poa.set_servant( defl );
                root.m_persist_domain_manager_poa.set_servant( defl );
            }
            catch ( WrongPolicy ex )
            {
                if ( root.getLogger().isErrorEnabled() )
                {
                    root.getLogger().error( "WrongPolicy when setting servant.", ex );
                }
            }

            try
            {
                root.m_domain_manager_poa.getAdapterManager().activate();
            }
            catch ( org.omg.PortableServer.POAManagerPackage.AdapterInactive ex )
            {
                if ( root.getLogger().isErrorEnabled() )
                {
                    root.getLogger().error( "Newly created adapter inactive.", ex );
                }
            }

            svrmgr.register_adapter( root.getAid(), root );
            svrmgr.register_adapter( root.m_domain_manager_poa.getAid(),
                    root.m_domain_manager_poa );
            svrmgr.register_adapter( root.m_persist_domain_manager_poa.getAid(),
                    root.m_persist_domain_manager_poa );

            return root;
        }
        catch ( final InvalidPolicy ex )
        {
            org.apache.avalon.framework.logger.Logger logger =
                     ( ( org.openorb.orb.core.ORBSingleton ) svrmgr.orb() ).getLogger();

            logger.error( "Invalid policy specified.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INV_POLICY(
                    "Invalid policy specified (" + ex + ")" ), ex );
        }
    }

    /**
     * Create a domain manager instance.
     *
     * @param aid Object id for the domain manager.
     * @param persist Flag to indicate whether the object reference
     * should be persistent or transient.
     */
    DomainManager create_poa_domain_manager( byte [] aid, boolean persist )
    {
        if ( persist )
        {
            return DomainManagerHelper.narrow(
                    m_persist_domain_manager_poa.create_reference_with_id( aid,
                    DomainManagerHelper.id() ) );
        }
        return DomainManagerHelper.narrow( m_domain_manager_poa.create_reference_with_id(
                aid, DomainManagerHelper.id() ) );
    }

    /**
     * Implementation of the DomainManager class.
     */
    private static class DomainManagerPOA
        extends POA
    {
        /**
         * Creates new DomainManagerPOA
         */
        public DomainManagerPOA( org.omg.CORBA.ORB orb, POA parent, String [] poa_name,
                                 org.openorb.orb.net.AdapterManager poa_manager,
                                 org.omg.CORBA.Policy [] policies )
            throws InvalidPolicy
        {
            super( orb, parent, poa_name, poa_manager, policies );
        }

        /**
         * Create an object reference.
         * @param oid Object id for the object reference to create.
         * @param intf Type if of the object's interface.
         */
        protected org.omg.CORBA.Object create_reference( byte[] oid, java.lang.String intf )
        {
            if ( getPoaCompSet() == null )
            {
                setPoaCompSet( new org.openorb.orb.pi.ComponentSet( getServerManager().orb(),
                        getPolicySet(), null ) );
                getPoaCompSet().interception_point();
            }

            return super.create_reference( oid, intf );
        }
    }
}

