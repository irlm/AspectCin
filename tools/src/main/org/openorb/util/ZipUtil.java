/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class provides utilities to manage a zip file.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/07/20 23:50:43 $
 */
public final class ZipUtil
{
    /**
     * Utility class. Do not instantiate.
     */
    private ZipUtil()
    {
    }

    /**
     * Create a zip file.
     *
     * @param name The name for the zip file to create.
     * @return A handle to the zip file.
     * @throws IOException When the FileOutputStream can not be created.
     */
    public static ZipHandle createZip( String name )
        throws IOException
    {
        return new ZipHandle( null, new ZipOutputStream( new FileOutputStream( name ) ) );
    }

    /**
     * Open a zip file.
     *
     * @param name The name for the zip file to open.
     * @return A handle to the zip file.
     * @throws IOException ????.
     */
    public static ZipHandle openZip( String name )
        throws IOException
    {
        return new ZipHandle( new ZipFile( new File( name ) ), null );
    }

    /**
     * Close a zip file
     *
     * @param handle The handle of the zip file to create.
     * @throws IOException When the handle can't be closed.
     */
    public static void closeZip( ZipHandle handle )
        throws IOException
    {
        if ( handle.getIn() != null )
        {
            handle.getIn().close();
        }
        if ( handle.getOut() != null )
        {
            handle.getOut().close();
        }
    }

    /**
     * This function builds a new zip file entry to add a file.
     *
     * @param name The name of the new entry.
     * @return The new zip file entry object.
     */
    private static ZipEntry newEntry( String name )
    {
        return new ZipEntry( name );
    }

    /**
     * This function adds a file to a zip file.
     *
     * @param src_name The name of the source file.
     * @param dst_name The name of the destination file.
     * @param dest The handle of the zip file into which to insert the file.
     * @throws IOException When the file can't be inserted into the zip file.
     */
    public static void insert( String src_name, String dst_name, ZipHandle dest )
        throws IOException
    {
        byte[] buffer = new byte[ 5000 ];
        int read;
        ZipOutputStream outputZip = dest.getOut();
        ZipEntry entry = newEntry( dst_name );
        outputZip.putNextEntry( entry );
        FileInputStream input = new FileInputStream( new File( src_name ) );
        do
        {
            read = input.read( buffer );
            outputZip.write( buffer, 0, read );
        }
        while ( read == 5000 );
        outputZip.closeEntry();
        input.close();
    }

    /**
     * This function adds a zip file entry to another zip.
     *
     * @param src_entry The entry in the source zip file.
     * @param inputZip The source zip file.
     * @param dst_name The name in the destination zip file.
     * @param outputZip The output stream of the destination zip file.
     * @throws IOException When the source can't be read or the destination
     * can't be written.
     */
    public static void addZipFileToZip( ZipEntry src_entry,
                                        ZipFile inputZip,
                                        String dst_name,
                                        ZipOutputStream outputZip )
        throws IOException
    {
        byte[] buffer = new byte[ 500 ];
        long read;
        ZipEntry dst_entry = newEntry( dst_name );
        outputZip.putNextEntry( dst_entry );
        InputStream input = inputZip.getInputStream( src_entry );
        long size = 0;
        long init = src_entry.getSize();
        if ( !src_entry.isDirectory() )
        {
            do
            {
                read = input.read( buffer );
                outputZip.write( buffer, 0, ( int ) read );
                size += read;
            }
            while ( size != init );
        }
        outputZip.closeEntry();
        input.close();
    }

    /**
     * Make a copy of a zip file.
     *
     * @param src The handle of the source zip file.
     * @param dst The handle of the destination zip file.
     * @param no_copy_list An array of filenames that should be left out
     * while copying.
     * @throws IOException When either the read or the write fails.
     */
    public static void copy( ZipHandle src, ZipHandle dst, String [] no_copy_list )
        throws IOException
    {
        Enumeration enumeration = src.getIn().entries();
        ZipEntry entry_src = null;
        while ( enumeration.hasMoreElements() )
        {
            entry_src = ( ZipEntry ) enumeration.nextElement();
            if ( notInList( no_copy_list, entry_src ) )
            {
                addZipFileToZip( entry_src, src.getIn(), entry_src.getName(), dst.getOut() );
            }
        }
    }

    /**
     * This function tests if a zip contains a file specified as parameter.
     *
     * @param handle The handle of the zip file to be tested.
     * @param file The file to check the zip file for.
     * @return True if the file exists in the zip file, false otherwise.
     */
    public static boolean containsFile( ZipHandle handle, String file )
    {
        return handle.getIn().getEntry( file ) != null;
    }

    /**
     * Extract a file and return its contents.
     *
     * @param handle The handle of the zip file.
     * @param file The file to extract.
     * @return The byte array of the extracted file.
     */
    public static byte [] getFileContent( ZipHandle handle, String file )
    {
        ZipEntry entry = handle.getIn().getEntry( file );
        byte [] content = null;
        try
        {
            InputStream input = handle.getIn().getInputStream( entry );
            content = new byte[ ( int ) entry.getSize() ];
            int b;
            int i = 0;
            while ( ( b = input.read() ) != -1 )
            {
                content[ i++ ] = ( byte ) b;
            }
            input.close();
        }
        catch ( IOException ex )
        {
            System.err.println( "IOException caught during read() or close(): " + ex );
            return null;
        }
        return content;
    }

    /**
     * This function tests if a entry is in a list.
     *
     * @param list The list of entries to check against.
     * @param entry The entry to check the list for.
     * @return True when the entry does not exist in the list, false
     * otherwise.
     */
    private static boolean notInList( String [] list, ZipEntry entry )
    {
        String name = entry.getName();
        if ( list == null )
        {
            return true;
        }
        for ( int i = 0; i < list.length; i++ )
        {
            if ( list[ i ].equals( name ) )
            {
                return false;
            }
        }
        return true;
    }
}

