/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

import java.sql.Timestamp;

import java.util.BitSet;

import javax.rmi.PortableRemoteObject;

import junit.framework.TestSuite;

import org.openorb.orb.rmi.DefaultORB;

import org.openorb.orb.test.rmi.RMITestCase;

/**
 * This test suit provides addition tests cases for complex RMI over
 * IIOP data types. In particular, it exchanges complexe data types
 * like strings, serializable ojects, remote objects and classes.
 *
 * @author Jerome Daniel
 * @author Michael Rumpf
 */
public class ComplexTest
    extends RMITestCase
{
    /** Implementation. */
    private ComplexImpl m_impl;

    /** Stub, used by test cases. */
    private RemoteComplex m_stub;

    /** Stub, used by test cases. */
    private java.rmi.Remote m_remote;

    private org.omg.CORBA.ORB m_orb;
    private org.omg.CORBA.Any m_any;

    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public ComplexTest( String name )
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
            m_orb = DefaultORB.getORB();
            m_impl = new ComplexImpl();
            PortableRemoteObject.exportObject( m_impl );
            m_remote = PortableRemoteObject.toStub( m_impl );
            m_stub = ( RemoteComplex ) PortableRemoteObject.narrow( ( org.omg.CORBA.Object )
                  m_remote , RemoteComplex.class );
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
    public void testString()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null string", m_stub.echo_string( null ) );

        // echo as string
        String str = "test";
        String echo = m_stub.echo_string( str );
        assertEquals( "Echoed string is not equal to sent string", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );
        assertEquals( "Echoed string as object is not equal to sent string", str, e );
    }

    /**
     * Test echoing a Long. Long are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testLong()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null long", m_stub.echo_long( null ) );

        // echo as long
        Long str = new Long( 1 );
        Long echo = m_stub.echo_long( str );
        assertEquals( "Echoed long is not equal to sent long", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );
        assertEquals( "Echoed long as object is not equal to sent long", str, e );
    }

    /**
     * Test echoing a Long. Long are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testInteger()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null int", m_stub.echo_int( null ) );

        // echo as long
        Integer str = new Integer( 1 );
        Integer echo = m_stub.echo_int( str );
        assertEquals( "Echoed int is not equal to sent long", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );
        assertEquals( "Echoed int as object is not equal to sent int", str, e );
    }

    /**
     * Test echoing a Long. Long are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testFloat()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null int", m_stub.echo_float( null ) );

        // echo as long
        Float str = new Float( 1 );
        Float echo = m_stub.echo_float( str );
        assertEquals( "Echoed int is not equal to sent long", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );
        assertEquals( "Echoed int as object is not equal to sent int", str, e );
    }

    /**
     * Test echoing a Long. Long are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testDouble()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null int", m_stub.echo_double( null ) );

        // echo as long
        Double str = new Double( 1 );
        Double echo = m_stub.echo_double( str );
        assertEquals( "Echoed int is not equal to sent long", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );

        assertEquals( "Echoed int as object is not equal to sent int", str, e );
    }

    /**
     * Test echoing a Long. Long are marshalled as valuetypes,
     * which allows nulls and multiple indirected writes.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testAny()
        throws Exception
    {
        // echo as long
        Double str = new Double( 1 );
        Double echo = m_stub.echo_double( str );
        assertEquals( "Echoed int is not equal to sent long", str, echo );

        // echo as object.
        Object e = m_stub.echo_object( str );
        assertEquals( "Echoed int as object is not equal to sent int", str, e );
    }


    /**
     * Test echoing a java.util.Vector, a serializable object. This test
     * tries the RMI over IIOP serializer engine.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testVector()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null vector", m_stub.echo_vector( null ) );

        // try to marshal an empty vector
        java.util.Vector empty = new java.util.Vector( 0 );
        java.util.Vector ret_empty = m_stub.echo_vector( empty );

        java.util.Vector val = new java.util.Vector( 30 );
        Integer element1 = new Integer( 5 );
        String element2 = "test string";
        byte[] element3 = new byte[ 50 ];
        org.omg.CORBA.Object element4 = ( org.omg.CORBA.Object ) m_stub;
        for ( int i = 0; i < 50; i++ )
        {
            element3[ i ] = ( byte ) i;
        }
        val.addElement( element1 );
        val.addElement( element2 );
        val.addElement( element3 );
        val.addElement( element4 );
        val.addElement( element1 );
        val.addElement( element1 );
        val.addElement( element1 );
        val.addElement( element1 );
        val.addElement( element2 );
        val.addElement( element2 );
        val.addElement( element2 );
        val.addElement( element2 );
        val.addElement( element3 );
        val.addElement( element3 );
        val.addElement( element3 );
        val.addElement( element3 );
        val.addElement( element4 );
        val.addElement( element4 );
        val.addElement( element4 );
        val.addElement( element4 );

        // echo as vector.
        java.util.Vector ret = m_stub.echo_vector( val );
        Integer e1 = ( Integer ) ret.elementAt( 0 );
        String e2 = ( String ) ret.elementAt( 1 );
        byte[] e3 = ( byte[] ) ret.elementAt( 2 );
        org.omg.CORBA.Object e4 = ( org.omg.CORBA.Object ) ret.elementAt( 3 );
        assertEquals( "Failed Vector exchanged on Integer", e1.intValue(), element1.intValue() );
        assertEquals( "Failed Vector exchanged on String", e2, element2 );
        for ( int i = 0; i < e3.length; i++ )
        {
            assertEquals( "Failed Vector exchanged on byte array", e3[ i ], element3[ i ] );
        }
        assertEquals( "Failed Vector exchanged on org.omg.CORBA.Object", e4, element4 );

        // echo as object
        ret = ( java.util.Vector ) m_stub.echo_object( val );
        e1 = ( Integer ) ret.elementAt( 0 );
        e2 = ( String ) ret.elementAt( 1 );
        e3 = ( byte[] ) ret.elementAt( 2 );
        e4 = ( org.omg.CORBA.Object ) ret.elementAt( 3 );
        assertEquals( "Failed Vector exchanged on Integer",
                      e1.intValue(), element1.intValue() );
        assertEquals( "Failed Vector exchanged on String",
                      e2, element2 );
        for ( int i = 0; i < e3.length; i++ )
        {
            assertEquals( "Failed Vector exchanged on byte array", e3[ i ], element3[ i ] );
        }
        assertEquals( "Failed Vector exchanged on org.omg.CORBA.Object",
                      e4, element4 );
    }

    /**
     * Echo a java.lang.Class.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testClass()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null class", m_stub.echo_class0( null ) );

        Class send = Integer.class;

        // echo as class
        Class clz = m_stub.echo_class0( send );

        assertEquals( "Failed class exchange",
                      send, clz );

        // echo as object
        clz = ( Class ) m_stub.echo_object( send );

        assertEquals( "Failed class exchange as object",
                      send, clz );
    }

    /**
     * Echo a java.lang.Object.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testObject()
        throws Exception
    {
        String str = new String( "new string" );
        // echo as object
        Object ret = m_stub.echo_object( str );
        assertEquals( "Echoed object not comparing equal to original", str, ret );

        //
        // BitSet
        //
        BitSet set = new BitSet( 64 );
        java.lang.Object obj = m_stub.echo_object( set );
        assertTrue( "Echoed BitSet as Object is not equal to the one that was sent",
              obj.equals( set ) );

        java.lang.Object retobj = m_stub.return_object();
        assertTrue( "Returned BitSet as Object is not equal to '" + set + "'",
              retobj.equals( set ) );
    }

    /**
     * Echo a remote object.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testEchoRemote()
        throws Exception
    {
        // as remote.
        java.rmi.Remote remote = m_stub.echo_remote0();
        RemoteComplex cpx = ( RemoteComplex )
              PortableRemoteObject.narrow( remote, RemoteComplex.class );
        assertEquals( "Echoed reference not comparing equal to narrowed", remote, cpx );

        RemoteComplex cpx1 = m_stub.echo_remote1( cpx );
        assertEquals( "Echoed reference not comparing equal", cpx, cpx1 );

        RemoteComplex cpx2 = m_stub.echo_remote2();
        assertEquals( "Received reference not comparing equal", cpx1, cpx2 );

        m_stub.echo_remote3( cpx );

        String str = cpx.echo_string( "test string" );
        assertEquals( "Echoed object not comparing equal to original", str, "test string" );
    }

    /**
     * Write some objects which use some of the custom serializable functions
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testEchoNull()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null object", m_stub.echo_object( null ) );
    }

    /**
     * Write some objects which use some of the custom serializable functions
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testCustomSerializable()
        throws Exception
    {
        // Date uses writeObject and readObject
        java.util.Date date = new java.util.Date();
        Object ret = m_stub.echo_object( date );
        assertEquals( "Date does not match", date, ret );

        // SQL date is a subclass of date but has the same serialization.
        java.sql.Date sdate = new java.sql.Date( date.getTime() );
        ret = m_stub.echo_object( sdate );
        assertEquals( "SQL Date does not match", sdate, ret );

        // ArrayList uses writeObject and readObject, and it's parent, abstract list
        // uses default serialization
        java.util.ArrayList arrList = new java.util.ArrayList();
        arrList.add( date );

        // do a recursive just for fun..
        arrList.add( arrList );
        ret = m_stub.echo_object( arrList );
        java.util.ArrayList arrRet = ( java.util.ArrayList ) ret;
        assertEquals( arrRet.size(), arrList.size() );
        assertEquals( arrRet.get( 0 ), arrList.get( 0 ) );
        assertEquals( arrRet.get( 1 ), arrRet );
    }

    /**
     * Test echoing a java.rmi.Remote array, a serializable object. This test
     * tries the RMI over IIOP serializer engine, especially the readArray method.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testRemoteArray()
        throws Exception
    {
        int i;
        s_objs = new ComplexImpl[ s_count ];
        s_stubs = new RemoteComplex[ s_count ];
        for ( i = 0; i < s_count; i++ )
        {
            s_objs[ i ] = new ComplexImpl();
            PortableRemoteObject.exportObject( s_objs[ i ] );
            java.rmi.Remote remote = PortableRemoteObject.toStub( s_objs[ i ] );
            s_stubs[ i ] = ( RemoteComplex )
                  PortableRemoteObject.narrow( remote, RemoteComplex.class );
        }
        java.rmi.Remote[] ret0 = m_stub.echo_remotearray0( s_objs );
        for ( i = 0; i < s_count; i++ )
        {
            assertTrue( "Elements of ret0 are not the same as elements of s_stubs",
                  s_stubs[ i ].equals( ret0[ i ] ) );
        }
        RemoteComplex[] ret1 = m_stub.echo_remotearray1( s_objs );
        for ( i = 0; i < s_count; i++ )
        {
            assertTrue( "Elements of ret1 are not assignable as elements of s_stubs",
                  s_stubs[ i ].equals( ret1[ i ] ) );
        }
        RemoteComplex[] ret2 = m_stub.echo_remotearray2( );
        for ( i = 0; i < s_count; i++ )
        {
            assertTrue( "Elements of ret2 are not assignable as elements of s_stubs",
                  s_stubs[ i ].equals( ret2[ i ] ) );
        }
        m_stub.echo_remotearray3( s_objs );
        for ( i = 0; i < s_count; i++ )
        {
            PortableRemoteObject.unexportObject( s_objs[ i ] );
        }
    }

    /**
     * Test echoing CORBA Objects and specialized versions of type CosNaming.NamingContext.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testCorbaObjects()
        throws Exception
    {
        org.omg.CORBA.Object obj = ( org.omg.CORBA.Object ) m_remote;
        org.omg.CORBA.Object obj_ret = m_stub.echo_corbaobject( obj );
        assertTrue( "Send obj=" + obj + ", Received obj_ret=" + obj_ret, obj.equals( obj_ret ) );
    }

    /**
     * Test echoing CORBA Objects.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testTypeHolder()
        throws Exception
    {
        TypeHolder th = new TypeHolder();
        TypeHolder ret = m_stub.echo_typeholder( th );

        assertEquals( "Echoed object not comparing equal to original", th, ret );
    }

    /**
     * Test the PutField serialization mechanism.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testPutFields()
        throws Exception
    {
        PutFieldTest pft = new PutFieldTest();
        PutFieldTest ret = m_stub.echo_pft( pft );
        assertEquals( "Echoed object not comparing equal to original", pft, ret );
    }

    /**
     * Test the serialPersistentFields serialization mechanism.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testSerialPersistentFields()
        throws Exception
    {
        SerialPersistentFieldsTest spft = new SerialPersistentFieldsTest();
        SerialPersistentFieldsTest ret = m_stub.echo_spft( spft );
        assertEquals( "Echoed object not comparing equal to original", spft, ret );
    }

    /**
     * Test Java collections.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testCollections()
        throws Exception
    {
        SubArrayList send = new SubArrayList();
        SubArrayList al = ( SubArrayList ) m_stub.echo_collection( send );
        assertEquals( "Echoed object not comparing equal to original", send, al );
    }

    /**
     * Test IDLEntity derived types.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testIDLEntity()
        throws Exception
    {
        // Any
        org.omg.CORBA.Any any = null;
        org.omg.CORBA.Any anyEcho = m_stub.echo_any( any );
        assertEquals( "Echoed object not comparing equal to original", anyEcho, any );

        org.omg.CORBA.Any[] anys = new org.omg.CORBA.Any[ 2 ];
        anys[ 0 ] = m_orb.create_any();
        anys[ 0 ].insert_long( 0 );
        anys[ 1 ] = m_orb.create_any();
        anys[ 1 ].insert_long( 1 );
        org.omg.CORBA.Any[] anysEcho = m_stub.echo_any( anys );

        // TypeCode
        org.omg.CORBA.TypeCode tc = null;
        org.omg.CORBA.TypeCode tcEcho = m_stub.echo_typecode( tc );
        assertEquals( "Echoed object not comparing equal to original", tcEcho, tc );

        org.omg.CORBA.TypeCode[] tcs = new org.omg.CORBA.TypeCode[ 2 ];
        tcs[ 0 ] = m_orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_long );
        tcs[ 1 ] = m_orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_long );
        org.omg.CORBA.TypeCode[] tcsEcho = m_stub.echo_typecode( tcs );

        // IDLStruct
        IDLStruct ent = new IDLStruct();
        IDLStruct entEcho = m_stub.echo_entity( ent );

        IDLStruct[] ents = new IDLStruct[ 2 ];
        ents[ 0 ] = new IDLStruct( ( short ) 0 );
        ents[ 1 ] = new IDLStruct( ( short ) 1 );
        IDLStruct[] entsEcho = m_stub.echo_entity( ents );
    }

    /**
     * Test the Throwable type.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testThrowable()
        throws Exception
    {
        if ( org.openorb.util.JREVersion.V1_4 )
        {
            String msg = "Test Throwable!";
            Throwable th = new Throwable( msg );
            Throwable thEcho = m_stub.echo_throwable( th );
            assertTrue( thEcho.getMessage().equals( msg ) );
        }
    }

    /**
     * Test the BigDecimal type.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testBigDecimal()
        throws Exception
    {
        java.math.BigDecimal bd = new java.math.BigDecimal( "1000000000000000000000000.0" );
        java.math.BigDecimal bdEcho = m_stub.echo_bigdecimal( bd );
        assertEquals( "Echoed BigDecimal not comparing equal to original", bdEcho, bd );
    }

    /**
     * Test echoing a java.rmi.Remote array, a serializable object. This test
     * tries the RMI over IIOP serializer engine, especially the readArray method.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testRemoteObjectArray()
        throws Exception
    {
        int i;
        s_objs = new ComplexImpl[ s_count ];
        s_stubs = new RemoteComplex[ s_count ];
        java.lang.Object[] objs = new java.lang.Object[ s_count ];
        for ( i = 0; i < s_count; i++ )
        {
            s_objs[ i ] = new ComplexImpl();
            PortableRemoteObject.exportObject( s_objs[ i ] );
            java.rmi.Remote remote = PortableRemoteObject.toStub( s_objs[ i ] );
            s_stubs[ i ] = ( RemoteComplex )
                  PortableRemoteObject.narrow( remote, RemoteComplex.class );
            objs[ i ] = s_stubs[ i ];
        }

        java.lang.Object[] objsEcho = m_stub.echo_remoteobjectarray( objs );
        for ( i = 0; i < s_count; i++ )
        {
            assertTrue( "Elements of objsEcho are not the same as elements of objs",
                  objs[ i ].equals( objsEcho[ i ] ) );
        }
        for ( i = 0; i < s_count; i++ )
        {
            PortableRemoteObject.unexportObject( s_objs[ i ] );
        }
    }

    /**
     * Test echoing a java.rmi.Remote array, a serializable object. This test
     * tries the RMI over IIOP serializer engine, especially the readArray method.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testMultipleObjectArray()
        throws Exception
    {
        java.lang.Object[] objs = new java.lang.Object[ 10 ];
        objs[ 0 ] = new Integer( 100 );
        objs[ 1 ] = new java.util.Vector( 20 );
        objs[ 2 ] = new java.util.ArrayList( 10 );
        objs[ 3 ] = new TypeHolder();
        objs[ 4 ] = null;
        objs[ 5 ] = null;
        objs[ 6 ] = null;
        objs[ 7 ] = null;
        objs[ 8 ] = null;
        objs[ 9 ] = m_stub;
        java.lang.Object[] objsEcho = m_stub.echo_remoteobjectarray( objs );
        for ( int i = 0; i < s_count; i++ )
        {
            assertTrue( "Elements of objsEcho are not the same as elements of objs",
                  objs[ i ].equals( objsEcho[ i ] ) );
        }
    }

    /**
     * This method tests whether we can marshal a Serializable interface.
     * This crashed the VM in previous versions because the server tried to
     * call DeserializationKernel.allocateNewObject with the interface
     * instead of the implementation class.
     *
     * @exception Exception If any unexpected problem occurs in the test case
     */
    public void testSerialInterfaceMarshal()
        throws Exception
    {
        SerialItf si = new SerialItfImpl();
        SerialItf siEcho = m_stub.echo_serialitf( si );
    }

    /**
     * This method tests whether we can marshal and unmarshal a class of
     * type java.lang.math.BitSet correctly.
     */
    public void testBitSet()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null BitSet", m_stub.echo_bitset( null ) );

        // echo as BitSet
        BitSet bs = new BitSet( 128 );
        BitSet echo = m_stub.echo_bitset( bs );
        assertEquals( "Echoed BitSet is not equal to sent BitSet", bs, echo );

        // echo as object.
        Object obj = m_stub.echo_object( bs );
        assertEquals( "Echoed BitSet as object is not equal to sent BitSet", bs, obj );
    }

    /**
     * This method tests whether we can marshal and unmarshal a class of
     * type java.sql.Timestamp correctly.
     */
    public void testTimestamp()
        throws Exception
    {
        // echo as null
        assertNull( "Could not echo null java.sql.Timestamp", m_stub.echo_timestamp( null ) );

        // echo as Timestamp
        Timestamp ts = new Timestamp( System.currentTimeMillis() );
        Timestamp echo = m_stub.echo_timestamp( ts );
        assertEquals( "Echoed Timestamp is not equal to sent Timestamp", ts, echo );

        // echo as object.
        Object obj = m_stub.echo_object( ts );
        assertEquals( "Echoed Timestamp as object is not equal to sent Timestamp", ts, obj );
    }

    private static RemoteComplex[] s_stubs = null;
    private static ComplexImpl[] s_objs = null;
    private static int s_count = 3;

    /**
     * Remote interface implementation
     */
    static class ComplexImpl
        implements RemoteComplex
    {
        public java.lang.String echo_string( String val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public Integer echo_int( Integer val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public Long echo_long( Long val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public Float echo_float( Float val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public Double echo_double( Double val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public org.omg.CORBA.Any echo_any( org.omg.CORBA.Any val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public org.omg.CORBA.Any[] echo_any( org.omg.CORBA.Any[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public java.util.Vector echo_vector( java.util.Vector val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public java.lang.Object echo_object( java.lang.Object val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public java.lang.Object return_object()
            throws java.rmi.RemoteException
        {
            return new BitSet( 64 );
        }

        public java.lang.Class echo_class0( java.lang.Class val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public TypeHolder echo_typeholder( TypeHolder th )
            throws java.rmi.RemoteException
        {
            return th;
        }

        public java.rmi.Remote echo_remote0()
            throws java.rmi.RemoteException
        {
            return this;
        }

        public RemoteComplex echo_remote1( RemoteComplex val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public RemoteComplex echo_remote2()
            throws java.rmi.RemoteException
        {
            return this;
        }

        public void echo_remote3( RemoteComplex val )
            throws java.rmi.RemoteException
        {
        }

        /**
         * Checks whether it works to echo the base type. This includes
         * marshalling in both ways and on both sides.
         */
        public java.rmi.Remote[] echo_remotearray0( java.rmi.Remote[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Checks whether it works to echo the interface type, which is
         * derived from java.rmi.Remote.  This includes marshalling in
         * both ways and on both sides.
         */
        public RemoteComplex[] echo_remotearray1( RemoteComplex[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Checks whether it works to retrieve interfaces from the server
         * side.
         */
        public RemoteComplex[] echo_remotearray2( )
            throws java.rmi.RemoteException
        {
            return s_objs;
        }

        /**
         * Checks whether it works to send interfaces to the server side.
         */
        public void echo_remotearray3( RemoteComplex[] val )
           throws java.rmi.RemoteException
        {
        }

        /**
         * Checks whether it works to send general CORBA objects between server and client.
         */
        public org.omg.CORBA.Object echo_corbaobject( org.omg.CORBA.Object obj )
            throws java.rmi.RemoteException
        {
            return obj;
        }

        public java.util.ArrayList echo_collection( java.util.ArrayList val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public org.omg.CORBA.TypeCode echo_typecode( org.omg.CORBA.TypeCode tc )
            throws java.rmi.RemoteException
        {
            return tc;
        }

        public org.omg.CORBA.TypeCode[] echo_typecode( org.omg.CORBA.TypeCode[] tc )
            throws java.rmi.RemoteException
        {
            return tc;
        }

        public IDLStruct echo_entity( IDLStruct ent )
            throws java.rmi.RemoteException
        {
            return ent;
        }

        public IDLStruct[] echo_entity( IDLStruct[] ents )
            throws java.rmi.RemoteException
        {
            return ents;
        }

        public PutFieldTest echo_pft( PutFieldTest pft )
            throws java.rmi.RemoteException
        {
            return pft;
        }

        public SerialPersistentFieldsTest echo_spft( SerialPersistentFieldsTest spft )
            throws java.rmi.RemoteException
        {
            return spft;
        }

        public Throwable echo_throwable( Throwable th )
            throws java.rmi.RemoteException
        {
            return th;
        }

        public java.math.BigDecimal echo_bigdecimal( java.math.BigDecimal bd )
            throws java.rmi.RemoteException
        {
            return bd;
        }

        public java.lang.Object[] echo_remoteobjectarray( Object[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        public SerialItf echo_serialitf( SerialItf si )
            throws java.rmi.RemoteException
        {
            return si;
        }

        public BitSet echo_bitset( BitSet bs )
            throws java.rmi.RemoteException
        {
            return bs;
        }

        public Timestamp echo_timestamp( Timestamp ts )
            throws java.rmi.RemoteException
        {
            return ts;
        }
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( ComplexTest.class ) );
    }
}

