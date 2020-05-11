/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.io.IOException;
import java.io.InvalidClassException;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.HashMap;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.UtilDelegate;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.TCKind;

import org.omg.CORBA.portable.UnknownException;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;

import org.omg.PortableServer.POA;

import org.openorb.orb.iiop.CDROutputStream;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;
import org.openorb.util.IdentityKey;

/**
 * This class provides a default implementation for
 * javax.rmi.CORBA.UtilDelegate
 *
 * @author Chris Wood
 */
public class UtilDelegateImpl
    implements UtilDelegate, LogEnabled
{
    /** The instance creation sync object. */
    private static final Object SYNC_INSTANCE = new Object();

    /** The per process instance. */
    private static UtilDelegateImpl s_instance;

    /** The logger instance. */
    private static Logger s_logger;

    /** Use only local codebase, i.e. do not try to load code from remote. */
    private static boolean s_local_codebase_only = false;

    /** Flag to avoid the costly is_local call for an object instance.
        The default is false which means unknown, i.e. a _is_local() call must be done. */
    private static boolean s_prohibit_is_local_test = false;

    /** The actual flag that indicates whether the call is local or remote when the actual
        is_local() test is prohibited (@see s_prohibit_is_local_test). */
    private static boolean s_is_local_result = false;

    /** Flag to avoid the costly copyObjects call for an compiler local optimized invocations. */
    private static boolean s_copy_local_objects = true;

    /** Reference to the list of exported object. */
    private static Map s_exported_objects = new HashMap();

    /** Singleton orb. */
    private static org.openorb.orb.core.ORBSingleton s_orb =
          new org.openorb.orb.core.ORBSingleton();

    /**
     * Empty default constructor. Thi smust be provided because an instance is constructed from
     * javax.rmi.CORBA.UtilDelegate.
     */
    public UtilDelegateImpl()
    {
        createUtilDelegate();
    }

    /**
     * This constructor has been added to break the recursion.
     */
    private UtilDelegateImpl( boolean dummy )
    {
    }

    /**
     * This method creates one instance of the class per process.
     *
     * @return The UtilDelegate instance.
     */
    public static UtilDelegate createUtilDelegate()
    {
        synchronized ( SYNC_INSTANCE )
        {
            if ( s_instance == null )
            {
                // use the dummy constructor to break the recursion
                s_instance = ( UtilDelegateImpl ) new UtilDelegateImpl( true );
                s_instance.enableLogging( ( ( org.openorb.orb.core.ORB )
                      DefaultORB.getORB() ).getLogger().getChildLogger( "ud" ) );
            }
        }
        return s_instance;
    }

    public void enableLogging( Logger logger )
    {
        s_logger = logger;
    }

    public Logger getLogger()
    {
        return s_logger;
    }

    /**
     * The mapSystemException method maps a CORBA system exception to a
     * java.rmi.RemoteException or a java.rmi.RuntimeException.The
     * mapping is described in Section 1.4.8, Mapping CORBA System Exceptions to RMI
     * Exceptions, on page 1-34. If the mapped exception is an instance of
     * java.rmi.RemoteException or a subclass, the mapped exception is returned;
     * otherwise, it is thrown.
     *
     * @param ex The CORBA system exception to map.
     * @return The RemoteException to which the system exception is mapped.
     */
    public java.rmi.RemoteException mapSystemException( org.omg.CORBA.SystemException ex )
    {
        String message_pre = "CORBA ";
        String message_post = " 0x" + Integer.toHexString( ex.minor ) + " ";
        switch ( ex.completed.value() )
        {

        case org.omg.CORBA.CompletionStatus._COMPLETED_MAYBE :
            message_post = message_post + "Maybe";
            break;

        case org.omg.CORBA.CompletionStatus._COMPLETED_NO :
            message_post = message_post + "No";
            break;

        case org.omg.CORBA.CompletionStatus._COMPLETED_YES :
            message_post = message_post + "Yes";
            break;
        }
        if ( ex instanceof org.omg.CORBA.COMM_FAILURE )
        {
            return new java.rmi.MarshalException( message_pre + "COMM_FAILURE" + message_post );
        }
        if ( ex instanceof org.omg.CORBA.INV_OBJREF )
        {
            return new java.rmi.NoSuchObjectException( message_pre + "INV_OBJREF" + message_post );
        }
        if ( ex instanceof org.omg.CORBA.NO_PERMISSION )
        {
            return new java.rmi.AccessException( message_pre + "NO_PERMISSION" + message_post );
        }
        if ( ex instanceof org.omg.CORBA.MARSHAL )
        {
            return new java.rmi.MarshalException( message_pre + "MARSHAL" + message_post );
        }
        if ( ex instanceof org.omg.CORBA.OBJECT_NOT_EXIST )
        {
            return new java.rmi.NoSuchObjectException( message_pre + "OBJECT_NOT_EXIST"
                  + message_post );
        }
        if ( ex instanceof org.omg.CORBA.OBJ_ADAPTER )
        {
            return new java.rmi.NoSuchObjectException( message_pre + "OBJ_ADAPTER" + message_post );
        }
        if ( ex instanceof org.omg.CORBA.BAD_PARAM )
        {
            if ( ex.minor == ( org.omg.CORBA.OMGVMCID.value | 6 ) )
            {
                return new java.rmi.RemoteException( message_pre + "BAD_PARAM" + message_post,
                      new java.io.NotSerializableException( message_pre + "BAD_PARAM"
                      + message_post ) );
            }
            return new java.rmi.MarshalException( message_pre + "BAD_PARAM" + message_post );
        }
        try
        {
            if ( ex instanceof org.omg.CORBA.TRANSACTION_REQUIRED )
            {
                return ( java.rmi.RemoteException )
                      Thread.currentThread().getContextClassLoader().loadClass(
                            "javax.transaction.TransactionRequiredException" ).getConstructor(
                            new Class[] { String.class } ).newInstance(
                            new Object[] { message_pre + "TRANSACTION_REQUIRED"
                            + message_post } );
            }
            if ( ex instanceof org.omg.CORBA.TRANSACTION_ROLLEDBACK )
            {
                return ( java.rmi.RemoteException )
                      Thread.currentThread().getContextClassLoader().loadClass(
                            "javax.transaction.TransactionRolledbackException" ).getConstructor(
                            new Class[] { String.class } ).newInstance(
                            new Object[] { message_pre + "TRANSACTION_ROLLEDBACK"
                            + message_post } );

            }
            if ( ex instanceof org.omg.CORBA.INVALID_TRANSACTION )
            {
                return ( java.rmi.RemoteException )
                      Thread.currentThread().getContextClassLoader().loadClass(
                            "javax.transaction.InvalidTransactionException" ).getConstructor(
                            new Class[] { String.class } ).newInstance(
                            new Object[] { message_pre + "INVALID_TRANSACTION"
                            + message_post } );
            }
        }
        catch ( Exception ex1 )
        {
            throw ExceptionTool.initCause( new RuntimeException(
                  "Unable to instantiate an exception from the package javax.transaction!" ), ex1 );
        }

        if ( ex instanceof UnknownException )
        {
            UnknownException uex = ( UnknownException ) ex;
            if ( uex.originalEx instanceof java.lang.Error )
            {
                return new java.rmi.ServerError( message_pre + "UNKNOWN" + message_post,
                      ( Error ) uex.originalEx );
            }
            // the only exception (sic) to the rule, runtime exceptions get thrown.
            if ( uex.originalEx instanceof java.lang.RuntimeException )
            {
                throw ( RuntimeException ) uex.originalEx;
            }
            // I'm fairly sure the embedded remote exception is not unwrapped
            // into the server exception, since the remote exception may be a base
            // class.
            if ( uex.originalEx instanceof java.rmi.RemoteException )
            {
                return new java.rmi.ServerException( message_pre + "UNKNOWN" + message_post,
                      ( Exception ) uex.originalEx );
            }
            // we unwrap the exception here i'm sure...
            return new java.rmi.RemoteException( message_pre + "UNKNOWN" + message_post,
                  ( Exception ) uex.originalEx );
        }
        // remaining system exceptions.
        String name = ex.getClass().getName();
        name = name.substring( name.lastIndexOf( '.' ) + 1 );
        return new java.rmi.RemoteException( message_pre + name + message_post );
    }

    /**
     * The writeAny method writes the Java object obj to the output stream out in the
     * form of a GIOP any. The contents of the GIOP any are determined by applying the
     * Java to IDL mapping rules to the actual runtime type of obj. If obj is null, then it is
     * written as follows: the TypeCode is tk_abstract_interface, the repository ID is
     * IDL:omg.org/CORBA/AbstractBase:1.0, the name string is ???, and the
     * any's value is a null abstract interface type (encoded as a boolean discriminant of
     * false followed by along value of 0x00000000).
     * This method writes the java object obj to the output stream out in the form of a GIOP any.
     *
     * @param out The output stream to which the object is written.
     * @param obj The object to be written to the stream.
     */
    public void writeAny( final org.omg.CORBA.portable.OutputStream out, final Object obj )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "writeAny( " + out + ", " + obj + " )" );
        }

        // use the orb associated with the output stream if there is one.
        org.omg.CORBA.TypeCode tc = ( org.omg.CORBA.TypeCode )
              java.security.AccessController.doPrivileged( new java.security.PrivilegedAction()
              {
                  public Object run()
                  {
                      try
                      {
                          return lookupRuntimeTypeCode( obj );
                      }
                      catch ( InvalidClassException ex )
                      {
                          throw mapIOSysException( ex );
                      }
                  }
              } );

        // TODO: minor code.
        if ( tc == null )
        {
            throw new org.omg.CORBA.MARSHAL();
        }
        out.write_TypeCode( tc );
        switch ( tc.kind().value() )
        {
        case TCKind._tk_abstract_interface:
            ( ( OutputStream ) out ).write_abstract_interface( ( java.io.Serializable ) obj );
            break;

        case TCKind._tk_objref:
            writeRemoteObject( out, obj );
            break;

        case TCKind._tk_value:
        case TCKind._tk_value_box:
            ( ( OutputStream ) out ).write_value( ( java.io.Serializable ) obj );
            break;

        default:
            throw new org.omg.CORBA.MARSHAL();
        }
    }

    /**
     * The readAny method reads a GIOP any from the input stream in and unmarshals it
     * as a Java object, which is returned. The following TypeCodes are valid for the GIOP
     * any: tk_value, tk_value_box, tk_objref, and tk_abstract_interface. For each of
     * these types, both null and non-null values are valid. If the TypeCode is anything other
     * than these, a MARSHAL exception is thrown.
     *
     * @param in The input stream from which to read the object.
     * @return The object read from the stream.
     */
    public java.lang.Object readAny( org.omg.CORBA.portable.InputStream in )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "readAny( " + in + " )" );
        }

        // read the any.
        org.omg.CORBA.TypeCode tc = in.read_TypeCode();
        while ( tc.kind() == org.omg.CORBA.TCKind.tk_alias )
        {
            try
            {
                tc = tc.content_type();
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                throw new Error( "Should be impossible" );
            }
        }

        switch ( tc.kind().value() )
        {

        case TCKind._tk_abstract_interface:
            return ( ( InputStream ) in ).read_abstract_interface();

        case TCKind._tk_value_box:
            if ( tc.equal( org.omg.CORBA.WStringValueHelper.type() ) )
            {
                return ( ( InputStream ) in ).read_value( new org.omg.CORBA.WStringValueHelper() );
            }
            // fallthrough
        case TCKind._tk_value:
            return ( ( InputStream ) in ).read_value();

        case TCKind._tk_objref:
            return in.read_Object();

        default:
            throw new IllegalArgumentException( "Illegal any contents" );
        }
    }

    /**
     * The writeRemoteObject method is a utility method for use by stubs when writing
     * an RMI/IDL object reference to an output stream. If obj is a stub object,
     * writeRemoteObject simply writes obj to out.write_Object. However, if
     * obj is an exported RMI/IDL implementation object, then writeRemoteObject
     * allocates (or reuses) a suitable Tie (see Section 1.4.4, Allocating Ties for Remote
     * Values, on page 1-32), plugs together the tie with obj, and writes the object reference
     * for the tie to out.write_Object. This method cannot be used to write a JRMP
     * object reference to an output stream.
     *
     * @param out The output stream to which to write the object to.
     * @param obj The object to write to the stream.
     */
    public void writeRemoteObject( org.omg.CORBA.portable.OutputStream out,
                                   java.lang.Object obj )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "writeRemoteObject( " + out + ", " + obj + " )" );
        }

        if ( obj != null && obj instanceof java.rmi.Remote )
        {
            out.write_Object( exportRemote( out, ( java.rmi.Remote ) obj ) );
        }
        else if ( obj == null || obj instanceof org.omg.CORBA.Object )
        {
            out.write_Object( ( org.omg.CORBA.Object ) obj );
        }
        else
        {
            throw new IllegalArgumentException( "Not a remote object" );
        }
    }

    /**
     * The writeAbstractObject method is another similar utility method for use by
     * stubs. If obj is a value object, or a stub object, writeAbstractObject simply
     * writes obj to out.write_abstract_interface. However,ifobj is an
     * exported RMI/IDL implementation object, then writeAbstractObject allocates
     * (or reuses) a suitable Tie (see Section 1.4.4, Allocating Ties for Remote Values, on
     * page 1-32), plugs together the tie with obj, and writes the object reference for the tie
     * to the out.write_abstract_interface. This method cannot be used to write a
     * JRMP object reference to an output stream.
     *
     * @param out The output stream to which to write the obejct to.
     * @param obj The object to be written to the stream.
     */
    public void writeAbstractObject( org.omg.CORBA.portable.OutputStream out,
                                     java.lang.Object obj )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "writeAbstractObject( " + out + ", " + obj + " )" );
        }

        if ( obj != null && obj instanceof java.rmi.Remote )
        {
            obj = exportRemote( out, ( java.rmi.Remote ) obj );
        }
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) out ).write_abstract_interface( obj );
    }

    /**
     * The registerTarget method is needed to support unexportObject. Because
     * unexportObject takes a target implementation object as its parameter, it is
     * necessary for the Util class to maintain a table mapping target objects back to their
     * associated Ties. It is the responsibility of the code that allocates a Tie to also call the
     * registerTarget method to notify the Util class of the target object for a given
     * tie. The registerTarget method will call the Tie.setTarget method to notify
     * the tie object of its target object.
     *
     * @param tie The tie that is connected to the target object.
     * @param target The target object.
     */
    public void registerTarget( Tie tie, java.rmi.Remote target )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "registerTarget( " + tie + ", " + target + " )" );
        }

        Object key = new IdentityKey( target );
        // locate orb for tie class.
        org.omg.CORBA.ORB orb = tie.orb();
        if ( orb == null )
        {
            throw new IllegalStateException( "Tie class is not connected to orb" );
        }
        synchronized ( s_exported_objects )
        {
            if ( s_exported_objects.get( key ) != null )
            {
                return;
            }
            boolean isPOA = tie instanceof org.omg.PortableServer.Servant;
            // start an orb daemon
            ORBDaemon orbKey = new ORBDaemon( orb );
            ORBDaemon daemon = ( ORBDaemon ) s_exported_objects.get( orbKey );
            if ( daemon == null )
            {
                daemon = orbKey;
                s_exported_objects.put( orbKey, orbKey );
                if ( isPOA )
                {
                    initPOA( orb );
                }
            }
            daemon.increment();
            if ( isPOA )
            {
                ( ( org.omg.CORBA_2_3.ORB ) orb ).set_delegate( tie );
            }
            // add a mapping for the target.
            s_exported_objects.put( key, tie );
        }
        // initialize the tie.
        tie.setTarget( target );
    }

    private void initPOA( org.omg.CORBA.ORB orb )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "initPOA( " + orb + " )" );
        }

        try
        {
            POA rootPOA = ( POA ) orb.resolve_initial_references( "RootPOA" );
            rootPOA.the_POAManager().activate();
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            throw new org.omg.CORBA.INITIALIZE( "POA not found" );
        }
        catch ( org.omg.PortableServer.POAManagerPackage.AdapterInactive ex )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "RootPOA has been deactivated" );
        }
    }

    /**
     * The unexportObject method deactivates an implementation object and removes its
     * associated Tie from the table maintained by the Util class. If the object is not
     * currently exported or could not be deactivated, a NoSuchObjectException is
     * thrown.
     *
     * @param target The target object to deactivate.
     */
    public void unexportObject( java.rmi.Remote target )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "unexportObject( " + target + " )" );
        }

        Object key = new IdentityKey( target );
        synchronized ( s_exported_objects )
        {
            Tie tie = ( Tie ) s_exported_objects.get( key );
            if ( tie == null )
            {
                // we should throw a NoSuchObjectException, but we can't.
                return;
            }
            s_exported_objects.remove( key );
            tie.setTarget( null );
            try
            {
                tie.deactivate();
            }
            catch ( java.lang.Exception ex )
            {
                // we should throw a NoSuchObjectException, but we can't.
                // If we caught an exception, the reason is probably because the object was
                // not registered into a POA. So, we just mask the problem.
            }
            Object orbKey = new IdentityKey( tie.orb() );
            ORBDaemon daemon = ( ORBDaemon ) s_exported_objects.get( orbKey );
            if ( daemon.decrement() )
            {
                s_exported_objects.remove( orbKey );
            }
        }
    }

    /**
     * This method return the Tie object for an implementation object target, or null if no Tie is
     * registered for the target object.
     *
     * @param target The target object to get the tie for.
     * @return The tie for the target object.
     */
    public Tie getTie( java.rmi.Remote target )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "getTie( " + target + " )" );
        }

        if ( target instanceof Stub )
        {
            return null;
        }
        if ( target instanceof Tie )
        {
            return ( Tie ) target;
        }
        Object key = new IdentityKey( target );
        synchronized ( s_exported_objects )
        {
            return ( Tie ) s_exported_objects.get( key );
        }
    }

    /**
     * This method returns a singleton instance of a class that implements
     * the ValueHandler interface.
     *
     * @return The created value handler implementation.
     */
    public javax.rmi.CORBA.ValueHandler createValueHandler()
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "createValueHandler()" );
        }

        return ValueHandlerImpl.createValueHandler( getLogger().getChildLogger( "vh" ) );
    }

    /**
     * The wrapException method wraps an exception thrown by an implementation
     * method. It returns the corresponding client-side exception. See Section 1.4.8.1,
     * Mapping of UnknownExceptionInfo Service Context, on page 1-35 for details.
     *
     * @param obj The throwable object to wrap into a RemoteException.
     * @return The RemoteException by which the Throwable is wrapped.
     */
    public java.rmi.RemoteException wrapException( Throwable obj )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "wrapException( " + obj + " )" );
        }

        if ( obj instanceof org.omg.CORBA.SystemException )
        {
            return mapSystemException( ( org.omg.CORBA.SystemException ) obj );
        }
        if ( obj instanceof java.lang.Error )
        {
            return new java.rmi.ServerError( "Error exception in Server", ( java.lang.Error ) obj );
        }
        if ( obj instanceof java.rmi.RemoteException )
        {
            return new java.rmi.ServerException( "RemoteException in Server" );
        }
        if ( obj instanceof java.lang.RuntimeException )
        {
            throw ( java.lang.RuntimeException ) obj;
        }
        throw new org.omg.CORBA.BAD_PARAM();
    }

    /**
     * The copyObject method is used by local stubs to copy an actual parameter, result
     * object, or exception.
     *
     * The copyObject and copyObjects methods ensure that remote call semantics are
     * observed for local calls. They observe copy semantics for value objects that are
     * equivalent to marshaling, and they handle remote objects correctly. Stubs must either
     * call these methods or generate inline code to provide equivalent semantics.
     *
     * @param obj The object to copy.
     * @param orb The ORB where the object is running on.
     * @return The copy of the object obj.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    public java.lang.Object copyObject( java.lang.Object obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "copyObject( " + obj + ", " + orb + " )" );
        }

        // do not make the costly copy operation for each parameter
        if ( !getCopyLocalObjects() )
        {
            return obj;
        }

        if ( obj == null )
        {
            return null;
        }

        java.lang.Class clz = obj.getClass();
        java.lang.Object new_obj = null;
        // check for immutable types
        if ( clz.isPrimitive() || obj instanceof String )
        {
            return obj;
        }
        // check for array types
        if ( clz.isArray() )
        {
            Class cmpt = clz.getComponentType();
            // check for immutable array types.
            if ( cmpt.isPrimitive() )
            {
                if ( cmpt.equals( Boolean.TYPE ) )
                {
                    return ( ( boolean[] ) obj ).clone();
                }
                if ( cmpt.equals( Character.TYPE ) )
                {
                    return ( ( char[] ) obj ).clone();
                }
                if ( cmpt.equals( Byte.TYPE ) )
                {
                    return ( ( byte[] ) obj ).clone();
                }
                if ( cmpt.equals( Short.TYPE ) )
                {
                    return ( ( short[] ) obj ).clone();
                }
                if ( cmpt.equals( Integer.TYPE ) )
                {
                    return ( ( int[] ) obj ).clone();
                }
                if ( cmpt.equals( Long.TYPE ) )
                {
                    return ( ( long[] ) obj ).clone();
                }
                if ( cmpt.equals( Float.TYPE ) )
                {
                    return ( ( float[] ) obj ).clone();
                }
                if ( cmpt.equals( Double.TYPE ) )
                {
                    return ( ( double[] ) obj ).clone();
                }
            }
            if ( cmpt.equals( String.class ) )
            {
                return ( ( String[] ) obj ).clone();
            }
            return copyObjects( ( Object [] ) obj, orb );
        }

        if ( obj instanceof org.omg.CORBA.Object )
        {
            try
            {
                ( ( org.omg.CORBA.portable.ObjectImpl ) obj )._get_delegate();
                return obj;
            }
            catch ( org.omg.CORBA.BAD_OPERATION ex )
            {
                orb.connect( ( org.omg.CORBA.Object ) obj );
                return obj;
            }
        }

        if ( obj instanceof java.rmi.Remote )
        {
            if ( obj instanceof Stub )
            {
                try
                {
                    ( ( Stub ) obj )._get_delegate();
                    return obj;
                }
                catch ( org.omg.CORBA.BAD_OPERATION ex )
                {
                    ( ( Stub ) obj ).connect( orb );
                    return obj;
                }
            }
            else
            {
                Tie tie = getTie( ( java.rmi.Remote ) obj );
                if ( tie == null )
                {
                    throw new org.omg.CORBA.INV_OBJREF( "Couldn get the tie from obj! " + obj );
                }
                if ( tie.orb() == null )
                {
                    tie.orb( orb );
                    Util.registerTarget( tie, ( java.rmi.Remote ) obj );
                }
                // return a stub here
                return javax.rmi.PortableRemoteObject.toStub( ( java.rmi.Remote ) obj );
            }
        }

        if ( obj instanceof org.omg.CORBA.SystemException )
        {
            try
            {
                if ( obj instanceof org.omg.CORBA.portable.UnknownException )
                {
                    //
                    // org.omg.CORBA.portable.UnknownException has no zero param ctor, so
                    // create explicitly.
                    //
                    new_obj = new org.omg.CORBA.portable.UnknownException(
                            ( ( org.omg.CORBA.portable.UnknownException ) obj ).originalEx );
                }
                else
                {
                    new_obj = clz.newInstance();
                }
                org.omg.CORBA.SystemException sys = ( org.omg.CORBA.SystemException ) new_obj;
                sys.completed = ( ( org.omg.CORBA.SystemException ) obj ).completed;
                sys.minor = ( ( org.omg.CORBA.SystemException ) obj ).minor;
                return new_obj;
            }
            catch ( java.lang.Exception ex )
            {
                throw new org.omg.CORBA.BAD_PARAM( "An exception occured while creating a "
                      + "CORBA SystemException from '" + clz.getName() + "': " + ex );
            }
        }

        // Serialisation
        if ( obj instanceof java.io.Serializable )
        {
            if ( obj instanceof org.omg.CORBA.portable.ValueBase )
            {
                // CORBA valuetype. Write to/from output stream.
                org.omg.IOP.Codec codec = initCodec( orb );
                org.omg.CORBA.Any any = orb.create_any();
                any.insert_Value( ( java.io.Serializable ) obj );
                try
                {
                    byte [] enc = codec.encode_value( any );
                    any = codec.decode_value( enc, orb.get_primitive_tc( TCKind.tk_value ) );
                }
                catch ( org.omg.CORBA.UserException ex )
                {
                    throw new Error( ex.toString() );
                }
                return any.extract_Value();
            }
            else
            {
                // RMI/IIOP valuetype. Write to/from object stream.
                try
                {
                    org.omg.CORBA_2_3.portable.OutputStream out =
                        ( org.omg.CORBA_2_3.portable.OutputStream ) orb.create_output_stream();
                    out.write_value( ( java.io.Serializable ) obj );
                    org.omg.CORBA_2_3.portable.InputStream in =
                        ( org.omg.CORBA_2_3.portable.InputStream ) out.create_input_stream();
                    return in.read_value();
                }
                catch ( ClassCastException ex )
                {
                    throw new java.rmi.MarshalException( "Exception occurred during copyObject()"
                          + " method: " + ex, new java.io.NotSerializableException() );
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    throw mapSystemException( ex );
                }
            }
        }
        throw new org.omg.CORBA.BAD_PARAM( "The object is not a of a type that is supported by the"
              + " marshaling engine of RMI-IIOP. The copyObject() operation failed." );
    }

    /**
     * The copyObjects method is used by local stubs to copy any
     * number of actual parameters, preserving sharing across parameters as necessary to
     * support RMI/IDL semantics. The actual parameter Object[] array holds the method
     * parameter objects that need to be copied, and the result Object[] array holds the
     * copied results.
     *
     * @param obj An array of objects to copy.
     * @param orb The orb on which the objects are running.
     * @return The copies of the objects obj.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    public java.lang.Object [] copyObjects( java.lang.Object [] obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "copyObjects( " + obj + ", " + orb + " )" );
        }

        if ( !getCopyLocalObjects() )
        {
            return obj;
        }

        if ( obj == null )
        {
            return obj;
        }

        Class cmpt = obj.getClass().getComponentType();
        if ( cmpt.equals( String.class ) )
        {
            return ( String[] ) ( ( String[] ) obj ).clone();
        }
        // search for the most derived interface from java.rmi.Remote
        if ( !cmpt.isInterface() || !java.rmi.Remote.class.isAssignableFrom( cmpt ) )
        {
            Class[] itfs = cmpt.getInterfaces();
            if ( itfs != null && itfs.length > 0 )
            {
                for ( int i = 0; i < itfs.length; i++ )
                {
                    if ( java.rmi.Remote.class.isAssignableFrom( itfs[ i ] ) )
                    {
                        cmpt = itfs[ i ];
                    }
                }
            }
        }
        Object [] ret = ( Object[] ) java.lang.reflect.Array.newInstance( cmpt, obj.length );
        if ( cmpt.isArray() )
        {
            for ( int i = 0; i < obj.length; i++ )
            {
                ret[ i ] = copyObjects( ( Object[] ) obj[ i ], orb );
            }
        }
        else
        {
            for ( int i = 0; i < obj.length; i++ )
            {
                ret[ i ] = copyObject( obj[ i ], orb );
            }
        }
        return ret;
    }

    /**
     * Create a codec. Used for copying valuetypes.
     */
    private org.omg.IOP.Codec initCodec( org.omg.CORBA.ORB orb )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "initCodec( " + orb + " )" );
        }

        org.omg.IOP.CodecFactory cf;
        try
        {
            cf = ( org.omg.IOP.CodecFactory ) orb.resolve_initial_references( "CodecFactory" );
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            throw new Error( ex.toString() );
        }
        try
        {
            return cf.create_codec( new org.omg.IOP.Encoding(
                    org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 2 ) );
        }
        catch ( org.omg.IOP.CodecFactoryPackage.UnknownEncoding ex )
        {
            throw new Error( ex.toString() );
        }
    }

    /**
     * This method has the same semantics as the ObjectImpl's _is_local() method, except
     * that it can throw a RemoteException.
     *
     * @param s The stub to check.
     * @return True if the object is a local object, false otherwise.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    public boolean isLocal( Stub s )
        throws java.rmi.RemoteException
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "isLocal( " + s + " )" );
        }

        // is the costly is_local() test prohibited
        if ( getProhibitIsLocalTest() )
        {
            // return whether the object is either local or remote
            return s_is_local_result;
        }

        // do the costly _is_local() call
        try
        {
            return s._is_local();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw mapSystemException( ex );
        }
    }

    /**
     * The getCodebase method returns the Java codebase for the Class object clz as a
     * space-separated list of URLs. See Section 1.4.9.2, Codebase Selection, on page 1-35
     * for details.
     *
     * @param clz The class for which to get the codebase.
     * @return A string separated list or URLs.
     */
    public String getCodebase( Class clz )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "getCodebase( " + clz + " )" );
        }

        return java.rmi.server.RMIClassLoader.getClassAnnotation( clz );
    }

    /**
     * The loadClass method loads a Java class object for the Java class name
     * className, using additional information passed in the remoteCodebase and
     * loader parameters. See Section 1.4.9.5, Codebase Usage, on page 1-37 for details.
     * This method also checks that the loaded class is compatable with the class argument.
     *
     * @param className The class to load.
     * @param remoteCodebase A list of URLs.
     * @param loadingContext The loading context.
     * @return The loaded class type.
     * @throws ClassNotFoundException When the class could not be loaded.
     */
    public Class loadClass( String className,
                            String remoteCodebase,
                            Class loadingContext )
        throws ClassNotFoundException
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "loadClass( " + className + ", " + remoteCodebase + ", "
                  + loadingContext + " )" );
        }

        try
        {
            Class ret = null;
            // 1. Find the first non-null ClassLoader on the call stack and attempt to
            //   load the class using this ClassLoader.
            try
            {
                ret = Thread.currentThread().getContextClassLoader().loadClass( className );
                if ( loadingContext == null || loadingContext.isAssignableFrom( ret ) )
                {
                    return ret;
                }
            }
            catch ( ClassNotFoundException ex )
            {
                if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                {
                    getLogger().debug( "Attempt (1) to load the class using the current Thread's"
                          + " context ClassLoader failed." );
                }
                // try other ways below...
            }

            // 2. If remoteCodebase is non-null and useCodebaseOnly is false, then call
            // java.rmi.server.RMIClassLoader.loadClass(remoteCodebase, className)
            if ( remoteCodebase != null && !s_local_codebase_only )
            {
                try
                {
                    ret = java.rmi.server.RMIClassLoader.loadClass( remoteCodebase, className );
                    if ( loadingContext == null || loadingContext.isAssignableFrom( ret ) )
                    {
                        return ret;
                    }
                }
                catch ( ClassNotFoundException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (2) to load the class via the RMIClassLoader"
                              + " failed as well." );
                    }
                    // try other ways below...
                }
                catch ( java.net.MalformedURLException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (2) to load the class via the RMIClassLoader"
                              + " failed as well." );
                    }
                    // try other ways below...
                }
            }

            // 3. If remoteCodebase is null or useCodebaseOnly is true, then call
            // java.rmi.server.RMIClassLoader.loadClass(null, className)
            try
            {
                ret = java.rmi.server.RMIClassLoader.loadClass( ( String ) null, className );
                if ( loadingContext.isAssignableFrom( ret ) )
                {
                    return ret;
                }
            }
            catch ( ClassNotFoundException ex )
            {
                if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                {
                    getLogger().debug( "Attempt (3) to load the class via the RMIClassLoader"
                          + " failed as well." );
                }
                // try other ways below...
            }
            catch ( java.net.MalformedURLException ex )
            {
                if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                {
                    getLogger().debug( "Attempt (3) to load the class via the RMIClassLoader"
                          + " failed as well." );
                }
                // try other ways below...
            }

            // 4. If a class was not successfully loaded by step 1, 2, or 3, and loader is non-null,
            // then call Class.forName(className, false, loader)
            if ( loadingContext != null )
            {
                ret = Class.forName( className, false, loadingContext.getClassLoader() );
                if ( loadingContext.isAssignableFrom( ret ) )
                {
                    return ret;
                }
            }
            if ( ret != null )
            {
                if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                {
                    getLogger().debug( "Attempt (4) to load the class via the system ClassLoader"
                          + " failed as well." );
                }
                throw new ClassNotFoundException(
                      "Loaded class was not compatible with desired class" );
            }
        }
        catch ( Throwable th )
        {
            getLogger().error( "An unexpected exception occured!", th );
        }
        throw new ClassNotFoundException( "Could not load class" );
    }

    /**
     * The loadClass method loads a Java class object for the Java class name
     * className, using additional information passed in the remoteCodebase and
     * loader parameters. See Section 1.4.9.5, Codebase Usage, on page 1-37 for details.
     *
     * @param className The class to load.
     * @param remoteCodebase A list of URLs.
     * @param loadingContext The loading context.
     * @return The loaded class type.
     * @throws ClassNotFoundException When the class could not be loaded.
     */
    public Class loadClass( String className,
                            String remoteCodebase,
                            ClassLoader loadingContext )
        throws ClassNotFoundException
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "loadClass( " + className + ", " + remoteCodebase + ", "
                  + loadingContext + " )" );
        }

        try
        {
            // 1. Find the first non-null ClassLoader on the call stack and attempt to
            //    load the class using this ClassLoader.
            // TODO: The JDK uses a native method to loop through the call stack
            //     It gets each method, the class the method is defined for, and the
            //     corresponding class loader. If a non-null class loader is found then
            //     this instance is returned, otherwise NULL is returned.
            //  It seems as if this implementation is not correct.
            //  Sun JDK: Uses JDKClassLoader makes the ObjectInputStream's private native
            //     method latestUserDefinedLoader accessible via reflection.
            //  IBM JDK: ???
            try
            {
                return Class.forName( className, false, null );
            }
            catch ( ClassNotFoundException ex )
            {
                if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                {
                    getLogger().debug( "Attempt (1) to load the class using the current Thread's"
                          + " context ClassLoader failed." );
                }
                // try other ways below...
            }

            // 2. If remoteCodebase is non-null and useCodebaseOnly is false, then call
            // java.rmi.server.RMIClassLoader.loadClass(remoteCodebase, className)
            if ( remoteCodebase != null && !s_local_codebase_only )
            {
                try
                {
                    return java.rmi.server.RMIClassLoader.loadClass( remoteCodebase, className );
                }
                catch ( ClassNotFoundException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (2) to load the class via the RMIClassLoader"
                              + " failed as well." );
                    }
                    // try other ways below...
                }
                catch ( java.net.MalformedURLException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (2) to load the class via the RMIClassLoader"
                              + " failed as well." );
                    }
                    // try other ways below...
                }
            }

            // 3. If remoteCodebase is null or localCodebaseOnly is false, then call
            // java.rmi.server.RMIClassLoader.loadClass(null, className)
            if ( !s_local_codebase_only )
            {
                try
                {
                    return java.rmi.server.RMIClassLoader.loadClass( ( String ) null, className );
                }
                catch ( ClassNotFoundException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (3) to load the class via the RMIClassLoader"
                               + " failed as well." );
                    }
                    // try other ways below...
                }
                catch ( java.net.MalformedURLException ex )
                {
                    if ( getLogger().isDebugEnabled() && Trace.isMedium() )
                    {
                        getLogger().debug( "Attempt (3) to load the class via the RMIClassLoader"
                              + " failed as well." );
                    }
                    // try other ways below...
                }
            }

            // 4. If a class was not successfully loaded by step 1, 2, or 3, and loader is non-null,
            // then call Class.forName(className, false, loader)
            if ( loadingContext != null )
            {
                return Class.forName( className, false, loadingContext );
            }
            if ( getLogger().isDebugEnabled() && Trace.isMedium() )
            {
                getLogger().debug( "Attempt (4) to load the class via the system ClassLoader"
                      + " failed as well." );
            }
        }
        catch ( Throwable th )
        {
            getLogger().error( "An unexpected exception occured!", th );
        }
        throw new ClassNotFoundException( "Couldn't load class \'" + className + "\'!" );
    }

    // --------------------------------------------------------------------------------
    // OpenORB specificities

    /**
     * This function maps an IOException into a special CORBA system exception
     * which can be successfully unmapped back to an IOException and will be.
     *
     * @param ex The IOException to map.
     * @return The corresponding CORBA system exception.
     */
    static org.omg.CORBA.SystemException mapIOSysException( IOException ex )
    {
        if ( ex instanceof CORBAIOException )
        {
            return ( ( CORBAIOException ) ex ).getTargetException();
        }
        // TODO: find appropriate minor codes.
        return new org.omg.CORBA.MARSHAL( ex.toString() );
    }

    /**
     * Map a system exception into an IOException.
     *
     * @param ex The CORBA system exception to map.
     * @return The IOException that has been wrapped by the CORBA system exception.
     */
    static IOException mapSysIOException( org.omg.CORBA.SystemException ex )
    {
        if ( ex instanceof UnknownException )
        {
            Throwable target = ( ( UnknownException ) ex ).originalEx;
            if ( target instanceof IOException )
            {
                return ( IOException ) target;
            }
        }
        return new CORBAIOException( ex );
    }

    /**
     * Locate an IDLEntity helper class.
     *
     * @param clz The class for which to load the helper class for.
     * @return The loaded helper class for clz.
     */
    static Class locateHelperClass( Class clz )
    {
        while ( org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom( clz ) )
        {
            try
            {
                return Util.loadClass( clz.getName() + "Helper", Util.getCodebase( clz ),
                        clz.getClassLoader() );
            }
            catch ( ClassNotFoundException ex )
            {
                clz = clz.getSuperclass();
            }
        }
        return null;
    }

    private org.omg.CORBA.ORB getORB( org.omg.CORBA.portable.OutputStream out )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "getORB( " + out + " )" );
        }

        if ( out instanceof CDROutputStream )
        {
            return ( ( CDROutputStream ) out ).orb();
        }
        else
        {
            return DefaultORB.getORB();
        }
    }

    static org.omg.CORBA.Object exportRemote( org.omg.CORBA.portable.OutputStream out,
            java.rmi.Remote obj )
    {
        if ( s_instance.getLogger().isDebugEnabled() && Trace.isLow() )
        {
            s_instance.getLogger().debug( "exportRemote( " + out + ", " + obj + " )" );
        }

        try
        {
            if ( obj instanceof Stub )
            {
                Stub stub = ( Stub ) obj;
                try
                {
                    stub._get_delegate();
                }
                catch ( org.omg.CORBA.BAD_OPERATION ex )
                {
                    stub.connect( s_instance.getORB( out ) );
                }
                return stub;
            }
            Tie tie = s_instance.getTie( obj );
            if ( tie == null )
            {
                tie = PortableRemoteObjectDelegateImpl.loadTie( obj );
                tie.orb( s_instance.getORB( out ) );
                s_instance.registerTarget( tie, obj );
            }
            return tie.thisObject();
        }
        catch ( IOException ex )
        {
            throw mapIOSysException( ex );
        }
    }

    /**
     * Create typecode from runtime class. This will only successfully return
     * typecodes for types which can be marshalled.
     *
     * @param obj The object for which to look the type code up.
     * @return the target object's typecode, or null if the target object is
     *    not serializable.
     * @throws java.io.InvalidClassException When the type() method could not be invoked
     * on the helper class.
     */
    public static org.omg.CORBA.TypeCode lookupRuntimeTypeCode( Object obj )
        throws java.io.InvalidClassException
    {
        if ( obj == null )
        {
            return s_orb.create_abstract_interface_tc( "IDL:omg.org/CORBA/AbstractBase:1.0", "" );
        }
        if ( obj instanceof org.omg.CORBA.Object )
        {
            if ( obj instanceof org.omg.CORBA.portable.ObjectImpl )
            {
                org.omg.CORBA.portable.ObjectImpl impl = ( org.omg.CORBA.portable.ObjectImpl ) obj;
                String [] ids = impl._ids();
                if ( ids.length > 0 )
                {
                    return s_orb.create_interface_tc( ids[ 0 ], "" );
                }
            }
            return s_orb.get_primitive_tc( TCKind.tk_objref );
        }
        if ( obj instanceof java.rmi.Remote )
        {
            RMIRemoteStreamClass rsc = RMIRemoteStreamClass.lookup( obj.getClass() );
            if ( rsc == null )
            {
                return null;
            }
            return rsc.getInterfaceTypesNoCopy() [ 0 ];
        }
        if ( !( obj instanceof java.io.Serializable ) )
        {
            return null;
        }
        if ( obj instanceof org.omg.CORBA.portable.ValueBase )
        {
            if ( obj instanceof org.omg.CORBA.portable.Streamable )
            {
                return ( ( org.omg.CORBA.portable.Streamable ) obj )._type();
            }
            Class helper = locateHelperClass( obj.getClass() );
            try
            {
                return ( org.omg.CORBA.TypeCode )
                        helper.getMethod( "type", new Class[ 0 ] ).invoke( null, new Object[ 0 ] );
            }
            catch ( InvocationTargetException ex )
            {
                Throwable real = ex.getTargetException();
                if ( real instanceof RuntimeException )
                {
                    throw ( RuntimeException ) real;
                }
                if ( real instanceof Error )
                {
                    throw ( Error ) real;
                }
                throw new InvalidClassException( helper.getName(), real.toString() );
            }
            catch ( IllegalAccessException ex )
            {
                throw new InvalidClassException( helper.getName(), "IllegalAccessException" );
            }
            catch ( NoSuchMethodException ex )
            {
                throw new InvalidClassException( helper.getName(), "NoSuchMethodException" );
            }
        }
        RMIObjectStreamClass osc = RMIObjectStreamClass.lookup( obj.getClass() );
        return osc.type();
    }

    /**
     * Get the isLocalResult flag.
     *
     * @return The value of the flag.
     */
    public static boolean getIsLocalResult()
    {
        return s_is_local_result;
    }

    /**
     * Set the isLocalResult flag.
     *
     * @param is_local_result The new value of the flag.
     */
    public static void setIsLocalResult( boolean is_local_result )
    {
        s_is_local_result = is_local_result;
    }

    /**
     * Get the prohibitIsLocalTest flag.
     *
     * @return The value of the flag.
     */
    public static boolean getProhibitIsLocalTest()
    {
        return s_prohibit_is_local_test;
    }

    /**
     * Set the prohibitIsLocalTest flag.
     *
     * @param prohibit_is_local_test The new value of the flag.
     */
    public static void setProhibitIsLocalTest( boolean prohibit_is_local_test )
    {
        s_prohibit_is_local_test = prohibit_is_local_test;
    }

    /**
     * Get the RMIClassloader flag.
     *
     * @return The value of the flag.
     */
    public static boolean getLocalCodebaseOnly()
    {
        return s_local_codebase_only;
    }

    /**
     * Set the RMIClassloader flag.
     *
     * @param local_codebase_only The new value of the flag.
     */
    public static void setLocalCodebaseOnly( boolean local_codebase_only )
    {
        s_local_codebase_only = local_codebase_only;
    }

    /**
     * Get the copyLocalObjects flag.
     *
     * @return The value of the flag.
     */
    public static boolean getCopyLocalObjects()
    {
        return s_copy_local_objects;
    }

    /**
     * Set the copyLocalObjects flag.
     *
     * @param copy_local_objects The new value of the flag.
     */
    public static void setCopyLocalObjects( boolean copy_local_objects )
    {
        s_copy_local_objects = copy_local_objects;
    }

    /**
     * This class is used to ensure the target orb does not get garbage
     * collected while there are RMI/IIOP objects exported.
     */
    private static class ORBDaemon extends IdentityKey implements Runnable
    {
        private Thread m_thread = null;
        private org.omg.CORBA.ORB m_orb;
        private int m_count = 0;

        ORBDaemon( org.omg.CORBA.ORB orb )
        {
            super( orb );
            m_orb = orb;
        }

        public void run()
        {
            m_orb.run();
        }

        /**
         * Increment the use count. Starts an orb.run() thread if
         * no objects were previously exported.
         */
        void increment()
        {
            if ( m_count++ == 0 )
            {
                m_thread = new Thread( this, "ORBDaemon [" + m_orb + "]" );
                m_thread.setDaemon( true );
                m_thread.start();
            }
        }

        /**
         * Returns true when the use count hits 0.
         */
        boolean decrement()
        {
            if ( --m_count <= 0 )
            {
                m_thread.interrupt();
                return true;
            }
            return false;
        }
    }
}

