/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.openorb.util.HexPrintStream;

/**
 * This class can be used for debugging IIOP traffic on the socket
 * layer. Analyzing the network traffic with tools like Ethereal
 * make it hard to keep track of the traffic on the GIOP/IIOP layer.
 * Especially in a multi-threaded application where multiple threads
 * write on the same socket.
 * The class creates a folder in the os default temp directory:
 *    ${java.io.tmp}/${user.name}/&lt;TIMESTAMP&gt;/input
 * which contains the socket traffic files:
 *    [localhost].[localport]-[remotehost].[remoteport].[TIMESTAMP].log
 * Each files contains a history of which bytes have been read
 * by which thread from the stream.
 *
 * @author Michael Rumpf
 */
public class DebugSocketInputStream extends InputStream
{
    private static final SimpleDateFormat FILE_SDF
            = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss.SSS" );

    private static final SimpleDateFormat SDF
            = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

    private final InputStream m_parent;

    private final OutputStream m_fileStream;
    private final PrintWriter m_printWriter;
    private final HexPrintStream m_hexStream;

    /**
     * Constructor.
     *
     * @param parent The parent input stream.
     * @param timestamp The VM wide timestamp to identify this process.
     * @param description The socket description generated from the socket.
     */
    public DebugSocketInputStream( final InputStream parent, final long timestamp,
            final String description ) throws IOException
    {
        m_parent = parent;

        final File dir = getDirectory( timestamp );

        if ( !dir.exists() )
        {
            if ( !dir.mkdirs() )
            {
                if ( !dir.exists() )
                {
                    throw new IOException( "Couldn't create temporary folder: " + dir );
                }
            }
        }

        final File file = getFile( dir, description );

        m_fileStream = new FileOutputStream( file );
        m_printWriter = new PrintWriter( m_fileStream, true );
        m_hexStream = new HexPrintStream( m_fileStream, 2 );

        m_printWriter.println( "Type   : DebugSocketInputStream" );
        m_printWriter.print( "Socket : " );
        m_printWriter.println( description );
        m_printWriter.print( "Created: " );
        m_printWriter.println( SDF.format( new Date( System.currentTimeMillis() ) ) );
        m_printWriter.print( "Thread : " );
        m_printWriter.println( Thread.currentThread() );
        m_printWriter.println();
    }

    private static File getDirectory( final long timestamp ) throws IOException
    {
        final File javaIOTmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        final File userTmpDir = new File( javaIOTmpDir, System.getProperty( "user.name" ) );
        final File timestampDir = new File( userTmpDir,
                FILE_SDF.format(  new Date( timestamp ) ) );

        return new File( timestampDir, "input" );
    }


    private static File getFile( final File directory, final String description )
    {
        return new File( directory, "socket." + description + ".["
                + FILE_SDF.format( new Date( System.currentTimeMillis() ) ) + "].log" );
    }

    private void printHeading()
    {
        m_printWriter.println();
        m_printWriter.println( "---------------------------------------------------------" );
        m_printWriter.print( "Thread : " );
        m_printWriter.println( Thread.currentThread() );
        m_printWriter.print( "Time   : " );
        m_printWriter.println( SDF.format( new Date( System.currentTimeMillis() ) ) );
    }

    public int read() throws IOException
    {
        final int b = m_parent.read();

        printHeading();
        m_printWriter.print( "Data   : 1 Byte read, " );
        m_printWriter.print( b );
        m_printWriter.print( " '" );
        m_printWriter.print( ( char ) b );
        m_printWriter.print( "' 0x" );
        m_printWriter.println( Integer.toHexString( b ) );

        return b;
    }

    public int read( final byte[] buf ) throws IOException
    {
        final int result = m_parent.read( buf );

        printHeading();
        m_printWriter.print( "Data   : " );
        m_printWriter.print( result );
        m_printWriter.print( " Bytes read, buf.length="  );
        m_printWriter.println( buf.length );

        m_hexStream.write( buf );
        m_hexStream.flush();

        return result;
    }

    public int read( final byte[] buf, final int off, final int len ) throws IOException
    {
        final int result = m_parent.read( buf, off, len );

        printHeading();
        m_printWriter.print( "Data   : " );
        m_printWriter.print( result );
        m_printWriter.print( " Bytes read, buf.length="  );
        m_printWriter.print( buf.length );
        m_printWriter.print( ", off=" );
        m_printWriter.print( off );
        m_printWriter.print( ", len=" );
        m_printWriter.println( len );

        m_hexStream.write( buf, off, len );
        m_hexStream.flush();

        return result;
    }

    public void close() throws IOException
    {
        m_parent.close();

        printHeading();
        m_printWriter.println( "Closing InputStream" );

        m_hexStream.close();
        m_printWriter.close();
        m_fileStream.close();
    }
}

