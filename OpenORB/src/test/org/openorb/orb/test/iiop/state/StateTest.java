/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.state;

import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.SetOverrideType;

import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;

/**
 * Tests bidirectional IIOP.
 *
 * @author Chris Wood
 */
public class StateTest
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public StateTest( String name )
    {
        super( name );
    }

    private static final int REPEATS = 5;

    private Policy m_biDirPolicy = null;
    private org.omg.CORBA.ORB m_orb;
    private org.omg.CORBA.ORB m_orb2;

    private StateTarget m_ref;
    private StateTarget m_ref2;

    /**
     * This is the specialized setup, called directly from the test functions.
     *
     * @param props Test case properties.
     * @param withBiDir A flag to use bi-directional IIOP.
     */
    private void setUp( Properties props, boolean withBiDir )
        throws org.omg.CORBA.UserException
    {
        if ( props == null )
        {
            props = new Properties();
        }
        props.setProperty( "openorb.useStaticThreadGroup", "true" );
        if ( withBiDir )
        {
            props.setProperty( "iiop.allowBidir", "true" );
            m_orb2 = org.omg.CORBA.ORB.init( ( String[] ) null, props );
            org.omg.CORBA.Any any = m_orb2.create_any();
            any.insert_ushort( org.omg.BiDirPolicy.BOTH.value );
            m_biDirPolicy = m_orb2.create_policy(
                  org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value, any );
            Thread.currentThread().interrupt();
            m_orb2.run();
            Thread.interrupted();
        }
        m_orb = org.omg.CORBA.ORB.init( ( String[] ) null, props );
        Thread.currentThread().interrupt();
        m_orb.run();
        Thread.interrupted();
        m_ref = initRef( m_orb );
        if ( withBiDir )
        {
            m_ref2 = StateTargetHelper.narrow(
                  m_orb.string_to_object( m_orb2.object_to_string( initRef( m_orb2 ) ) ) );
        }
    }

    /**
     * Dispose the test case.
     */
    public void tearDown()
    {
        if ( m_orb != null )
        {
            m_orb.shutdown( true );
        }
        if ( m_orb2 != null )
        {
            m_orb2.shutdown( true );
        }
    }

    private StateTarget initRef( org.omg.CORBA.ORB orb ) throws org.omg.CORBA.UserException
    {
        POA rootPOA = ( POA ) orb.resolve_initial_references( "RootPOA" );
        Policy[] policies = new Policy[ ( m_biDirPolicy == null ) ? 2 : 3 ];
        policies[ 0 ] = orb.create_policy( org.openorb.orb.policy.FORCE_MARSHAL_POLICY_ID.value,
              orb.create_any() );
        policies[ 1 ] = rootPOA.create_implicit_activation_policy(
              ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );
        if ( m_biDirPolicy != null )
        {
            policies[ 2 ] = m_biDirPolicy;
        }
        // set client policies, use bidir and force marshalling
        PolicyManager opm = ( PolicyManager ) orb.resolve_initial_references( "ORBPolicyManager" );
        opm.set_policy_overrides( policies, SetOverrideType.ADD_OVERRIDE );
        // setup poa
        POA poa = rootPOA.create_POA( "BidirAdapter", rootPOA.the_POAManager(), policies );
        // activate root poa
        rootPOA.the_POAManager().activate();
        return ( new StateTargetImpl( poa ) )._this( orb );
    }

    /**
     * Tests pause from the client side. This test will actualy work only
     * statisticaly, hence the repeating.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     * @exception InterruptedException if the thread is interrupted for any reason
     */
    public void testClientPause()
        throws org.omg.CORBA.UserException, InterruptedException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testClientPause" );
        Properties props = new Properties();
        props.setProperty( "openorb.client.reapPauseDelay", "200" );
        setUp( props, false );
        for ( int i = 0; i < REPEATS; ++i )
        {
            m_ref.ping();
            Thread.sleep( 500 );
        }
    }

    /**
     * Tests pause from the server side. This test will actualy work only
     * statisticaly, hence the repeating.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     * @exception InterruptedException if the thread is interrupted for any reason
     */
    public void testServerPause()
        throws org.omg.CORBA.UserException, InterruptedException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testServerPause" );
        Properties props = new Properties();
        props.setProperty( "openorb.server.reapCloseDelay", "500" );
        setUp( props, false );
        for ( int i = 0; i < REPEATS; ++i )
        {
            m_ref.ping();
            Thread.sleep( 700 );
        }
    }

    /**
     * Tests pause from the initiating side of a bidir connection. This test
     * will actualy work only statisticaly, hence the repeating.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     * @exception InterruptedException if the thread is interrupted for any reason
     */
    public void testBiDirClientPause()
        throws org.omg.CORBA.UserException, InterruptedException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testBiDirClientPause" );
        Properties props = new Properties();
        props.setProperty( "openorb.client.reapPauseDelay", "200" );
        setUp( props, true );
        for ( int i = 0; i < REPEATS; ++i )
        {
            m_ref2.call_ping( m_ref );
            Thread.sleep( 500 );
        }
        // try to reuse previous ref in new call
        m_ref2.call_ping( null );
    }

    /**
     * Tests pause from the listening side of a bidir connection. This test will
     * actualy work only statisticaly, hence the repeating.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     * @exception InterruptedException if the thread is interrupted for any reason
     */
    public void testBiDirServerPause()
        throws org.omg.CORBA.UserException, InterruptedException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testBiDirServerPause" );
        Properties props = new Properties();
        props.setProperty( "openorb.server.reapCloseDelay", "700" );
        setUp( props, true );
        for ( int i = 0; i < REPEATS; ++i )
        {
            m_ref2.call_ping( m_ref );
            Thread.sleep( 1000 );
        }
        // try to reuse previous ref in new call
        m_ref2.call_ping( null );
    }

    private static class StateTargetImpl
        extends StateTargetPOA
    {
        private POA m_poa;

        private StateTarget m_lastTest;

        StateTargetImpl( POA poa )
        {
            m_poa = poa;
        }

        public POA _default_POA()
        {
            return m_poa;
        }

        public void ping()
        {
            // do nothing;
        }

        public boolean sleep( long time )
        {
            try
            {
                Thread.sleep( time );
                return false;
            }
            catch ( InterruptedException ex )
            {
                return true;
            }
        }

        public boolean call_sleep( StateTarget test, long time )
        {
            if ( test != null )
            {
                m_lastTest = test;
            }
            m_lastTest.ping();
            return test.sleep( time );
        }

        public void call_ping( StateTarget test )
        {
            if ( test != null )
            {
                m_lastTest = test;
            }
            m_lastTest.ping();
        }
    }

    /**
     * The entry point of the test-case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing the " + StateTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( StateTest.class ) );
    }
}

