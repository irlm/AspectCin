/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.HashMap;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import org.openorb.orb.util.Trace;

/**
 * This class should be used as the helper for all system exceptions.
 *
 * @author Chris Wood
 */
public abstract class SystemExceptionHelper
{
    private static final String [] SYSTEM_EXCEPTIONS =
            {
                "UNKNOWN",
                "BAD_PARAM",
                "NO_MEMORY",
                "IMP_LIMIT",
                "COMM_FAILURE",
                "INV_OBJREF",
                "NO_PERMISSION",
                "INTERNAL",
                "MARSHAL",
                "INITIALIZE",
                "NO_IMPLEMENT",
                "BAD_TYPECODE",
                "BAD_OPERATION",
                "NO_RESOURCES",
                "NO_RESPONSE",
                "PERSIST_STORE",
                "BAD_INV_ORDER",
                "TRANSIENT",
                "FREE_MEM",
                "INV_IDENT",
                "INV_FLAG",
                "INTF_REPOS",
                "BAD_CONTEXT",
                "OBJ_ADAPTER",
                "DATA_CONVERSION",
                "OBJECT_NOT_EXIST",
                "TRANSACTION_REQUIRED",
                "TRANSACTION_ROLLEDBACK",
                "INVALID_TRANSACTION",
                "INV_POLICY",
                "CODESET_INCOMPATIBLE",
                "REBIND",
                "TIMEOUT",
                "TRANSACTION_UNAVAILABLE",
                "TRANSACTION_MODE",
                "BAD_QOS"
            };

    private static final Map FINDER = new HashMap( SYSTEM_EXCEPTIONS.length * 2 );
    private static final MapEntry UNKNOWN;

    private static class MapEntry
    {
        public MapEntry( String id, TypeCode type, Class clz, Class hclz, Constructor strCons )
        {
            m_id      = id;
            m_type    = type;
            m_clz     = clz;
            m_hClz    = hclz;
            m_strCons = strCons;
        }

        private final String      m_id;
        private final TypeCode    m_type;
        private final Class       m_clz;
        private final Class       m_hClz;
        private final Constructor m_strCons;
    }

    static
    {
        final Thread currentThread = Thread.currentThread();
        ClassLoader ctxClassLoader = currentThread.getContextClassLoader();
        ClassLoader originalClassLoader = ctxClassLoader;

        if ( ctxClassLoader == null )
        {
            // this may happen in the finalizer thread. The generated code
            // for Exception helper classes also use the current thread's
            // contextclassloader so we need to set that classloader
            // temporarily to avoid NullPointerExceptions there as well
            ctxClassLoader = SystemExceptionHelper.class.getClassLoader();
            currentThread.setContextClassLoader( ctxClassLoader );
        }
        try
        {
            try
            {
                UNKNOWN = new MapEntry( org.omg.CORBA.UNKNOWNHelper.id(),
                    org.omg.CORBA.UNKNOWNHelper.type(),
                    org.omg.CORBA.UNKNOWN.class, org.omg.CORBA.UNKNOWNHelper.class,
                    org.omg.CORBA.UNKNOWN.class.getConstructor( new Class[] { String.class } ) );
                FINDER.put( UNKNOWN.m_id, UNKNOWN );
                FINDER.put( UNKNOWN.m_clz, UNKNOWN );
            }
            catch ( final NoSuchMethodException ex )
            {
                throw Trace.signalIllegalCondition( null,
                    "Unable to find String constructor of "
                    + "org.omg.CORBA.UNKNOWN class (" + ex + ")" );
            }

            for ( int i = 1; i < SYSTEM_EXCEPTIONS.length; ++i )
            {
                try
                {
                    Class clz;
                    Class hclz;
                    try
                    {
                        clz = ctxClassLoader.loadClass(
                            "org.omg.CORBA." + SYSTEM_EXCEPTIONS[ i ] );
                        hclz = ctxClassLoader.loadClass(
                            "org.omg.CORBA." + SYSTEM_EXCEPTIONS[ i ] + "Helper" );
                    }
                    catch ( ClassNotFoundException ex )
                    {
                        System.err.println( "Warning: missing system exception class "
                            + SYSTEM_EXCEPTIONS[ i ] );
                        continue;
                    }
                    String id = ( String ) hclz.getMethod( "id", new Class[ 0 ] ).invoke(
                        null, new Object[ 0 ] );
                    TypeCode type = ( TypeCode ) hclz.getMethod( "type", new Class[ 0 ] ).invoke(
                        null, new Object[ 0 ] );
                    Constructor strCons = clz.getConstructor( new Class[] { String.class } );
                    MapEntry entry = new MapEntry( id, type, clz, hclz, strCons );
                    FINDER.put( entry.m_id, entry );
                    FINDER.put( entry.m_clz, entry );
                }
                catch ( RuntimeException ex )
                {
                    throw ex;
                }
                catch ( InvocationTargetException ex )
                {
                    throw Trace.signalIllegalCondition( null,
                        "An exception occured while invoking the 'id' or the 'type' method in"
                        + " 'org.omg.CORBA." + SYSTEM_EXCEPTIONS[ i ] + "'!" );
                }
                catch ( NoSuchMethodException ex )
                {
                    throw Trace.signalIllegalCondition( null,
                        "Unable to find 'id' or 'type' method in 'org.omg.CORBA."
                        + SYSTEM_EXCEPTIONS[ i ] + "'!" );
                }
                catch ( IllegalAccessException ex )
                {
                    throw Trace.signalIllegalCondition( null,
                        "Unable to access 'id' or 'type' method in 'org.omg.CORBA."
                        + SYSTEM_EXCEPTIONS[ i ] + "'!" );
                }
            }
        }
        finally
        {
            currentThread.setContextClassLoader( originalClassLoader );
        }
    }

    /**
     * Utility class, do not instantiate.
     */
    private SystemExceptionHelper()
    {
    }

    /**
     * Get a typecode from a repository ID.
     * @param repo_id Repository ID of the system exception.
     * @return the required typecode, or typecode of UNKNOWN for an unknown
     *          repository ID.
     */
    public static TypeCode type( String repo_id )
    {
        MapEntry entry = ( MapEntry ) FINDER.get( repo_id );
        if ( entry == null )
        {
            entry = UNKNOWN;
        }
        return entry.m_type;
    }

    /**
     * Get a typecode from an exception.
     * @param ex The exception to get the typecode of.
     * @return the required typecode, or typecode of UNKNOWN for an unknown
     *          exception type.
     */
    public static TypeCode type( SystemException ex )
    {
        MapEntry entry = ( MapEntry ) FINDER.get( ex.getClass() );
        if ( entry == null )
        {
            entry = UNKNOWN;
        }
        return entry.m_type;
    }

    /**
     * Get the repository ID from an exception.
     * @param ex the system exception.
     * @return the repository ID of the system exception, or UNKNOWN for an
     *      unknown exception.
     */
    public static String id( SystemException ex )
    {
        MapEntry entry = ( MapEntry ) FINDER.get( ex.getClass() );
        if ( entry == null )
        {
            entry = UNKNOWN;
        }
        return entry.m_id;
    }

    /**
     * Insert a system exception into an any. If the exception type is unknown
     * an unknown exception will be inserted into the any. UnknownExceptions can
     * be successfully inserted/extracted from any types.
     *
     * @param any the any to get inserted into.
     * @param sysex the system exception to insert into the any.
     */
    public static void insert( Any any, SystemException sysex )
    {
        MapEntry entry = ( MapEntry ) FINDER.get( sysex.getClass() );
        Logger logger = ( ( org.openorb.orb.core.Any ) any ).getLogger();
        if ( entry == null )
        {
            if ( sysex instanceof org.omg.CORBA.portable.UnknownException
                  && any instanceof org.openorb.orb.core.Any )
            {
                ( ( org.openorb.orb.core.Any ) any ).setUnknownException( sysex );
            }
            sysex = new org.omg.CORBA.UNKNOWN( sysex.minor, sysex.completed );
            entry = UNKNOWN;
        }
        Class [] arg_clz = new Class[ 2 ];
        Object [] args = new Object[ 2 ];
        arg_clz[ 0 ] = Any.class;
        args[ 0 ] = any;
        arg_clz[ 1 ] = sysex.getClass();
        args[ 1 ] = sysex;
        try
        {
            entry.m_hClz.getMethod( "insert", arg_clz ).invoke( null, args );
            return;
        }
        catch ( java.lang.reflect.InvocationTargetException ex )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( "Invalid insert operation", ex );
            }
            Throwable tex = ex.getTargetException();

            if ( tex instanceof java.lang.RuntimeException )
            {
                throw ( java.lang.RuntimeException ) tex;
            }
            if ( tex instanceof java.lang.Error )
            {
                throw ( java.lang.Error ) tex;
            }
        }
        catch ( RuntimeException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( "Unexpected exception", ex );
            }
        }
    }

    /**
     * Extract a system exception from an any. UnknownExceptions can
     * be successfully inserted/extracted from any types.
     *
     * @param any the any to extract from.
     * @throws org.omg.CORBA.BAD_OPERATION the any does not contain a system exception.
     */
    public static SystemException extract( Any any )
    {
        MapEntry entry = null;
        Logger logger = ( ( org.openorb.orb.core.Any ) any ).getLogger();
        try
        {
            entry = ( MapEntry ) FINDER.get( any.type().id() );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( "Unknown TCKind", ex );
            }
        }
        if ( entry == null )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        Class [] arg_clz = new Class[ 1 ];
        Object [] args = new Object[ 1 ];
        arg_clz[ 0 ] = Any.class;
        args[ 0 ] = any;
        SystemException ret;
        try
        {
            ret = ( SystemException ) entry.m_hClz.getMethod( "extract",
                  arg_clz ).invoke( null, args );
            if ( ret instanceof org.omg.CORBA.UNKNOWN && any instanceof org.openorb.orb.core.Any )
            {
                SystemException tmp = new org.omg.CORBA.portable.UnknownException(
                      ( ( org.openorb.orb.core.Any ) any ).getUnknownException() );
                tmp.minor = ret.minor;
                tmp.completed = ret.completed;
                ret = tmp;
            }
            return ret;
        }
        catch ( java.lang.reflect.InvocationTargetException ex )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( "Invalid extract operation", ex );
            }
            Throwable tex = ex.getTargetException();

            if ( tex instanceof java.lang.RuntimeException )
            {
                throw ( java.lang.RuntimeException ) tex;
            }
            if ( tex instanceof java.lang.Error )
            {
                throw ( java.lang.Error ) tex;
            }
        }
        catch ( RuntimeException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( "Unexpected exception", ex );
            }
        }
        throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
              MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Read a system exception from an input stream.
     */
    public static SystemException read( InputStream is )
    {
        return create( is.read_string(), null, is.read_ulong(),
              CompletionStatus.from_int( is.read_ulong() ) );
    }

    /**
     * Read a system exception from an input stream.
     * @param reasonPrefix prefix to add to the reason string.
     */
    public static SystemException read( String reasonPrefix, InputStream is )
    {
        return create( is.read_string(), reasonPrefix, is.read_ulong(),
              CompletionStatus.from_int( is.read_ulong() ) );
    }

    /**
     * Create a system exception from it's repository ID, minor value and
     * completion status.
     */
    public static SystemException create( String repo_id, int minor, CompletionStatus completed )
    {
        return create( repo_id, null, minor, completed );
    }

    /**
     * Create a system exception from it's repository ID, minor value and
     * completion status.
     *
     * @param reasonPrefix prefix to add to the reason string.
     */
    public static SystemException create( String repo_id,
          String reasonPrefix, int minor, CompletionStatus completed )
    {
        MapEntry entry = ( MapEntry ) FINDER.get( repo_id );
        if ( entry == null )
        {
            entry = UNKNOWN;
        }
        String reason = getReasonString( entry, minor );
        if ( reasonPrefix != null )
        {
            reason = reasonPrefix + ": " + reason;
        }
        try
        {
            SystemException ret = ( SystemException )
                  entry.m_strCons.newInstance( new Object[] {reason} );
            ret.minor = minor;
            ret.completed = completed;
            return ret;
        }
        catch ( RuntimeException ex )
        {
            throw ex;
        }
        catch ( Throwable ex )
        {
            return null;
        }
    }

    /**
     * Write a system excpetion to an output stream.
     */
    public static void write( OutputStream out, SystemException val )
    {
        out.write_string( id( val ) );
        out.write_ulong( val.minor );
        out.write_ulong( val.completed.value() );
    }

    private static String getReasonString( MapEntry entry, int minor )
    {
        int vendorID  = minor & 0xFFFFF000;    // 20 bits VMCID
        int minorCode = minor & 0xFFF;         // 12 bits

        switch ( vendorID )
        {

        case org.omg.CORBA.OMGVMCID.value:
            return "OMG Minor Code: " + minorCode;

        case 0x444f7000:  // org.openorb.orb.policy.OPENORB_VPVID.value:
            return "OpenORB Minor Code: " + minorCode;

        case 0:
            return "Unregistered vendor Minor Code: " + minorCode;

        case 0x4f4f0000:
            return "ORBacus Minor Code: " + minorCode;

        case 0x4A430000:
            return "JacORB Minor Code: " + minorCode;

        case 0x53550000:  // com.sun.corba.se.internal.util.SUNVMCID
            return "Sun ORB Minor Code: " + minorCode;

        default:
            return "Unknown vendor (0x" + Integer.toString( vendorID, 16 )
                   + ")  Minor Code: " + minorCode;
        }
    }
}

