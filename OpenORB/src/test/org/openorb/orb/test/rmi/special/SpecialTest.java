/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.special;

import org.openorb.orb.test.rmi.RMITestCase;

import org.openorb.orb.test.rmi.primitive.RemoteEcho;
import org.openorb.orb.test.rmi.primitive.PrimitiveTest.EchoImpl;

import javax.rmi.PortableRemoteObject;

import junit.framework.TestSuite;

/**
 * This test suit is used in order to test some other parts of the RMI
 * over IIOP implementation :
 * - narrowing
 * - stub serialization and deserialization
 *
 * @author Jerome Daniel
 */
public class SpecialTest
    extends RMITestCase
{
    /**
     * Implementation.
     */
    private EchoImpl m_impl;

    /**
     * Stub, used by test cases.
     */
    private RemoteEcho m_stub;

    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public SpecialTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case.
     */
    protected void setUp()
    {
        super.setUp();
        try
        {
            m_impl = new EchoImpl();
            PortableRemoteObject.exportObject( m_impl );
            java.rmi.Remote remote = PortableRemoteObject.toStub( m_impl );
            m_stub = ( RemoteEcho ) PortableRemoteObject.narrow( remote, RemoteEcho.class );
        }
        catch ( Exception ex )
        {
            fail( ex.toString() );
        }
    }

    /**
     * Dispose the test case.
     */
    protected void tearDown()
    {
        try
        {
            PortableRemoteObject.unexportObject( m_impl );
        }
        catch ( Exception ex )
        {
            // ignore
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testBadNarrow()
    {
        try
        {
            PortableRemoteObject.narrow( m_stub, java.rmi.Remote.class );
        }
        catch ( java.lang.ClassCastException ex )
        {
            fail( "Exception raised when attempting to narrow to java.rmi.Remote" );
        }
        try
        {
            PortableRemoteObject.narrow( m_stub,
                  org.openorb.orb.test.rmi.complex.RemoteComplex.class );
            fail( "exception not raised in incorrent narrow" );
        }
        catch ( java.lang.ClassCastException ex )
        {
            // we are not allowed to just cast
        }
    }

    /**
     * Test case for serialization and deserialization of a stub class.
     */
    public void testSerialize()
    {
        byte[] buf;
        try
        {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream io = new java.io.ObjectOutputStream( output );
            io.writeObject( m_stub );
            io.flush();
            io.close();
            buf = output.toByteArray();
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Unable to serialize a RMI stub" );
            return;
        }
        try
        {
            java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream( buf );
            java.io.ObjectInputStream io = new java.io.ObjectInputStream( input );
            java.rmi.Remote newRemote = ( java.rmi.Remote ) io.readObject();
            // we have to explicitly connect the target after deserialization
            // before we can use the equals operation.
            ( ( javax.rmi.CORBA.Stub ) newRemote ).connect( (
                  ( javax.rmi.CORBA.Stub ) m_stub )._orb() );
            assertEquals( "Deserialize object is different from the serialized one.",
                  newRemote, m_stub );
            RemoteEcho newStub = ( RemoteEcho )
                  PortableRemoteObject.narrow( newRemote, RemoteEcho.class );
            newStub.echo_void();
        }
        catch ( java.lang.Exception ex )
        {
            fail( ex.toString() );
        }
    }

    /**
     * The entry point for the test case.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( SpecialTest.class ) );
    }
}

