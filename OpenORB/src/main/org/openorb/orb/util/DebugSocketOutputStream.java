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
 *  ${java.io.tmp}/${user.name}/&lt;TIMESTAMP&gt;/output
 * which contains the socket traffic files:
 *    [localhost].[localport]-[remotehost].[remoteport].[TIMESTAMP].log
 * Each files contains a history of which bytes have been written
 * by which thread to the stream.
 *
 * @author Michael Rumpf
 */
public class DebugSocketOutputStream extends OutputStream
{
    private static final SimpleDateFormat FILE_SDF
            = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss.SSS" );

    private static final SimpleDateFormat SDF
            = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

    private final OutputStream m_parent;

    private final FileOutputStream m_fileStream;
    private final PrintWriter m_printWriter;
    private final HexPrintStream m_hexStream;

    /**
     * Constructor.
     *
     * @param parent The parent output stream.
     * @param timestamp The VM wide timestamp to identify this process.
     * @param description The socket description generated from the socket.
     */
    public DebugSocketOutputStream( final OutputStream parent, final long timestamp,
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

        m_printWriter.println( "Type   : DebugSocketOutputStream" );
        m_printWriter.print( "Socket : " );
        m_printWriter.println( description );
        m_printWriter.print( "Created: " );
        m_printWriter.println( SDF.format( new Date( System.currentTimeMillis() ) ) );
        m_printWriter.print( "Thread : " );
        m_printWriter.println( Thread.currentThread() );
        m_printWriter.println();
    }

    private static File getDirectory( final long timestamp )
    {
        final File javaIOTmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        final File userTmpDir = new File( javaIOTmpDir, System.getProperty( "user.name" ) );
        final File timestampDir = new File( userTmpDir,
                FILE_SDF.format(  new Date( timestamp ) ) );

        return new File( timestampDir, "output" );
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

    public void write( final int b ) throws IOException
    {
        m_parent.write( b );

        printHeading();
        m_printWriter.print( "Data   : 1 Byte written, " );
        m_printWriter.print( b );
        m_printWriter.print( " '" );
        m_printWriter.print( ( char ) b );
        m_printWriter.print( "' 0x" );
        m_printWriter.println( Integer.toHexString( b ) );
    }

    public void write( final byte[] buf ) throws IOException
    {
        m_parent.write( buf );

        printHeading();
        m_printWriter.print( "Data   : " );
        m_printWriter.print( buf.length );
        m_printWriter.print( " Bytes written, buf.length="  );
        m_printWriter.println( buf.length );

        m_hexStream.write( buf );
        m_hexStream.flush();
    }

    public void write( final byte[] buf, final int off, final int len ) throws IOException
    {
        m_parent.write( buf, off, len );

        printHeading();
        m_printWriter.print( "Data   : " );
        m_printWriter.print( len );
        m_printWriter.print( " Bytes written, buf.length="  );
        m_printWriter.print( buf.length );
        m_printWriter.print( ", off=" );
        m_printWriter.print( off );
        m_printWriter.print( ", len=" );
        m_printWriter.println( len );

        m_hexStream.write( buf, off, len );
        m_hexStream.flush();
    }

    public void close() throws IOException
    {
        m_parent.close();

        printHeading();
        m_printWriter.println( "Closing OutputStream" );

        m_hexStream.close();
        m_printWriter.close();
        m_fileStream.close();
    }

    public void flush() throws IOException
    {
        m_parent.flush();

        printHeading();
        m_printWriter.println( "Flushing OutputStream" );
        m_printWriter.println( "#########################################################" );

        m_printWriter.flush();
    }
}
