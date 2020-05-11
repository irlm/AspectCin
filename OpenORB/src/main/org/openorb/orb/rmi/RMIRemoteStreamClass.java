/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.rmi.CORBA.Util;

import org.omg.CORBA.TCKind;

import org.openorb.util.RepoIDHelper;

/**
 * This is a default implementation for a Remote interface
 *
 * @author Jerome Daniel
 */
public final class RMIRemoteStreamClass
{
   /**
    * Keep the stream classes in thread thread local hash maps.
    * This avoids expensive global mutex locks.
    */
    private static final ThreadLocal
        STREAM_CLASSES = new ThreadLocal();

    private static final Map
        GLOBAL_CLASSES = new HashMap();

    private static final RMIRemoteStreamClass
        RC = new RMIRemoteStreamClass();

    private static org.openorb.orb.core.ORBSingleton
        s_orb =  new org.openorb.orb.core.ORBSingleton();

    private Class m_implementation_class;
    private Class m_tie_class;

    private Class [] m_remote_interface_classes;
    private Class [] m_stub_classes;

    private String [] m_repository_ids;
    private org.omg.CORBA.TypeCode [] m_types;

    static
    {
        RC.m_implementation_class = java.rmi.Remote.class;
        RC.m_remote_interface_classes = new Class [] { java.rmi.Remote.class };
        RC.m_stub_classes = new Class [] { _Remote_Stub.class };
        RC.m_repository_ids = new String [] { "IDL:omg.org/CORBA/Object:1.0" };
        RC.m_types = new org.omg.CORBA.TypeCode[] { s_orb.get_primitive_tc( TCKind.tk_objref ) };
    }

    /**
     * Lookup the remote class data for the given class.
     *
     * @param clz The class for which to get the remote stream class.
     * @return the remote class data, or null if the class
     * does not implement Remote.
     */
    public static RMIRemoteStreamClass lookup( Class clz )
    {
        if ( clz == null || !java.rmi.Remote.class.isAssignableFrom( clz ) )
        {
            return null;
        }

        RMIRemoteStreamClass rc = getFromCache( clz );
        if ( rc != null )
        {
            return rc;
        }

        Class [] derr = getHelperClasses( clz );
        if ( derr == null )
        {
            return null;
        }

        rc = getFromCache( derr[ 0 ] );
        if ( rc == null )
        {
            rc = new RMIRemoteStreamClass( derr );
            addToCache( clz, rc );
        }

        return rc;
    }

    // special constructor.
    private RMIRemoteStreamClass()
    {
    }

    // constructor for ordinary types
    private RMIRemoteStreamClass( Class[] helperClz )
    {
        m_implementation_class = helperClz[ 0 ];
        if ( !m_implementation_class.isInterface() )
        {
            m_remote_interface_classes = new Class[ helperClz.length - 1 ];
            System.arraycopy( helperClz, 1, m_remote_interface_classes, 0,
                    m_remote_interface_classes.length );
        }
        else
        {
            m_remote_interface_classes = new Class[] { m_implementation_class };
        }
        m_repository_ids = new String[ m_remote_interface_classes.length ];
        m_types = new org.omg.CORBA.TypeCode[ m_remote_interface_classes.length ];
        for ( int i = 0; i < m_remote_interface_classes.length; ++i )
        {
            String [] names = RepoIDHelper.mangleClassName( m_remote_interface_classes[ i ] );
            m_repository_ids[ i ] = "RMI:" + names[ 0 ] + ":0000000000000000";
            m_types[ i ] = s_orb.create_interface_tc( m_repository_ids[ i ], names[ 1 ] );
        }
    }


    /**
     * Return repository id string array.
     *
     * @return The repository id string array.
     */
    public String[] getRepoIDs()
    {
        return ( String[] ) m_repository_ids.clone();
    }

    String [] getRepoIDsNoCopy()
    {
        return m_repository_ids;
    }

    /**
     * Return the typecodes of the supported interfaces.
     *
     * @return The type codes for the interface types.
     */
    public org.omg.CORBA.TypeCode[] getInterfaceTypes()
    {
        return ( org.omg.CORBA.TypeCode[] ) m_types.clone();
    }

    /**
     * Return the typecodes of the supported interfaces without copying.
     */
    org.omg.CORBA.TypeCode [] getInterfaceTypesNoCopy()
    {
        return m_types;
    }

    /**
     * Create a new tie class.
     */
    synchronized javax.rmi.CORBA.Tie createTie()
        throws java.rmi.server.ExportException
    {
        if ( m_tie_class == null )
        {
            String [] parts = RepoIDHelper.extractClassName( m_implementation_class );
            String tieName = ( ( parts[ 0 ].length() == 0 ) ? "_"
                  : ( parts[ 0 ] + "._" ) ) + parts[ 1 ] + "_Tie";
            try
            {
                m_tie_class = Util.loadClass(
                        tieName, Util.getCodebase( m_implementation_class ),
                        m_implementation_class.getClassLoader() );
            }
            catch ( ClassNotFoundException ex )
            {
                throw new java.rmi.server.ExportException(
                        "Tie class not found (" + ex + ")", ex );
            }
        }

        try
        {
            return ( javax.rmi.CORBA.Tie ) m_tie_class.newInstance();
        }
        catch ( InstantiationException ex )
        {
            throw new java.rmi.server.ExportException(
                    "InstantiationException while creating tie (" + ex + ")", ex );
        }
        catch ( IllegalAccessException ex )
        {
            throw new java.rmi.server.ExportException(
                    "IllegalAccessException while creating tie (" + ex + ")", ex );
        }
    }

    /**
     * Create a new stub class.
     * @param idx index of the stub class to create.
     */
    synchronized javax.rmi.CORBA.Stub createStub( int idx )
        throws ClassNotFoundException
    {
        if ( m_stub_classes == null )
        {
            m_stub_classes = new Class[ m_remote_interface_classes.length ];
        }
        if ( m_stub_classes[ idx ] == null )
        {
            String [] parts = RepoIDHelper.extractClassName(
                    m_remote_interface_classes[ idx ] );
            String stubName = ( ( parts[ 0 ].length() == 0 ) ? "_"
                  : ( parts[ 0 ] + "._" ) ) + parts[ 1 ] + "_Stub";
            m_stub_classes[ idx ] = Util.loadClass(
                  stubName, Util.getCodebase( m_implementation_class ),
                  m_implementation_class.getClassLoader() );
        }
        try
        {
            return ( javax.rmi.CORBA.Stub ) m_stub_classes[ idx ].newInstance();
        }
        catch ( InstantiationException ex )
        {
            throw new IncompatibleClassChangeError(
                    "InstantiationException while creating stub (" + ex + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new IncompatibleClassChangeError(
                    "IllegalAccessException while creating stub (" + ex + ")" );
        }
    }

    int countStubs()
    {
        return m_remote_interface_classes.length;
    }

    /**
     * This method generates a list of all the helper interfaces, the first
     * element in the array is the class for which the tie class is generated
     * if this is not an interface class the remaining elements will be the
     * classes for which the repository IDs are generated.
     */
    private static Class [] getHelperClasses( Class clz )
    {
        List tops = new ArrayList();
        Class tie = getMostDerived( clz, new HashSet(), tops );
        if ( tie == null )
        {
            return null;
        }
        if ( tie.isInterface() )
        {
            return new Class [] { tie };
        }
        Class [] ret = new Class[ tops.size() + 1 ];
        tops.toArray( ret );
        System.arraycopy( ret, 0, ret, 1, ret.length - 1 );
        ret[ 0 ] = tie;
        return ret;
    }

    /**
     * Generate the leaf classes. This returns a tree, the leaf nodes of which
     * are the interfaces which implement remote directly.
     */
    private static Class getMostDerived( Class clz, Set leaf, List tops )
    {
        if ( !java.rmi.Remote.class.isAssignableFrom( clz )
              || java.rmi.Remote.class.equals( clz ) )
        {
            return null;
        }
        boolean isIfc = clz.isInterface();
        if ( isIfc && leaf.contains( clz ) )
        {
            return null;
        }
        Class [] ifs = clz.getInterfaces();
        Class sup = isIfc ? null : clz.getSuperclass();
        if ( !isIfc )
        {
            if ( ifs.length == 0 )
            {
                return getMostDerived( clz.getSuperclass(), leaf, tops );
            }
            if ( tops != null )
            {
                // generate list of topmost interfaces.
                for ( int i = 0; i < ifs.length; ++i )
                {
                    if ( !java.rmi.Remote.class.isAssignableFrom( ifs[ i ] )
                          || java.rmi.Remote.class.equals( ifs[ i ] ) )
                    {
                        ifs[ i ] = null;
                        continue;
                    }
                    boolean found = false;
                    ListIterator itt = tops.listIterator();
                    while ( itt.hasNext() )
                    {
                        Class t = ( Class ) itt.next();
                        if ( ifs[ i ].isAssignableFrom( t ) )
                        {
                            found = true;
                            break;
                        }
                        else if ( t.isAssignableFrom( ifs[ i ] ) )
                        {
                            itt.set( ifs[ i ] );
                            found = true;
                            break;
                        }
                    }
                    if ( !found )
                    {
                        tops.add( ifs[ i ] );
                    }
                }
            }
        }
        Class lc = null;
        if ( sup != null )
        {
            lc = getMostDerived( sup, leaf, tops );
        }
        int i;
        for ( i = 0; i < ifs.length && lc == null; ++i )
        {
            if ( ifs[ i ] != null )
            {
                lc = getMostDerived( ifs[ i ], leaf, tops );
            }
        }
        for ( ; i < ifs.length; ++i )
        {
            if ( ifs[ i ] != null && getMostDerived( ifs[ i ], leaf, tops ) != null )
            {
                lc = null;
            }
        }
        if ( isIfc )
        {
            leaf.add( clz );
            return clz;
        }
        return ( lc == null ) ? clz : lc;
    }

    /**
     * Keep the stream classes as thread local storage to avoid expensive mutex locks.
     * When threads die the content of the map will be gc'ed.
     */
    private static Map getStreamClassesMap()
    {
        Map map  = ( Map ) STREAM_CLASSES.get();
        if ( map == null )
        {
            map = new HashMap();
            // add the default types
            map.put( java.rmi.Remote.class, RC );
            STREAM_CLASSES.set( map );
        }
        return map;
    }

    /**
     * Add a stream instance for a class to the cache.
     * Store the instance in the thread local cache for a faster non-synchronized
     * access. Additionally keep it in a global map because threads could have been
     * started temporary.
     */
    private static void addToCache( Class clz, RMIRemoteStreamClass strclz )
    {
        Map map = getStreamClassesMap();
        map.put( clz, strclz );
        synchronized ( GLOBAL_CLASSES )
        {
            if ( GLOBAL_CLASSES.get( clz ) == null )
            {
                GLOBAL_CLASSES.put( clz, strclz );
            }
        }
    }

    /**
     * Get a stream instance for a class from the cache.
     * Store the instance in the thread local cache for a faster non-synchronized
     * access. Additionally keep it in a global map because threads could have been
     * started temporary.
     */
    private static RMIRemoteStreamClass getFromCache( Class clz )
    {
        Map map = getStreamClassesMap();
        RMIRemoteStreamClass strclz = ( RMIRemoteStreamClass ) map.get( clz );

        // class not in the thread-local cache
        if ( strclz == null )
        {
            // search in the global cache
            synchronized ( GLOBAL_CLASSES )
            {
                strclz = ( RMIRemoteStreamClass ) GLOBAL_CLASSES.get( clz );
            }
            // if found -> add to thread local cache
            if ( strclz != null )
            {
                map.put( clz, strclz );
            }
        }
        return strclz;
    }
}

