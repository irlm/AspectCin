/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

/**
 * This class is a class loader for Java classes. It provides to OpenORB
 * the ability to load some of its classes by getting their path from
 * the OpenORB XML file.
 * The class loader has support for manifest file referenced archives.
 *
 * @author Jerome Daniel
 * @author Olivier Modica
 * @version $Revision: 1.6 $ $Date: 2004/02/08 12:23:51 $
 */
public class ClassLoader
    extends java.lang.ClassLoader
{
    /**
     * List of path
     */
    private java.util.Vector m_pathList;

    /**
     * Used Archive
     */
    private java.util.Hashtable m_usedArchive;

    /**
     * Constructor
     */
    public ClassLoader()
    {
        m_pathList = new java.util.Vector();
        m_usedArchive = new java.util.Hashtable();
    }

    /**
     * Add a class path to the path list.
     */
    public void addPath( String path )
    {
        java.util.StringTokenizer token = new java.util.StringTokenizer(
              path, java.io.File.pathSeparator );
        while ( token.hasMoreTokens() )
        {
            String nextToken = token.nextToken();
            m_pathList.addElement( nextToken );
            // Support for manifest file referenced class path
            if ( nextToken.endsWith( ".jar" ) )
            {
                addManifestReferences( nextToken );
            }
        }
    }

    /**
     * This operation reads the manifest file of a jar archive,
     * and add the contents the the path list.
     *
     * All entries are added relative to the current jar file path.
     * I.e. if the entry reads foobar.jar, it will be added at the
     * same path level than the current path
     *
     */
    private void addManifestReferences( String archive )
    {
        try
        {
            // Open the jar file
            java.util.jar.Manifest m = ( new java.util.jar.JarFile( archive ) ).getManifest();
            String basePath = archive.substring( 0,
                  archive.lastIndexOf( java.io.File.separator ) + 1 );
            if ( m == null )
            {
                return;
            }
            String paths = m.getMainAttributes().getValue( "Class-Path" );
            if ( paths == null )
            {
                return;
            }
            java.util.StringTokenizer token = new java.util.StringTokenizer( paths, " " );
            while ( token.hasMoreTokens() )
            {
                m_pathList.addElement( ( basePath + token.nextToken() ) );
            }
        }
        catch ( Exception ex )
        {
            // Just don't care because this is optional anyway
        }
    }

    /**
     * This operation is used to load a class
     */
    public Class loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        java.lang.Class clz = findLoadedClass( name );
        if ( clz == null )
        {
            try
            {
                clz = findSystemClass( name );
            }
            catch ( ClassNotFoundException ex )
            {
                // try other ways below
            }
        }
        if ( clz == null )
        {
            clz = findClass( name );
        }
        if ( resolve && clz != null )
        {
            resolveClass( clz );
        }
        if ( clz == null )
        {
            throw new ClassNotFoundException();
        }
        return clz;
    }

    /**
     * This operation is used to find a class
     */
    protected java.lang.Class findClass( String name )
    {
        byte [] content = null;
        // We are going to parse all path
        for ( int i = 0; i < m_pathList.size(); i++ )
        {
            String path = ( String ) m_pathList.elementAt( i );
            if ( path.endsWith( ".zip" ) || path.endsWith( ".jar" ) )
            {
                content = loadClassFromArchive( path, name_to_archive_class( name ) );
                if ( content != null )
                {
                    break;
                }
            }
            else
            {
                content = loadClassFromPath( path, name );
                if ( content != null )
                {
                    break;
                }
            }
        }
        if ( content == null )
        {
            return null;
        }
        return defineClass( name, content, 0, content.length );
    }

    /**
     * This operation is used to return a class content ( as a byte stream )
     * from an archive ( ZIP or JAR )
     */
    private byte [] loadClassFromArchive( String archive_name, String name )
    {
        try
        {
            boolean new_archive = false;
            org.openorb.util.ZipHandle archive = ( org.openorb.util.ZipHandle )
                m_usedArchive.get( archive_name );
            if ( archive == null )
            {
                // A new archive must be open
                try
                {
                    archive = org.openorb.util.ZipUtil.openZip( archive_name );

                    new_archive = true;
                }
                catch ( java.io.IOException ex )
                {
                    org.openorb.util.ZipUtil.closeZip( archive );
                    // Unable to open the zip file
                    return null;
                }
            }
            if ( !org.openorb.util.ZipUtil.containsFile( archive, name ) )
            {
                if ( new_archive )
                {
                    org.openorb.util.ZipUtil.closeZip( archive );
                }
                return null;
            }
            byte [] content = org.openorb.util.ZipUtil.getFileContent( archive, name );
            if ( new_archive )
            {
                m_usedArchive.put( archive_name, archive );
            }
            return content;
        }
        catch ( java.io.IOException ex )
        {
            return null;
        }
    }

    /**
     * Return a class name (for an archive)
     */
    private String name_to_archive_class( String name )
    {
        String file_name = name.replace( '.', '/' );
        return ( file_name + ".class" );
    }

    /**
     * Return a class name
     */
    private String name_to_class( String name )
    {
        String file_name = name.replace( '.', java.io.File.separatorChar );
        return ( file_name + ".class" );
    }

    /**
     * This operation is used to return
     */
    private byte [] loadClassFromPath( String path, String name )
    {
        if ( !path.endsWith( java.io.File.separator ) )
        {
            path = path + java.io.File.separator;
        }
        java.io.File file = new java.io.File( path + name_to_class( name ) );
        if ( !file.exists() )
        {
            return null;
        }
        try
        {
            java.io.FileInputStream input = new java.io.FileInputStream( file );
            long size = 0;
            java.util.Vector list = new java.util.Vector();
            byte [] packet = null;
            while ( true )
            {
                packet = new byte[ 2048 ];
                long read = input.read( packet, 0, packet.length );
                if ( read == -1 )
                {
                    break;
                }
                size += read;
                byte[] correctPacket = new byte[ ( int ) read ];
                System.arraycopy( packet, 0, correctPacket, 0, ( int ) read );
                list.addElement( correctPacket );
            }
            input.close();
            byte [] content = new byte[ ( int ) size ];
            int index = 0;
            for ( int i = 0; i < list.size(); i++ )
            {
                packet = ( byte [] ) list.elementAt( i );
                System.arraycopy( packet, 0, content, index, packet.length );
                index += packet.length;
            }
            return content;
        }
        catch ( java.lang.Throwable ex )
        {
            // An exception has been intercepted during the class loading
            return null;
        }
    }

    /**
     * Get system resource
     */
    public java.net.URL getResource( String name )
    {
        try
        {
            // try by using the default ClassLoader
            java.net.URL url = java.lang.ClassLoader.getSystemClassLoader().getResource( name );
            if ( url != null )
            {
                return url;
            }
            return findResource( name );
        }
        catch ( java.lang.Exception ex )
        {
            return null;
        }
    }

    /**
     * This operation is used to find a class
     */
    protected java.net.URL findResource( String name )
    {
        try
        {
            String resourcePath = null;
            // We are going to parse all path
            for ( int i = 0; i < m_pathList.size(); i++ )
            {
                String path = ( String ) m_pathList.elementAt( i );
                if ( path.endsWith( ".zip" ) || path.endsWith( ".jar" ) )
                {
                    resourcePath = loadResourceFromArchive( path, name );

                    if ( resourcePath != null )
                    {
                        break;
                    }
                }
                else
                {
                    resourcePath = loadResourceFromPath( path, name );

                    if ( resourcePath != null )
                    {
                        break;
                    }
                    break;
                }
            }
            if ( resourcePath == null )
            {
                return null;
            }
            return new java.net.URL( resourcePath );
        }
        catch ( java.lang.Exception ex )
        {
            return null;
        }
    }

    /**
     * This operation is used to return a class content ( as a byte stream )
     * from an archive ( ZIP or JAR )
     */
    private String loadResourceFromArchive( String archive_name, String name )
    {
        try
        {
            boolean new_archive = false;
            org.openorb.util.ZipHandle archive = ( org.openorb.util.ZipHandle )
                  m_usedArchive.get( archive_name );
            if ( archive == null )
            {
                // A new archive must be open
                try
                {
                    archive = org.openorb.util.ZipUtil.openZip( archive_name );
                    new_archive = true;
                }
                catch ( java.io.IOException ex )
                {
                    org.openorb.util.ZipUtil.closeZip( archive );
                    // Unable to open the zip file
                    return null;
                }
            }
            if ( !org.openorb.util.ZipUtil.containsFile( archive, name ) )
            {
                if ( new_archive )
                {
                    org.openorb.util.ZipUtil.closeZip( archive );
                }
                return null;
            }
            return new String( "jar:file:" + archive_name + "!/" + name );
        }
        catch ( java.io.IOException ex )
        {
            return null;
        }
    }

    /**
     * This operation is used to return
     */
    private String loadResourceFromPath( String path, String name )
    {
        if ( !path.endsWith( java.io.File.separator ) )
        {
            path = path + java.io.File.separator;
        }
        java.io.File file = new java.io.File( path + name );
        if ( !file.exists() )
        {
            return null;
        }
        return new String( "file:" + file.getAbsolutePath() );
    }
}

