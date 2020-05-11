/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import java.io.IOException;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA.IntHolder;

import org.openorb.orb.core.MinorCodes;

/**
 * This class can be used as a base class for buffer input stream
 * implementations. It handles buffer management.
 *
 * @author Chris Wood
 * @author Michael Rumpf
 */
public abstract class AbstractInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
    implements ExtendedInputStream
{
    /**
     * Current source buffer.
     */
    private StorageBuffer m_curr_buf;

    /**
     * Source of fragments for this buffer
     */
    private BufferSource m_source;

    /**
     * Pass back overread value
     */
    private int m_overread;

    /**
     * Exception thrown by all non IOStream operations if not null.
     */
    private org.omg.CORBA.SystemException m_cancel_exception;

    /**
     * Internal IntHolder instance. There is no need to allocate
     * a new instance each time, the value member of this instance
     * can be reset each time the holder is used.
     */
    private final IntHolder m_skip_len = new IntHolder( 0 );

    /**
     * Constructor.
     * Use the passed buffer to perform the stream IO
     * operations on.
     * @param buf The storage buffer used as input source,
     */
    public AbstractInputStream( StorageBuffer buf )
    {
        m_curr_buf = buf;
    }

    /**
     * Constructor.
     *
     * @param source The source of buffers for this input stream.
     */
    public AbstractInputStream( BufferSource source )
    {
        m_curr_buf = source.next();
        m_source = source;
    }

    /**
     * Get codebase associated with stream.
     * @return The contents of any TAG_JAVA_CODEBASE service
     * context when unmarshaling, or otherwise return null.
     */
    public String get_codebase()
    {
        return null;
    }

    /**
     * Read a single octet from a stream.
     * @return The actual number of bytes read or -1 for the end of
     * the stream rather than throwing an exception.
     */
    public int read()
        throws IOException
    {
        if ( m_overread > 0 )
        {
            return -1;
        }
        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }
        try
        {
            return ( int ) read_octet();
        }
        catch ( org.omg.CORBA.MARSHAL ex )
        {
            if ( m_overread > 0 )
            {
                return -1;
            }
            // no need to call cancel here, rethrowing the exception.
            throw ex;
        }
    }

    /**
     * Read array from stream.
     * @param val The array into which to read the data.
     * @return The actual number of bytes read or -1 for end of stream
     * rather than throwing an exception.
     */
    public int read( byte [] val )
    {
        return read( val, 0, val.length );
    }

    /**
     * Read array from stream.
     * @param val The array into which to read the data.
     * @param off The offset to skip from the beginning.
     * @param len The requested number of bytes to read.
     * @return The actual number of bytes read or -1 for end of
     * stream rather than throwing an exception.
     */
    public int read( byte [] val, int off, int len )
    {
        if ( m_overread > 0 )
        {
            return -1;
        }
        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }
        try
        {
            read_octet_array( val, off, len );
            return len;
        }
        catch ( org.omg.CORBA.MARSHAL ex )
        {
            if ( m_overread > 0 )
            {
                int ret = len - m_overread;
                return ( ret == 0 ) ? -1 : ret;
            }

            // no need to call cancel here, rethrowing the exception.
            throw ex;
        }
    }

    /**
     * Try to force skipping a number of bytes.
     * Throw a MARSHAL exception if skipping fails.
     * @param len The number of bytes to skip.
     */
    protected void force_skip( int len )
    {
        len -= skip( len );

        if ( len != 0 )
        {
            m_overread = len;
            // overreads are never reported to interceptors
            // and completion status for overreads will be maybe
            throw new org.omg.CORBA.MARSHAL( "Buffer overread by " + len + " bytes",
                    MinorCodes.MARSHAL_BUFFER_OVERREAD, CompletionStatus.COMPLETED_MAYBE );
        }
    }

    /**
     * Notify source of cancelation of input.
     * @param ex The exception which is used for notification.
     */
    protected void cancel( org.omg.CORBA.SystemException ex )
    {
        if ( m_source == null )
        {
            m_cancel_exception = ex;
            throw m_cancel_exception;
        }

        m_source.setException( ex );
        // this will throw an exception
        try
        {
            m_source.next();
        }
        catch ( org.omg.CORBA.SystemException nex )
        {
            m_cancel_exception = nex;
            throw nex;
        }

        org.openorb.orb.util.Trace.signalIllegalCondition( null, "Invalid state." );
    }

    /**
     * Available bytes in the input buffer. This can change upwards
     * as fragments arrive.
     * @return The number of available bytes.
     */
    public int available()
    {
        return m_curr_buf.available() + ( ( m_source != null ) ? m_source.available() : 0 );
    }

    /**
     * Check if the buffer supports position demarcation.
     * @return Always true.
     */
    public boolean markSupported()
    {
        return true;
    }

    /**
     * Set a mark at the current buffer position.
     * @param readlimit NOT USED
     */
    public void mark( int readlimit )
    {
        if ( m_source != null )
        {
            m_source.mark();
        }
        else
        {
            m_curr_buf.mark();
        }
    }

    /**
     * Reset the current position to the latest mark.
     * @throws IOException An IOException is thrown when
     * there is no mark where the buffer can be reset to.
     */
    public void reset()
        throws IOException
    {
        if ( m_source != null )
        {
            StorageBuffer ncurr = m_source.reset();

            if ( ncurr == null )
            {
                throw new IOException( "No marked position to reset to" );
            }
            m_curr_buf = ncurr;
        }
        else if ( !m_curr_buf.reset() )
        {
            throw new IOException( "No marked position to reset to" );
        }
        m_overread = 0;
    }

    /**
     * Skip over bytes in the input buffer.
     * @param count The number of bytes to skip.
     */
    public long skip( long count )
    {
        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }
        long total = count;

        if ( m_source != null )
        {
            if ( m_curr_buf == null )
            {
                return 0L;
            }
            while ( count > m_curr_buf.available() )
            {
                count -= m_curr_buf.available();

                try
                {
                    m_curr_buf = m_source.next();
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    m_cancel_exception = ex;
                    throw ex;
                }

                if ( m_curr_buf == null )
                {
                    return total - count;
                }
            }

            m_skip_len.value = ( int ) count;
            count -= m_curr_buf.skip( m_skip_len );

            if ( m_curr_buf.available() == 0 )
            {
                try
                {
                    m_curr_buf = m_source.next();
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    m_cancel_exception = ex;
                    throw ex;
                }
            }
        }
        else
        {
            m_skip_len.value = ( int ) count;
            count -= m_curr_buf.skip( m_skip_len );
        }

        return total - count;
    }

    /**
     * Handles obtaining new buffer if old one is empty.
     * @param dst The destination where the bytes should be copied to.
     * @param off The offset to skip on the source buffer.
     * @param len The request number of bytes from the buffer.
     * @return The number of actual bytes.
     */
    protected int next( OctetSeqHolder dst, IntHolder off, IntHolder len )
    {
        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }
        if ( len.value == 0 )
        {
            return 0;
        }
        // check for an overread from a BufferSource
        if ( m_curr_buf == null )
        {
            // overreads are never reported to interceptors
            // and completion status for overreads will be maybe
            m_overread = len.value;
            throw new org.omg.CORBA.MARSHAL( "Buffer overread by " + len.value + " bytes",
                    MinorCodes.MARSHAL_BUFFER_OVERREAD, CompletionStatus.COMPLETED_MAYBE );
        }

        int ret = m_curr_buf.next( dst, off, len );

        // if we have reached the end of the buffer try to get a new one.
        while ( ( m_source != null ) && ( m_curr_buf != null ) && ( m_curr_buf.available() == 0 ) )
        {
            try
            {
                m_curr_buf = m_source.next();
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                m_cancel_exception = ex;
                throw ex;
            }
        }
        if ( ret < 0
             && ( m_source == null
                  || m_curr_buf == null
                  || m_curr_buf.available() == 0 ) )
        {
            // report non-sourced buffer overreads.
            m_overread = len.value;
            throw new org.omg.CORBA.MARSHAL( "Buffer overread by " + len.value + " bytes",
                    MinorCodes.MARSHAL_BUFFER_OVERREAD, CompletionStatus.COMPLETED_MAYBE );
        }


        return ret;
    }
}
