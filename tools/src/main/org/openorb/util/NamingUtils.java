/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Array;

import java.util.HashMap;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.ORB;

/**
 * The functions in this class perform various translations on stringified
 * CosNaming names.
 *
 * @author Chris Wood
 * @author Michael Rumpf
 */
public abstract class NamingUtils
{
    /** RootPOA initial reference name */
    public static final String IR_ROOT_POA = "RootPOA";

    /** POACurrent initial reference name */
    public static final String IR_POA_CURRENT = "POACurrent";

    /** InterfaceRepository initial reference name */
    public static final String IR_INTERFACE_REPOSITORY = "InterfaceRepository";

    /** NameService initial reference name */
    public static final String IR_NAME_SERVICE = "NameService";

    /** TradingService initial reference name */
    public static final String IR_TRADING_SERVICE = "TradingService";

    /** SecurityCurrent initial reference name */
    public static final String IR_SECURITY_CURRENT = "SecurityCurrent";

    /** TransactionCurrent initial reference name */
    public static final String IR_TRANSACTION_CURRENT = "TransactionCurrent";

    /** DynAnyFactory initial reference name */
    public static final String IR_DYN_ANY_FACTORY = "DynAnyFactory";

    /** ORBPolicyManager initial reference name */
    public static final String IR_ORB_POLICY_MANAGER = "ORBPolicyManager";

    /** PolicyCurrent initial reference name */
    public static final String IR_POLICY_CURRENT = "PolicyCurrent";

    /** NotificationService initial reference name */
    public static final String IR_NOTIFICATION_SERVICE = "NotificationService";

    /** TypedNotificationService initial reference name */
    public static final String IR_TYPED_NOTIFICATION_SERVICE = "TypedNotificationService";

    /** CodecFactory initial reference name */
    public static final String IR_CODEC_FACTORY = "CodecFactory";

    /** PICurrent initial reference name */
    public static final String IR_PI_CURRENT = "PICurrent";

    /** ComponentHomeFinder initial reference name */
    public static final String IR_COMPONENT_HOME_FINDER = "ComponentHomeFinder";

    /** PSS initial reference name */
    public static final String IR_PSS = "PSS";

    /** CCS initial reference name (non-standard) */
    public static final String IR_CCS = "ConcurrencyControlService";

    /** OTS initial reference name (non-standard) */
    public static final String IR_OTS = "TransactionService";

    /** EventService initial reference name (non-standard) */
    public static final String IR_EVENT_SERVICE = "EventService";

    /** PropertyService initial reference name (non-standard) */
    public static final String IR_PROPERTY_SERVICE = "PropertyService";

    /** Trader ServiceTypeRepository initial reference name (non-standard) */
    public static final String IR_SERVICE_TYPE_REPOSITORY = "ServiceTypeRepository";

    /** TimeService initial reference name (non-standard) */
    public static final String IR_TIME_SERVICE = "TimeService";


    //
    // Bootstrap
    //

    /** NamingService temp folder name. */
    private static final String NS_IOR_DIRNAME   = "NS_IOR";


    /** Top-level service context */
    public static final String ROOT_COS_CONTEXT             = "COS";

    /* NamingService */
    public static final String NS_NAME                          = "ns";
    public static final String NS_NAME_LONG                     = "NameService";
    public static final String NAMING_CONTEXT_NAME              = "NamingContextExt";
    public static final String NAMING_CONTEXT_NAME_NS           =
        ROOT_COS_CONTEXT + "/" + NS_NAME_LONG + "/" + NAMING_CONTEXT_NAME;
    public static final String CALLBACK_MANAGER_NAME            = "CallbackManager";
    public static final String CALLBACK_MANAGER_NAME_NS         =
        ROOT_COS_CONTEXT + "/" + NS_NAME_LONG + "/" + CALLBACK_MANAGER_NAME;

    /* ConcurrencyControlService */
    public static final String CCS_NAME                         = "ccs";
    public static final String CCS_NAME_LONG                    = "ConcurrencyControlService";
    public static final String LOCK_SET_FACTORY_NAME            = "LockSetFactory";
    public static final String LOCK_SET_FACTORY_NAME_NS         =
        ROOT_COS_CONTEXT + "/" + CCS_NAME_LONG + "/" + LOCK_SET_FACTORY_NAME;

    /* EventService */
    public static final String EVENT_SERVICE_NAME               = "event";
    public static final String EVENT_SERVICE_NAME_LONG          = "EventService";
    public static final String EVENT_CHANNEL_FACTORY_NAME       = "EventChannelFactory";
    public static final String EVENT_CHANNEL_FACTORY_NAME_NS    =
        ROOT_COS_CONTEXT + "/" + EVENT_SERVICE_NAME_LONG + "/" + EVENT_CHANNEL_FACTORY_NAME;
    public static final String DEFAULT_EVENT_CHANNEL_NAME       = "DefaultEventChannel";
    public static final String DEFAULT_EVENT_CHANNEL_NAME_NS    =
        ROOT_COS_CONTEXT + "/" + EVENT_SERVICE_NAME_LONG + "/" + DEFAULT_EVENT_CHANNEL_NAME;

    /* NotificationService */
    public static final String NOTIFICATION_SERVICE_NAME        = "notify";
    public static final String NOTIFICATION_SERVICE_NAME_LONG   = "NotificationService";
    public static final String NOTIFICATION_CHANNEL_FACTORY_NAME = "EventChannelFactory";
    public static final String NOTIFICATION_CHANNEL_FACTORY_NAME_NS =
        ROOT_COS_CONTEXT + "/" + NOTIFICATION_SERVICE_NAME_LONG + "/"
            + NOTIFICATION_CHANNEL_FACTORY_NAME;

    /* PSS */
    public static final String PSS_SERVICE_NAME                 = "pss";
    public static final String PSS_SERVICE_NAME_LONG            = "PersistentStateService";
    public static final String PSS_CONNECTOR_FACTORY_NAME       = "ConnectorFactory";
    public static final String PSS_CONNECTOR_FACTORY_NAME_NS    =
        ROOT_COS_CONTEXT + "/" + PSS_SERVICE_NAME_LONG + "/" + PSS_CONNECTOR_FACTORY_NAME;

    /* PropertyService */
    public static final String PROPERTY_SERVICE_NAME            = "property";
    public static final String PROPERTY_SERVICE_NAME_LONG       = "PropertyService";
    public static final String PROPERTY_SET_FACTORY_NAME        = "PropertySetFactory";
    public static final String PROPERTY_SET_FACTORY_NAME_NS     =
        ROOT_COS_CONTEXT + "/" + PROPERTY_SERVICE_NAME_LONG + "/" + PROPERTY_SET_FACTORY_NAME;
    public static final String PROPERTY_SET_DEF_FACTORY_NAME    = "PropertySetDefaultFactory";
    public static final String PROPERTY_SET_DEF_FACTORY_NAME_NS =
        ROOT_COS_CONTEXT + "/" + PROPERTY_SERVICE_NAME_LONG + "/" + PROPERTY_SET_DEF_FACTORY_NAME;

    /* TimeService */
    public static final String TIME_SERVICE_NAME                = "time";
    public static final String TIME_SERVICE_NAME_LONG           = "TimeService";
    public static final String TIME_SERVICE_NAME_NS             =
        ROOT_COS_CONTEXT + "/" + TIME_SERVICE_NAME_LONG + "/" + TIME_SERVICE_NAME_LONG;
    public static final String TIMER_EVENT_SERVICE_NAME         = "TimerEventService";
    public static final String TIMER_EVENT_SERVICE_NAME_NS      =
        ROOT_COS_CONTEXT + "/" + TIME_SERVICE_NAME_LONG + "/" + TIMER_EVENT_SERVICE_NAME;

    /* TradingService */
    public static final String TRADING_SERVICE_NAME             = "trader";
    public static final String TRADING_SERVICE_NAME_LONG        = "TradingService";
    public static final String TRADING_KERNEL_NAME              = "Kernel";
    public static final String TRADING_KERNEL_NAME_NS           =
        ROOT_COS_CONTEXT + "/" + TRADING_SERVICE_NAME_LONG + "/" + TRADING_KERNEL_NAME;
    public static final String TRADING_SVC_TYPE_REPO_NAME       = "ServiceTypeRepository";
    public static final String TRADING_SVC_TYPE_REPO_NAME_NS    =
        ROOT_COS_CONTEXT + "/" + TRADING_SERVICE_NAME_LONG + "/" + TRADING_SVC_TYPE_REPO_NAME;

    /* TransactionService */
    public static final String OTS_NAME                         = "ots";
    public static final String OTS_NAME_LONG                    = "TransactionService";
    public static final String TRANSACTION_FACTORY_NAME         = "TransactionFactory";
    public static final String TRANSACTION_FACTORY_NAME_NS      =
        ROOT_COS_CONTEXT + "/" + OTS_NAME_LONG + "/" + TRANSACTION_FACTORY_NAME;

    /* InterfaceRepository */
    public static final String IR_NAME                          = "ir";
    public static final String IR_NAME_LONG                     = "InterfaceRepository";
    public static final String REPOSITORY_NAME                  = "Repository";
    public static final String REPOSITORY_NAME_NS               =
        ROOT_COS_CONTEXT + "/" + IR_NAME_LONG + "/" + REPOSITORY_NAME;

    /** Sync object for hash maps. */
    private static final Object SYNC_MAP = new Object();

    /** Resolve initial references map. */
    private static final HashMap RIR_MAP = new HashMap();

    /**
     * Utility class. Do not instantiate.
     */
    private NamingUtils()
    {
    }

    /**
     * Provides a mapping between names passed to the method
     * resolve_initial_references() and the lookup name for the NamingService.
     *
     * See CORBA 3.0, Table 4-1, for details.
     */
    public static String rirMapping( String rirName )
    {
        synchronized ( SYNC_MAP )
        {
            if ( RIR_MAP.isEmpty() )
            {
                RIR_MAP.put( IR_NAME_SERVICE,
                          NAMING_CONTEXT_NAME_NS );
                RIR_MAP.put( IR_INTERFACE_REPOSITORY,
                          REPOSITORY_NAME_NS );
                RIR_MAP.put( IR_NOTIFICATION_SERVICE,
                          NOTIFICATION_CHANNEL_FACTORY_NAME_NS );
                // not bound -- not yet supported
                //RIR_MAP.add( IR_TYPED_NOTIFICATION_SERVICE,
                //      TYPED_NOTIFICATION_CHANNEL_FACTORY_NAME_NS );
                RIR_MAP.put( IR_TRADING_SERVICE,
                        TRADING_KERNEL_NAME_NS );
                RIR_MAP.put( IR_PSS,
                           PSS_CONNECTOR_FACTORY_NAME_NS );

                // Non-Standard (not defined in CORBA spec.)
                RIR_MAP.put( IR_CCS,
                          LOCK_SET_FACTORY_NAME_NS );
                RIR_MAP.put( IR_EVENT_SERVICE,
                          EVENT_CHANNEL_FACTORY_NAME_NS );
                RIR_MAP.put( IR_OTS,
                          TRANSACTION_FACTORY_NAME_NS );
                RIR_MAP.put( IR_PROPERTY_SERVICE,
                          PROPERTY_SET_FACTORY_NAME_NS );
                RIR_MAP.put( IR_SERVICE_TYPE_REPOSITORY,
                          TRADING_SVC_TYPE_REPO_NAME_NS );
                RIR_MAP.put( IR_TIME_SERVICE,
                          TIME_SERVICE_NAME_NS );
            }
        }
        return ( String ) RIR_MAP.get( rirName );
    }

    /**
     * Encodes a string according to RFC2396.
     * All escaped chars use UTF-8
     * encoding ( RFC2396 has been updated to RFC2732 ).
     *
     * @param str The string to encode.
     * @return The encoded string.
     * @throws UnsupportedEncodingException When the string can't be converted
     * into UTF-8 format.
     */
    public static String encodeRFC2396( String str )
        throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        int start = 0;
        byte [] tmp;
        for ( int i = 0; i < str.length(); ++i )
        {
            char c = str.charAt( i );
            if ( ( c >= 'a' && c <= 'z' )
                  || ( c >= 'A' && c <= 'Z' )
                  || ( c >= '0' && c <= '9' ) )
            {
                continue;
            }
            switch ( c )
            {
            // reserved
            case ';':
            case '/':
            case '?':
            case ':':
            case '@':
            case '&':
            case '=':
            case '+':
            case '$':
            case ',':
            // unreserved
            case '-':
            case '_':
            case '.':
            case '!':
            case '~':
            case '*':
            case '\'':
            case '(':
            case ')':
                break;

            default:
                sb.append( str.substring( start, i ) );
                tmp = str.substring( i, i + 1 ).getBytes( "UTF-8" );
                for ( int j = 0; j < tmp.length; ++j )
                {
                    sb.append( '%' );
                    if ( ( tmp[ j ] & 0xF0 ) < 0xA0 )
                    {
                        sb.append( ( tmp[ j ] & 0xF0 ) >> 4 );
                    }
                    else
                    {
                        sb.append( ( char ) ( 'A' + ( ( tmp[ j ]
                              & 0xF0 - 0xA0 ) >> 4 ) ) );
                    }
                    if ( ( tmp[ j ] & 0xF ) < 0xA )
                    {
                        sb.append( tmp[ j ] & 0xF );
                    }
                    else
                    {
                        sb.append( ( char ) ( 'A' + ( tmp[ j ]
                              & 0xF - 0xA ) ) );
                    }
                }
                start = i + 1;
            }
        }
        sb.append( str.substring( start ) );
        return sb.toString();
    }

    /**
     * Decodes a RFC2396 encoded string.
     *
     * @param enc The string to encode.
     * @return The decoded string.
     * @throws UnsupportedEncodingException
     * @throws NumberFormatException
     */
    public static String decodeRFC2396( String enc )
        throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        int start = 0;
        for ( int i = 0; i < enc.length(); ++i )
        {
            if ( sb.charAt( i ) == '%' )
            {
                sb.append( enc.substring( start, i ) );
                int count = 1;
                while ( sb.charAt( i + 2 * count ) == '%' )
                {
                    ++count;
                }
                byte [] buf = new byte[ count ];
                for ( int j = 0; j < count; ++j )
                {
                    buf[ i ] = Byte.parseByte(
                        enc.substring( i + 2 * j + 1, i + 2 * j + 2 ), 16 );
                }
                sb.append( new String( buf, "UTF-16" ) );
                i += count * 3 - 1;
            }
        }
        sb.append( enc.substring( start ) );
        return sb.toString();
    }

    /**
     * This function checks an address for the correct format.
     *
     * @param addr The address to check.
     * @return True if the format is correct, false otherwise.
     */
    public static boolean checkAddress( String addr )
    {
        int end = addr.length();
        int start;
        do
        {
            start = addr.lastIndexOf( ",", end );
            if ( start < 0 )
            {
                start = 0;
            }
            int proto = addr.indexOf( ":", start );
            // parse the protocol
            if ( proto < 0 || proto > end )
            {
                return false;
            }
            String strProtocol = addr.substring( start + 1, proto );
            // only "iiop" is currently supported
            if ( strProtocol != null && strProtocol.length() > 0
                  && !strProtocol.equals( "iiop" ) )
            {
                return false;
            }
            int vers = addr.indexOf( "@", proto );
            if ( vers > 0 && vers < end )
            {
                // parse the version.
                int major = addr.indexOf( ".", proto );
                if ( major < 0 || major > vers )
                {
                    return false;
                }
                int min;
                int maj;
                try
                {
                    maj = Integer.parseInt( addr.substring( proto + 1, major ) );
                    min = Integer.parseInt( addr.substring( major + 1, vers ) );
                }
                catch ( NumberFormatException ex )
                {
                    return false;
                }
                if ( maj != 1 )
                {
                    return false;
                }
                if ( min < 0 || min > 2 /* 3 */ )
                {
                    return false;
                }
            }
            else
            {
                vers = proto;
            }
            int host = addr.indexOf( ":", vers + 1 );
            if ( host > 0 && host < end )
            {
                // parse the port
                int iPort;
                try
                {
                    iPort = Integer.parseInt( addr.substring( host + 1, end ) );
                }
                catch ( NumberFormatException ex )
                {
                    return false;
                }
                if ( iPort < 0 || iPort > 0xFFFF )
                {
                    return false;
                }
            }
            else
            {
                host = end;
            }
            // parse the host
            String strHost = addr.substring( vers + 1, host );
            if ( strHost == null || strHost.length() == 0 )
            {
                return false;
            }
            end = start;
        }
        while ( start > 0 );
        return true;
    }

    /**
     * Convert a string to a org.omg.CosNaming.Name using Java reflection.
     *
     * @param name The string to be converted into a org.omg.CosNaming.Name.
     * @return The org.omg.CosNaming.Name converted from the String.
     * @throws Exception Various reflection exceptions.
     */
    public static Object dynamicStringToName( String name )
        throws Exception
    {
        // split the name into an array of NameComponents
        java.util.StringTokenizer st = new java.util.StringTokenizer( name, "/" );
        Class ncomp_clz = Thread.currentThread().getContextClassLoader().loadClass(
              "org.omg.CosNaming.NameComponent" );
        java.lang.reflect.Field ncomp_id_fld = ncomp_clz.getDeclaredField( "id" );
        java.lang.reflect.Field ncomp_kind_fld = ncomp_clz.getDeclaredField( "kind" );
        java.lang.Object ncomp_arr = java.lang.reflect.Array.newInstance(
              ncomp_clz, st.countTokens() );
        int i = 0;
        while ( st.hasMoreTokens() )
        {
            java.lang.Object ncomp_obj = ncomp_clz.newInstance();
            String id = "";
            String kind = "";
            String token = st.nextToken();
            int idx = token.indexOf( '.' );
            if ( idx > 0 )
            {
                id = token.substring( 0, idx );
                kind = token.substring( idx + 1 );
            }
            else
            {
                id = token;
            }
            ncomp_id_fld.set( ncomp_obj, id );
            ncomp_kind_fld.set( ncomp_obj, kind );
            java.lang.reflect.Array.set( ncomp_arr, i++, ncomp_obj );
        }
        return ncomp_arr;
    }

    /**
     * This method binds the object under the specified name at the NamingService.
     * The NamingService is retrieved by rir "NameService". The method returns true
     * upon success and false otherwise.
     *
     * @param orb The orb instance on which to resolve the "NameService" reference.
     * @param name The name under which to bind the object.
     * @param obj The object to bind.
     * @return True if bind was successful or false otherwise.
     */
    public static boolean dynamicRebind( org.omg.CORBA.ORB orb, String name,
          org.omg.CORBA.Object obj )
    {
        boolean result = false;
        try
        {
            org.omg.CORBA.Object namingobj = orb.resolve_initial_references( NS_NAME_LONG );
            Class nc_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming.NamingContext" );
            Class ncstub_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming._NamingContextStub" );
            if ( namingobj != null && ( namingobj.getClass().isAssignableFrom( nc_clz )
                  || namingobj._is_a( "IDL:omg.org/CosNaming/NamingContext:1.0" ) ) )
            {
                Object ncstub_obj = ReflectionUtils.getStubInstance( ncstub_clz, namingobj );

                // bind the parent contexts
                Object ncomp_arr = dynamicStringToName( name );
                java.util.StringTokenizer st = new java.util.StringTokenizer( name, "/" );
                String par_ctx = null;
                boolean bExOccured = false;
                while ( st.hasMoreTokens() )
                {
                    String tok = st.nextToken();
                    if ( par_ctx == null )
                    {
                        par_ctx = tok;
                    }
                    else
                    {
                        par_ctx += "/" + tok;
                    }
                    if ( st.hasMoreTokens() )
                    {
                        Object par_nam = dynamicStringToName( par_ctx );
                        java.lang.reflect.Method bind_new_context = ncstub_obj.getClass().getMethod(
                              "bind_new_context", new Class[] { par_nam.getClass() } );
                        try
                        {
                            bind_new_context.invoke( ncstub_obj, new Object[] { par_nam } );
                        }
                        catch ( Exception ex )
                        {
                            // Check whether exception is other than AlreadyBound exception
                            Class already_bound_clz = null;
                            try
                            {
                                  Thread.currentThread().getContextClassLoader().loadClass(
                                  "org.omg.CosNaming.NamingContextPackage.AlreadyBound" );
                            }
                            catch ( Exception notfound )
                            {
                                // ignore, will be handled below
                            }
                            if ( already_bound_clz != null
                                  && !already_bound_clz.isAssignableFrom( ex.getClass() ) )
                            {
                                bExOccured = true;
                            }
                        }
                    }
                }
                // do not try to bind the object when the context already failed.
                if ( !bExOccured )
                {
                    // get the rebind method and do the invocation
                    java.lang.Class[] paramTypes = new Class[] { ncomp_arr.getClass(),
                          org.omg.CORBA.Object.class };
                    java.lang.Object[] paramObjects = new java.lang.Object[] { ncomp_arr, obj };
                    java.lang.reflect.Method rebind = ncstub_obj.getClass().getDeclaredMethod(
                          "rebind", paramTypes );
                    rebind.invoke( ncstub_obj, paramObjects );

                    // invocation was successful
                    result = true;
                }
            }
        }
        catch ( final Exception ex )
        {
            // do nothing, return false
//            ex.printStackTrace();
        }
        return result;
    }

    /**
     * This method tries to resolve an object from the NamingService.
     * The NamingService is retrieved by rir "NameService". The method returns the object
     * when one could be found under the specified path name, or null otherwise.
     *
     * @param orb The orb instance on which to resolve the "NameService" reference.
     * @param name The name under which to bind the object.
     * @return The object resolved or null when no object was found.
     */
    public static org.omg.CORBA.Object dynamicResolve( org.omg.CORBA.ORB orb, String name )
    {
        org.omg.CORBA.Object result = null;
        try
        {
            org.omg.CORBA.Object namingobj = orb.resolve_initial_references( NS_NAME_LONG );
            Class nc_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming.NamingContext" );
            Class ncstub_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming._NamingContextStub" );
            if ( namingobj != null && ( namingobj.getClass().isAssignableFrom( nc_clz )
                  || namingobj._is_a( "IDL:omg.org/CosNaming/NamingContext:1.0" ) ) )
            {
                Object ncstub_obj = ReflectionUtils.getStubInstance( ncstub_clz, namingobj );

                // bind the parent contexts
                Object ncomp_arr = dynamicStringToName( name );

                // get the rebind method and do the invocation
                java.lang.Class[] paramTypes = new Class[] { ncomp_arr.getClass() };
                java.lang.Object[] paramObjects = new java.lang.Object[] { ncomp_arr };
                java.lang.reflect.Method resolve = ncstub_obj.getClass().getDeclaredMethod(
                      "resolve", paramTypes );
                result = ( org.omg.CORBA.Object ) resolve.invoke( ncstub_obj, paramObjects );
            }
        }
        catch ( final Exception ex )
        {
            // do nothing, return null
//        ex.printStackTrace();
        }
        return result;
    }

    /**
     * This method tries to unbind an object from the NamingService.
     * The NamiingService is retrieved by rir "NameService". The method tries to unbind
     * the object from the NamingService. The method returns true when the unbind was
     * successful or false otherwise.
     *
     * @param orb The orb instance on which to resolve the "NameService" reference.
     * @param name The name under which to bind the object.
     * @return The object resolved or null when no object was found.
     */
    public static boolean dynamicUnbind( org.omg.CORBA.ORB orb, String name )
    {
        boolean result = false;
        try
        {
            org.omg.CORBA.Object namingobj = orb.resolve_initial_references( NS_NAME_LONG );
            Class nc_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming.NamingContext" );
            Class ncstub_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.omg.CosNaming._NamingContextStub" );
            if ( namingobj != null && ( namingobj.getClass().isAssignableFrom( nc_clz )
                  || namingobj._is_a( "IDL:omg.org/CosNaming/NamingContext:1.0" ) ) )
            {
                Object ncstub_obj = ReflectionUtils.getStubInstance( ncstub_clz, namingobj );

                // bind the parent contexts
                Object ncomp_arr = dynamicStringToName( name );

                // get the rebind method and do the invocation
                java.lang.Class[] paramTypes = new Class[] { ncomp_arr.getClass() };
                java.lang.Object[] paramObjects = new java.lang.Object[] { ncomp_arr };
                java.lang.reflect.Method resolve = ncstub_obj.getClass().getDeclaredMethod(
                      "unbind", paramTypes );
                resolve.invoke( ncstub_obj, paramObjects );
                result = true;
            }
        }
        catch ( final Exception ex )
        {
            // do nothing, return false
        }
        return result;
    }

    /**
     * Bind the specified object at the CorbalocService.
     *
     * @param orb The orb from which to get the CorbalocService.
     * @param name The name under which to bind the object.
     * @param obj The object to bind.
     * @return A string containing the corbaloc URL, or null, when the bind failed.
     */
    public static String bindObjectToCorbalocService( ORB orb, String name,
          org.omg.CORBA.Object obj )
    {
        String result = null;
        try
        {
            org.omg.CORBA.Object clsobj = orb.resolve_initial_references( "CorbalocService" );
            Class cls_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.openorb.orb.corbaloc.CorbalocService" );
            Class clsstub_clz = Thread.currentThread().getContextClassLoader().loadClass(
                  "org.openorb.orb.corbaloc._CorbalocServiceStub" );

            if ( clsobj != null && ( clsobj.getClass().isAssignableFrom( cls_clz )
                  || clsobj._is_a( "IDL:orb.openorb.org/corbaloc/CorbalocService:1.1" ) ) )
            {
                // create an instance of _CorbalocServiceStub (default constructor)
                Object clsstub_obj = ReflectionUtils.getStubInstance( clsstub_clz, clsobj );

                // get the put method
                java.lang.reflect.Method put = clsstub_obj.getClass().getMethod(
                      "put", new Class[] { String.class, org.omg.CORBA.Object.class } );

                // call the _get_delegate() method on the stub
                Object deleg = ( ( org.omg.CORBA.portable.ObjectImpl ) clsobj )._get_delegate();

                try
                {
                    // call the put operation on the CorbalocService
                    put.invoke( clsstub_obj, new Object[] { name, obj } );

                    // invocation was successful, get several methods and classes
                    Class orgOpenorbOrbNetAddressClz =
                          Thread.currentThread().getContextClassLoader().loadClass(
                          "org.openorb.orb.net.Address" );
                    Class orgOpenorbOrbCoreDelegateClz =
                          Thread.currentThread().getContextClassLoader().loadClass(
                          "org.openorb.orb.core.Delegate" );
                    java.lang.reflect.Method getAddresses =
                          orgOpenorbOrbCoreDelegateClz.getMethod(
                          "getAddresses", new java.lang.Class[] {
                          org.omg.CORBA.Object.class } );
                    java.lang.reflect.Method getProtocol =
                          orgOpenorbOrbNetAddressClz.getMethod(
                          "getProtocol", new java.lang.Class[] {} );
                    java.lang.reflect.Method getEndpointString =
                          orgOpenorbOrbNetAddressClz.getMethod(
                          "getEndpointString", new java.lang.Class[] {} );
                    Object addrs = getAddresses.invoke( deleg, new Object[] { obj } );
                    String endpoint = null;
                    if ( addrs != null )
                    {
                        int len = Array.getLength( addrs );
                        for ( int i = 0; i < len; i++ )
                        {
                            Object addr = Array.get( addrs, i );
                            String prot = ( String ) getProtocol.invoke( addr, new Object[] {} );
                            if ( prot.equals( "iiop" ) )
                            {
                                endpoint = ( String ) getEndpointString.invoke( addr,
                                      new Object[] {} );
                            }
                        }
                        if ( endpoint == null )
                        {
                            endpoint = ( String ) getEndpointString.invoke( Array.get( addrs, 0 ),
                                  new Object[] {} );
                        }
                        result = "corbaloc:" + endpoint + "/" + name;
                    }
                }
                catch ( Exception ex )
                {
                    // do nothing, return false
                }
            }
        }
        catch ( final Exception ex )
        {
            // do nothing, return false
        }
        return result;
    }

    /**
     * Binds an object into the Naming Service or uses an alternative binding method.
     * The standard route is taken first, i.e. it is attempted to bind the
     * object at a running NamingService instance. If this fails a temporary folder
     * is created that reflects the hierarchy of the NamingService.
     *
     * @param orb The orb instance to resolve the NamingService from.
     * @param name The name under which to bind the object in the NamingService.
     * @param obj The object to bind.
     * @param logger logger to use for logging
     * @return True when the bind was successful, false otherwise.
     */
    public static boolean bindObjectToNamingService( ORB orb,
            String name,
            org.omg.CORBA.Object obj,
            Logger logger )
    {
        return bindObjectToNamingService( orb, name, obj, false, logger );
    }


    /**
     * Binds an object into the Naming Service or uses an alternative binding method.
     * The standard route is taken first, i.e. it is attempted to bind the
     * object at a running NamingService instance. If this fails a temporary folder
     * is created that reflects the hierarchy of the NamingService.
     *
     * @param orb The orb instance to resolve the NamingService from.
     * @param name The name under which to bind the object in the NamingService.
     * @param obj The object to bind.
     * @param bNoNS Do not try to bind at the NamingService.
     * @param logger logger to use for logging
     * @return True when the bind was successful, false otherwise.
     */
    public static boolean bindObjectToNamingService(
            ORB orb,
            String name,
            org.omg.CORBA.Object obj,
            boolean bNoNS,
            Logger logger )
    {
        boolean bSuccess = false;

        if ( !bNoNS )
        {
            if ( logger != null && logger.isDebugEnabled() )
            {
                logger.debug( "Trying to bind: '" + name + "' to NamingService" );
            }

            bSuccess = dynamicRebind( orb, name, obj );

            if ( !bSuccess )
            {
                logger.warn( "Failed to bind '" + name + "' to the NamingService."
                             + " Check whether your NamingService is running!" );
            }
        }

        if ( !bSuccess )
        {
            // use a fallback strategy now

            if ( logger != null && logger.isDebugEnabled() )
            {
                logger.debug( "Using fallback strategy for binding: '" + name + "'" );
            }

            bSuccess = bindObjectToFileSystem( orb, name, obj );

            if ( !bSuccess && logger != null )
            {
                logger.warn( "Failed to bind '" + name + "' IOR to the File System." );
            }
        }

        return bSuccess;
    }


    /**
     * "Bind" the object by writing the IOR into a file.
     * @param orb The orb instance to resolve the NamingService from.
     * @param name The name under which to bind the object in the NamingService.
     * @param obj The object to bind.
     * @return true if success; false if failure
     */
    public static boolean bindObjectToFileSystem( ORB orb,
            String name,
            org.omg.CORBA.Object obj )
    {
        boolean bSuccess = false;

        // create folder "OpenORB" in the tmp folder

        try
        {
            File fNSDir = getNSRootDir();

            // convert object to IOR
            String strIOR = orb.object_to_string( obj );

            // create a file with the name of the object
            File fIOR = new File( fNSDir, name + ORBUtils.IOR_FILE_EXT );
            // create any path elements that have not yet been created
            File fIORPath = new File( fIOR.getParent() );
            bSuccess = fIORPath.mkdirs();
            // create the actual file
            bSuccess = fIOR.createNewFile();

            // write the IOR into that file
            FileWriter fwIOR = new FileWriter( fIOR );
            fwIOR.write( strIOR );
            fwIOR.close();

            // we successfully finished the fallback binding
            bSuccess = true;
        }
        catch ( IOException ex )
        {
            // do nothing, return false
        }

        return bSuccess;
    }


    /**
     * @see #resolveObjectFromNamingService(org.omg.CORBA.ORB,java.lang.String,boolean)
     */
    public static org.omg.CORBA.Object resolveObjectFromNamingService( ORB orb, String name )
    {
        return resolveObjectFromNamingService( orb, name, false );
    }

    /**
     * @see #resolveObjectFromNamingService(org.omg.CORBA.ORB,java.lang.String,boolean,Logger)
     */
    public static org.omg.CORBA.Object resolveObjectFromNamingService( ORB orb, String name,
        boolean bNoNS )
    {
        return resolveObjectFromNamingService( orb, name, bNoNS, null );
    }

    /**
     * Resolve an object from the Naming Service or use an alternative lookup method.
     * The standard route is taken first, i.e. it is attempted to resolve the
     * object from a running NamingService instance. If this fails a temporary folder
     * is searched that reflects the hierarchy of the NamingService.
     *
     * @param orb The orb instance to resolve the NamingService from.
     * @param name The name under which to bind the object in the NamingService.
     * @param bNoNS Do not try to resolve from the NamingService. This option is used for the
     * NamingService itself, because resolving the initial NamingService at the NamingService
     * wouldn't make any sense.
     * @return The object bound or null if no object was found.
     */
    public static org.omg.CORBA.Object resolveObjectFromNamingService( ORB orb, String name,
          boolean bNoNS, Logger logger )
    {
        org.omg.CORBA.Object result = null;
        if ( !bNoNS )
        {
            result = dynamicResolve( orb, name );
        }

        if ( result == null )
        {
            try
            {
                // use a fallback strategy now
                File fNSDir = getNSRootDir();

                // get the file with the name of the object
                File fIOR = new File( fNSDir, name + ORBUtils.IOR_FILE_EXT );

                if ( logger != null && logger.isDebugEnabled() )
                {
                    logger.debug( "Reference file is " + fNSDir + "/" + name
                            + ORBUtils.IOR_FILE_EXT );
                }

                FileReader frIOR = new FileReader( fIOR );
                BufferedReader brIOR = new BufferedReader( frIOR );
                String strIOR = brIOR.readLine();
                brIOR.close();
                frIOR.close();

                // convert IOR
                result = orb.string_to_object( strIOR );
            }
            catch ( IOException ex )
            {
                if ( logger != null && logger.isDebugEnabled() )
                {
                    logger.debug( "Reference file not found " + name );
                }
                // do nothing, return null
            }
        }
        return result;
    }

    /**
     * @see #unbindObjectFromNamingService(org.omg.CORBA.ORB,java.lang.String,boolean)
     */
    public static boolean unbindObjectFromNamingService( ORB orb, String name )
    {
        return unbindObjectFromNamingService( orb, name, false );
    }

    /**
     * unbind an object from the Naming Service or delete the ior file from the file system.
     * The standard route is taken first, i.e. it is attempted to unbind the
     * object from a running NamingService instance. If this fails a temporary folder
     * is searched that reflects the hierarchy of the NamingService. If an ior file for the service
     * is found then the file will be deleted.
     *
     * @param orb The orb instance to unbind the NamingService from.
     * @param name The name of the object to unbind from the NamingService.
     * @param bNoNS Do not try to unbind from the NamingService. This option is used for the
     * NamingService itself, because unbinding the initial NamingService from the NamingService
     * wouldn't make any sense.
     * @return True when unbind was successful, false otherwise.
     */
    public static boolean unbindObjectFromNamingService( ORB orb, String name,
          boolean bNoNS )
    {
        boolean bSuccess = false;
        if ( !bNoNS )
        {
            bSuccess = dynamicUnbind( orb, name );
        }
        if ( !bSuccess )
        {
            try
            {
                // use a fallback strategy now
                File fNSDir = getNSRootDir();

                // get the file with the name of the object
                File fIOR = new File( fNSDir, name + ORBUtils.IOR_FILE_EXT );

                bSuccess = fIOR.delete();
            }
            catch ( IOException ex )
            {
                bSuccess = false;
            }
        }

        return bSuccess;
    }


    /**
     * Returns the Directory to use for the Naming Service
     * bindings.
     * <p>
     * Tries the following locations in order:
     * <ol>
     * <li>System Property: OpenORB home directory</li>
     * <li>System Property: User home directory</li>
     * <li>Current Directory</li>
     * </ol>
     * @return File directory
     * @exception IOException occurs if unable to create temporary directory
     */
    private static File getNSRootDir()
            throws IOException
    {
        return ORBUtils.getTemporaryDir( null, NS_IOR_DIRNAME );
    }
}

