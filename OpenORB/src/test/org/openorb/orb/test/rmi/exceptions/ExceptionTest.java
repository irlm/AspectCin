/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.exceptions;

import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import junit.framework.TestSuite;

import org.openorb.orb.test.rmi.RMITestCase;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * A test case checking the different exceptions.
 *
 * @author Stefan Reich
 */
public class ExceptionTest
    extends RMITestCase
{
    /**
     * Implementation.
     */
    private ExceptionImpl m_impl;

    /**
     * Stub, used by test cases.
     */
    private ExceptionTestRemote m_stub;

    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public ExceptionTest( String name )
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
            m_impl = new ExceptionImpl();
            PortableRemoteObject.exportObject( m_impl );
            Remote remote = PortableRemoteObject.toStub( m_impl );
            m_stub = ( ExceptionTestRemote ) PortableRemoteObject.narrow(
                  ( org.omg.CORBA.Object ) remote , ExceptionTestRemote.class );
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
     * Test echoing a string. Strings are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testExceptions()
        throws Exception
    {
        try
        {
            m_stub.throwCommFailure();
        }
        catch ( MarshalException e )
        {
            assertEquals( "CORBA COMM_FAILURE 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected MarshalException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwInvObjRef();
        }
        catch ( NoSuchObjectException e )
        {
            assertEquals( "CORBA INV_OBJREF 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected MarshalException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwNoPermission();
        }
        catch ( AccessException e )
        {
            assertEquals( "CORBA NO_PERMISSION 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected MarshalException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwMarshal();
        }
        catch ( MarshalException e )
        {
            assertTrue( true );
            assertEquals( "CORBA MARSHAL 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected MarshalException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwBadParam();
        }
        catch ( MarshalException e )
        {
            assertEquals( "CORBA BAD_PARAM 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected MarshalException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwObjNotExist();
        }
        catch ( NoSuchObjectException e )
        {
            assertEquals( "CORBA OBJECT_NOT_EXIST 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected NoSuchObjectException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwTaRequired();
        }
        catch ( javax.transaction.TransactionRequiredException e )
        {
            assertEquals( "CORBA TRANSACTION_REQUIRED 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected TransactionRequiredException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwTaRolledBack();
        }
        catch ( javax.transaction.TransactionRolledbackException e )
        {
            assertEquals( "CORBA TRANSACTION_ROLLEDBACK 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected TransactionRolledbackException, but got " + t.getMessage() );
        }

        try
        {
            m_stub.throwInvalidTransaction();
        }
        catch ( javax.transaction.InvalidTransactionException e )
        {
            assertEquals( "CORBA INVALID_TRANSACTION 0x0 No", e.getMessage() );
        }
        catch ( Throwable t )
        {
            fail( "Expected InvalidTransactionException, but got " + t.getMessage() );
        }

        // see Java to IDL mapping section 1.4.8.1 for the remaining test cases
        try
        {
            m_stub.throwNPE();
        }
        catch ( NullPointerException e )
        {
            // ok
        }
        catch ( Throwable t )
        {
            fail( "Expected NullPointerException, but got " + t );
        }

        try
        {
            m_stub.throwRuntimeException();
        }
        catch ( RuntimeException e )
        {
            // ok
        }
        catch ( Throwable t )
        {
            fail( "Expected RemoteException, but got " + t );
        }
    }

    /**
     * Remote interface implementation
     */
    static class ExceptionImpl
        implements org.openorb.orb.test.rmi.exceptions.ExceptionTestRemote
    {
        public void throwCommFailure()
            throws RemoteException
        {
            throw new COMM_FAILURE();
        }
        public void throwInvObjRef()
            throws RemoteException
        {
            throw new INV_OBJREF();
        }
        public void throwNoPermission()
            throws RemoteException
        {
            throw new NO_PERMISSION();
        }
        public void throwMarshal()
            throws RemoteException
        {
            throw new MARSHAL();
        }
        public void throwBadParam()
            throws RemoteException
        {
            throw new BAD_PARAM();
        }
        public void throwObjNotExist()
            throws RemoteException
        {
            throw new OBJECT_NOT_EXIST();
        }
        public void throwTaRequired()
            throws RemoteException
        {
            throw new TRANSACTION_REQUIRED();
        }
        public void throwTaRolledBack()
            throws RemoteException
        {
            throw new TRANSACTION_ROLLEDBACK();
        }
        public void throwInvalidTransaction()
            throws RemoteException
        {
            throw new INVALID_TRANSACTION();
        }
        public void throwThrowable()
            throws RemoteException
        {
            //throw new Throwable();
        }
        public void throwNPE()
            throws RemoteException
        {
            throw new NullPointerException();
        }
        public void throwRuntimeException()
            throws RemoteException
        {
            throw new RuntimeException();
        }
        public void throwError()
            throws RemoteException
        {
            throw new Error();
        }
    }

    /**
     * The entry point of this application.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( ExceptionTest.class ) );
    }
}

