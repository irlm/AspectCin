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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Non-clobbering file output stream. This behaves similarly to the standard
 * java.io.FileOutputStream, however if the file already exists it will not
 * touch the file unless the data being written is different to the current
 * file contents.
 *
 * Note that care must be taken to always close the output stream, if this
 * does not occour and the class is not garbage collected it can leave data
 * from the old file at the end of the new file.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/10 22:11:50 $
 */
public class DiffFileOutputStream extends OutputStream
{
    // state

    /**
     * The filename
     */
    private File m_file;

    /**
     * The random access file, the file being written to.
     */
    private RandomAccessFile m_io;

    /**
     * Write-through, when true the first diff has occoured and all
     * future writes should just write directly without checking.
     */
    private boolean m_wt = false;

    /**
     * File position. Note this may differ from the m_io's position, as
     * it uses the readahead.
     */
    private int m_pos = 0;

    /**
     * Allocation size of the compare buffer
     */
    private static final int RB_SIZE = 1024;

    /**
     * Readahead buffer.
     */
    private byte [] m_rb = new byte[RB_SIZE];

    /**
     * Index into the readahead buffer.
     */
    private int m_rbIdx = 0;

    /**
     * Size of the readahead buffer.
     */
    private int m_rbLen = 0;

    // constructors

    /**
     * Construct a new diff output stream
     *
     * @param file the file to be written to.
     * @throws FileNotFoundException if the file exists but is a directory
     *     rather than a regular file, does not exist but cannot be created,
     *     or cannot be opened for any other reason.
     */
    public DiffFileOutputStream( File file )
        throws FileNotFoundException
    {
        if ( !file.exists() )
        {
            m_io = new RandomAccessFile( file, "rw" );
            m_wt = true;
            return;
        }

        if ( !file.canWrite() )
        {
            throw new FileNotFoundException( "Cannot write to file '" + file + "'" );
        }

        // open for reading.
        m_file = file;
        m_io = new RandomAccessFile( file, "r" );
    }

    /**
     * Construct a new diff output stream
     *
     * @param filename the name of the file.
     * @throws FileNotFoundException if the file exists but is a directory
     *     rather than a regular file, does not exist but cannot be created,
     *     or cannot be opened for any other reason.
     */
    public DiffFileOutputStream( String filename )
        throws FileNotFoundException
    {
        this( new File( filename ) );
    }


    /**
     * Perform a non-clobbering copy. This will copy the contents of one file
     * into the other, but only if the two files differ.
     *
     * @param src the source file.
     * @param dest the destination file.
     * @return true if the file was clobbered.
     * @throws IOException if an IOException occours.
     */
    public static boolean copyFileNoClobber( File src, File dest )
        throws IOException
    {
        InputStream is = new FileInputStream( src );
        DiffFileOutputStream os = new DiffFileOutputStream( dest );

        byte [] buf = new byte[1024];
        for ( int len = 0; ( len = is.read( buf ) ) > 0; )
        {
            os.write( buf, 0, len );
        }
        is.close();
        os.close();

        return os.isWriting();
    }

    /**
     * Perform a non-clobbering copy. This will copy the contents of one file
     * into the other, but only if the two files differ.
     *
     * @param src the source file.
     * @param dest the destination file.
     * @return true if the file was clobbered.
     * @throws IOException if an IOException occours.
     */
    public static boolean copyFileNoClobber( String src, String dest )
        throws IOException
    {
        return copyFileNoClobber( new File( src ), new File( dest ) );
    }

    /**
     * Returns true if the output stream is now in write mode, and the file
     * is being overwritten.
     */
    public boolean isWriting()
    {
        return m_wt;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     *
     */
    public void write( int b )
        throws IOException
    {
        if ( checkReadahead( 1 ) )
        {
            m_io.write( b );
            ++m_pos;
            return;
        }

        // test the diff
        if ( m_rb[m_rbIdx] == ( byte ) ( b & 0xFF ) )
        {
            ++m_rbIdx;
            ++m_pos;
            return;
        }

        // write the byte
        openForWrite();
        m_io.write( b );
        ++m_pos;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this file output stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write( byte [] b, int off, int len )
        throws IOException
    {
        if ( off < 0 || len < 0 || b.length < ( off + len ) )
        {
            throw new IllegalArgumentException();
        }

        while ( len > 0 )
        {
            // get the next readahead buffer
            if ( checkReadahead( len ) )
            {
                m_io.write( b, off, len );
                m_pos += len;
                return;
            }

            // diff over the current readahead buffer
            for ( ; m_rbIdx < m_rbLen && len > 0; ++m_rbIdx, ++m_pos, ++off, --len )
            {
                if ( b[off] != m_rb[m_rbIdx] )
                {
                    openForWrite();
                    m_io.write( b, off, len );
                    m_pos += len;
                    return;
                }
            }
        }
    }

    /**
     * Closes the output stream and releases any system resources
     * associated with this stream. This file output stream may no longer
     * be used for writing bytes.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close()
        throws IOException
    {
        // check for truncation required.
        if ( m_io.length() > m_pos )
        {
            // in this case we truncate the file and close it.
            if ( !m_wt )
            {
                openForWrite();
            }
            m_io.setLength( m_pos );
        }

        m_io.close();
    }

    /**
     * Check the readahead buffer.
     */
    private boolean checkReadahead( long size )
        throws IOException
    {
        // writethrough mode
        if ( m_wt )
        {
            return true;
        }

        // buffer still contains data
        if ( ( m_rbLen - m_rbIdx ) > 0 )
        {
            return false;
        }

//        assert m_pos == m_io.getFilePointer()
//        : "The position and the file pointer should be equal";

        // check the size
        if ( m_io.length() < m_pos + size )
        {
            openForWrite();
            return true;
        }

        // read new buffer.
        m_rbIdx = 0;
        m_rbLen = m_io.read( m_rb );

//        assert m_rbLen > 0
//        : "The buffer length should be non-zero";

        return false;
    }

    /**
     * Reopen the file for writing.
     */
    private void openForWrite()
        throws IOException
    {
        m_io.close();

        m_io = new RandomAccessFile( m_file, "rw" );
        m_io.seek( m_pos );
        m_wt = true;
    }


    public static void main( String [] args )
        throws IOException
    {
        if ( args.length != 2 )
        {
            System.out.println(
              "usage: java org.openorb.util.DiffFileOutputStream <source> <destination>" );
            System.out.println(
              "Copies the file, if the files are the same the destination is not touched" );
            return;
        }

        copyFileNoClobber( args[0], args[1] );
    }
}

