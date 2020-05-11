/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA.IntHolder;
import org.openorb.orb.util.Trace;

/**
 * MarshalBuffers serve as a sink for data to be marshaled into. Fragmentation
 * of the data as it is marshaled can be controlled by the Listener registered
 * with the buffer at creation time.
 *
 * @author Unknown
 */
public class MarshalBuffer
{
   private static final boolean DEBUG_ENABLED =
         Boolean.getBoolean( "openorb.debug.enabled" );

   private Logger m_logger;

   private org.omg.CORBA.SystemException m_cancel_exception = null;

   private Scrap m_head;
   private Scrap m_tail;

   private OctetSeqHolder m_last_buffer = null;

   private boolean m_allow_fragment = false;
   private boolean m_in_fragment = false;

   private boolean m_header_fragment = true;
   private HeaderData m_last_header = null;

   private boolean m_block_fragment = true;
   private BlockData m_last_block = null;

   // informed when size grows and buffer is closed.
   private Listener m_listener = null;
   private Object m_listener_cookie = null;
   private boolean m_in_listener = false;

   // dummy buffer to marshal data into once cancel has been called.
   private byte [] m_ignore_buffer = null;

    /**
     * Construct marshal buffer without listener. Use the fragment and
     * lastFragment methods to extract data from the buffer.
     */
    public MarshalBuffer()
    {
        m_tail = new Scrap();
        m_head = m_tail;
        m_tail.m_fBuffer = new byte[ Scrap.SCRAP_SIZE_DEFAULT ];
        m_tail.m_fOffset = 0;
        m_tail.m_fLength = 0;
        m_tail.m_fMode = Scrap.SCRAP_MODE_NORMAL;
        m_tail.m_fPosition = 0;
    }

    /**
     * Construct marshall buffer with listener. The listener is informed
     * when the buffer grows or is closed, and calls the fragment or
     * lastFragment methods respectivly.
     */
    public MarshalBuffer( Listener listener, Object listenerCookie )
    {
        this();

        m_listener = listener;
        m_listener_cookie = listenerCookie;
    }

    /**
     * Enable logging for this class.
     *
     * @param logger The logger instance.
     */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    /**
     * Return the logger for this class.
     *
     * @return The logger instance.
     */
    protected Logger getLogger()
    {
        if ( null == m_logger )
        {
           Trace.signalIllegalCondition( null, "The logger has not been set!" );
        }
        return m_logger;
    }

    /**
     * Returns true if not connected to a listener. To get the data from
     * the buffer use lastFragment()
     */
    public boolean isStandalone()
    {
        return m_listener == null;
    }

    /**
     * Counts of all bytes inserted into the buffer, including previous
     * fragments.
     *
     * @return The number of all bytes inserted into this buffer.
     */
    public int size()
    {
        if ( !prealloc() )
        {
            return -1;
        }
        return m_tail.m_fPosition;
    }

    /**
     * Count of all bytes available for extracting into a fragment.
     *
     * @return The bumber fo bytes available for a fragment.
     */
    public int available()
    {
        if ( !prealloc() )
        {
            return -1;
        }
        return m_tail.m_fPosition - m_head.m_fPosition + m_head.m_fLength;
    }

    /**
     * Allow or dissallow fragmentation. An opertunity to send a fragment will
     * occour when this is true only after the next append to the buffer.
     */
    public void setAllowFragment( boolean allowFragment )
    {
        m_allow_fragment = allowFragment;
    }

    /**
     * Test if fragmentation is currently enabled. This may be false even after
     * a call to setAllowFragment(true) if fragmentation is disabled for some
     * other reason. While this returns false availIncreaced will not be called
     * on the listener
     */
    public boolean getAllowFragment()
    {
        return m_allow_fragment && m_header_fragment && m_block_fragment && !m_in_fragment;
    }

    /**
     * Alocate space at end of buffer. The destination scrap is modified so
     * its contents contain the allocated space. The allocated space is
     * considered to be scratch space, its buffer is only available
     * until the next call.
     *
     * @param buf Out parameter, holds pointer to scratch space on
     * return. This space should not be stored. The pointer will be
     * invalidated by setting it's value to null on next call.
     * @param off Out parameter, holds buffer offset on return.
     * @param len Length of requested buffer.
     */
    public void alloc( OctetSeqHolder buf, IntHolder off, int len )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'alloc(" + buf.value + ", " + off.value + ", "
                  + len + ")'..." );
        }

        if ( !prealloc() )
        {
            if ( m_ignore_buffer == null || m_ignore_buffer.length < len )
            {
                m_ignore_buffer = new byte[ len ];
            }
            buf.value = m_ignore_buffer;

            off.value = 0;

            return;
        }

        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( m_tail.m_fBuffer.length - m_tail.m_fLength - m_tail.m_fOffset >= len )
        {
            // tail contains both previously allocated space and
            // newly allocated space
            buf.value = m_tail.m_fBuffer;
            off.value = m_tail.m_fOffset + m_tail.m_fLength;

            m_tail.m_fLength += len;
            m_tail.m_fPosition += len;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Found space in tail element: " + m_tail );
            }
        }
        else
        {
            // previously allocated space in old tail
            // newly allocated space in new tail
            Scrap nex = new Scrap();

            if ( len > Scrap.SCRAP_SIZE_DEFAULT )
            {
                nex.m_fBuffer = new byte[ len ];
            }
            else
            {
                nex.m_fBuffer = new byte[ Scrap.SCRAP_SIZE_DEFAULT ];
            }
            nex.m_fOffset = 0;

            nex.m_fLength = len;

            nex.m_fMode = Scrap.SCRAP_MODE_NORMAL;

            nex.m_fPosition = m_tail.m_fPosition + len;

            buf.value = nex.m_fBuffer;

            off.value = 0;

            m_tail.m_fNext = nex;

            m_tail = nex;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Added new element: " + nex );
            }
        }

        m_last_buffer = buf;
    }

    /**
     * Attach a readonly scrap to the end of the buffer. This will be
     * stored by reference and will not be copied unless the StorageBuffer
     * it spawns uses readWriteMode when iterating over itself.
     */
    public void append( byte [] buf, int off, int len )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'append(" + buf + ", " + off + ", "
                  + len + ")'..." );
        }

        if ( !prealloc() )
        {
            return;
        }
        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( len == 0 )
        {
            return;
        }
        if ( len < Scrap.SCRAP_SIZE_DEFAULT )
        {
            // append by copy. Not worth doing otherwise, really!
            if ( m_tail.m_fBuffer.length - m_tail.m_fLength - m_tail.m_fOffset >= len )
            {
                // tail contains entire append buffer.
                System.arraycopy( buf, off,
                        m_tail.m_fBuffer, m_tail.m_fOffset + m_tail.m_fLength, len );

                m_tail.m_fLength += len;
                m_tail.m_fPosition += len;

                if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
                {
                    getLogger().debug( "Found space in tail element: " + m_tail );
                }
            }
            else
            {
                // use some of the old and some of the new.
                int rem = m_tail.m_fBuffer.length - m_tail.m_fLength - m_tail.m_fOffset;
                System.arraycopy( buf, off,
                        m_tail.m_fBuffer, m_tail.m_fOffset + m_tail.m_fLength, rem );

                m_tail.m_fLength += rem;
                m_tail.m_fPosition += rem;

                Scrap nex = new Scrap();
                nex.m_fBuffer = new byte[ Scrap.SCRAP_SIZE_DEFAULT ];
                nex.m_fOffset = 0;
                nex.m_fLength = len - rem;
                nex.m_fMode = Scrap.SCRAP_MODE_NORMAL;
                nex.m_fPosition = m_tail.m_fPosition + len - rem;

                System.arraycopy( buf, off + rem, nex.m_fBuffer, 0, len - rem );

                m_tail.m_fNext = nex;
                m_tail = nex;

                if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
                {
                    getLogger().debug( "Added new element: " + nex );
                }
            }

            return;
        }

        /* invariant: Tail scrap is always a normal buffer. */

        if ( m_tail.m_fLength == 0 )
        {
            // a special scrap has just been allocated, leaving an empty
            // scrap at the tail. Put readonly data in tail and move tail's
            // contents one forward.
            Scrap nex = new Scrap();
            nex.m_fBuffer = m_tail.m_fBuffer;
            nex.m_fOffset = m_tail.m_fOffset;
            nex.m_fLength = 0;
            nex.m_fMode = Scrap.SCRAP_MODE_NORMAL;
            nex.m_fPosition = m_tail.m_fPosition + len;

            m_tail.m_fBuffer = buf;
            m_tail.m_fOffset = off;
            m_tail.m_fLength = len;
            m_tail.m_fMode = Scrap.SCRAP_MODE_READONLY;
            m_tail.m_fPosition = nex.m_fPosition;

            m_tail.m_fNext = nex;
            m_tail = nex;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Added new element before 0-length tail: " + nex );
            }
        }
        else
        {
            // make current tail shared, append a readonly scrap then append
            // a normal scrap with the rest of the old tail's buffer.
            m_tail.m_fMode = Scrap.SCRAP_MODE_SHARED;

            Scrap ro = new Scrap();
            ro.m_fBuffer = buf;
            ro.m_fOffset = off;
            ro.m_fLength = len;
            ro.m_fMode = Scrap.SCRAP_MODE_READONLY;
            ro.m_fPosition = m_tail.m_fPosition + len;

            Scrap nex = new Scrap();
            nex.m_fBuffer = m_tail.m_fBuffer;
            nex.m_fOffset = m_tail.m_fOffset + m_tail.m_fLength;
            nex.m_fLength = 0;
            nex.m_fPosition = ro.m_fPosition;
            nex.m_fMode = Scrap.SCRAP_MODE_NORMAL;

            m_tail.m_fNext = ro;
            ro.m_fNext = nex;
            m_tail = nex;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Added two elements (RO, SHARED) to tail: " + ro + ", " + nex );
            }
        }

        postalloc();
    }

    /**
     * Insert padding.
     *
     * @param len The number of bytes to add.
     */
    public void pad( int len )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'pad(" + len + ")'..." );
        }

        if ( !prealloc() )
        {
            return;
        }
        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( len == 0 )
        {
            return;
        }
        int rem = m_tail.m_fBuffer.length - m_tail.m_fLength - m_tail.m_fOffset;

        if ( rem >= len )
        {
            // all the padding fits in the current scrap
            m_tail.m_fLength += len;
            m_tail.m_fPosition += len;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Found space in tail element: " + m_tail );
            }
        }
        else
        {
            // put some padding into current scrap
            m_tail.m_fLength += rem;
            m_tail.m_fPosition += rem;

            // and the rest into a new scrap.
            Scrap nex = new Scrap();
            nex.m_fBuffer = new byte[ Scrap.SCRAP_SIZE_DEFAULT ];
            nex.m_fOffset = 0;
            nex.m_fLength = len - rem;
            nex.m_fMode = Scrap.SCRAP_MODE_NORMAL;
            nex.m_fPosition = m_tail.m_fPosition + nex.m_fLength;

            m_tail.m_fNext = nex;
            m_tail = nex;

            if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Added new element: " + nex );
            }
        }

        postalloc();
    }

    /**
     * Add a header generator. Header generators generate data at a later time,
     * typically writing the length of the entire fragment or message when it
     * is about to be sent. Header generators also get an opportunity to write
     * to the beginning of a new fragment when a fragment is taken from the buffer.
     *
     * @param gen the header generator
     * @param len the length of the data which will be written by the generator.
     * @param frag true if the header allows fragmentation to occour within it's
     *         range of authority.
     * @param cookie passed to the begin message and end message operations.
     */
    public void addHeader( HeaderGenerator gen, int len, boolean frag, Object cookie )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'addHeader(" + gen + ", " + len + ", "
                  + frag + ", " + cookie + ")'..." );
        }

        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( len < 0 )
        {
            throw new IndexOutOfBoundsException();
        }
        if ( gen == null )
        {
            throw new NullPointerException();
        }
        if ( !prealloc() )
        {
            return;
        }
        HeaderData hd = new HeaderData();

        hd.m_previous = m_last_header;

        m_last_header = hd;

        hd.m_lastHeaderFragment = m_header_fragment;

        hd.m_position = m_tail.m_fPosition;

        hd.m_generator = gen;

        hd.m_cookie = cookie;

        // disable fragmentation while we add the header
        if ( len != 0 )
        {
            m_header_fragment = false;
            OctetSeqHolder bufh = new OctetSeqHolder();
            IntHolder offh = new IntHolder();

            alloc( bufh, offh, len );

            hd.m_buffer = bufh.value;
            hd.m_offset = offh.value;
            hd.m_length = len;
        }

        // update header fragmentability.
        m_header_fragment = hd.m_lastHeaderFragment && frag;
    }

    /**
     * Begin a block. Blocks unlike headers can overlap fragmentation boundaries
     * but can themselves be fragmented when the position in the buffer that they
     * hold is about to be sent. Every beginBlock operation is always paired with
     * an endBlock operation some time later in the stream.<p>
     * Nonfragmentable blocks can be nested, however fragmentable blocks cannot.
     *
     * @param gen the block generator.
     * @param len the length of the data which will be written.
     * @param frag true if the block can be fragmented.
     * @param cookie passed to the fragment block and end block operation.
     */
    public void beginBlock( BlockGenerator gen, int len, boolean frag, Object cookie )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'beginBlock(" + gen + ", " + len + ", "
                  + frag + ", " + cookie + ")'..." );
        }

        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( len < 0 )
        {
            throw new IndexOutOfBoundsException();
        }
        if ( gen == null )
        {
            throw new NullPointerException();
        }
        if ( !prealloc() )
        {
            return;
        }
        BlockData bd = new BlockData();

        bd.m_previous = m_last_block;

        bd.m_isFragment = ( m_last_block == null ) ? frag : false;

        m_last_block = bd;

        bd.m_position = m_tail.m_fPosition;

        bd.m_generator = gen;

        bd.m_cookie = cookie;

        if ( len != 0 )
        {
            // disable fragmentation while we add the header
            m_block_fragment = false;
            OctetSeqHolder bufh = new OctetSeqHolder();
            IntHolder offh = new IntHolder();

            alloc( bufh, offh, len );

            bd.m_buffer = bufh.value;
            bd.m_offset = offh.value;
            bd.m_length = len;
        }

        // update block fragmentablilty.
        m_block_fragment = bd.m_isFragment;
    }

    /**
     * Call the endBlock operation on the last block written with the beginBlock
     * operation and remove the hold on the data.
     */
    public void endBlock()
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'endBlock()'..." );
        }

        if ( m_in_listener && !m_in_fragment )
        {
            throw new IllegalStateException( "Cannot modify buffer while calling listener" );
        }
        if ( m_last_block == null )
        {
            throw new IllegalStateException( "End block without begin block" );
        }

        // size() calls prealloc() and maybe postalloc()!
        m_last_block.m_generator.endBlock( m_last_block.m_buffer, m_last_block.m_offset,
                m_last_block.m_length, size() - m_last_block.m_position,
                m_last_block.m_cookie );

        m_last_block = m_last_block.m_previous;

        m_block_fragment = ( m_last_block == null ) ? true : m_last_block.m_isFragment;
    }

    /**
     * Close. Calls close operation on listeners and frees all data
     * contained within the buffer. It is illegal to call close while within a
     * block.
     */
    public void close()
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'close()'..." );
        }

        if ( m_in_listener )
        {
            throw new IllegalStateException( "Cannot close buffer while calling listener." );
        }
        if ( m_last_block != null )
        {
            throw new IllegalStateException( "All blocks must be closed before closing buffer" );
        }
        if ( !prealloc() )
        {
            return;
        }
        if ( m_listener != null )
        {
            m_in_listener = true;
            try
            {
               m_listener.bufferClosed( this, m_tail.m_fPosition, m_listener_cookie );
            }
            finally
            {
                m_in_listener = false;
            }
        }

        m_head = null;
        m_tail = null;
        m_last_header = null;
    }

    /**
     * Cancel marshal. Calls cancel operation on listeners and frees all
     * data contained within the buffer. All following allocations will
     * either be silently discarded (if the listener does not throw an exception)
     * or will be responded to with the exception passed.
     */
    public void cancel( org.omg.CORBA.SystemException ex )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'cancel(" + ex + ")'..." );
        }

        if ( !prealloc() )
        {
            return;
        }
        if ( m_listener != null )
        {
            m_in_listener = true;
            try
            {
                m_listener.bufferCanceled( this, ex, m_listener_cookie );
            }
            catch ( org.omg.CORBA.SystemException sex )
            {
                m_cancel_exception = sex;
            }
            finally
            {
                m_in_listener = false;
            }
        }
        else
        {
            m_cancel_exception = ex;
        }
        m_head = null;
        m_tail = null;

        m_last_header = null;

        m_last_block = null;

        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }
    }

    /**
     * Prepare a fragment message.
     * The returned storage buffer will be exactly the length specified.
     */
    public StorageBuffer fragment( int len )
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'fragment(" + len + ")'..." );
        }

        if ( !prealloc() )
        {
            return null;
        }
        if ( m_listener != null && !m_in_listener )
        {
            throw new IllegalStateException( "Operation disallowed, must be called from listener" );
        }
        if ( !getAllowFragment() )
        {
            throw new IllegalStateException( "Buffer cannot currently be fragmented" );
        }
        if ( len > m_tail.m_fPosition - m_head.m_fPosition + m_head.m_fLength )
        {
            throw new IndexOutOfBoundsException();
        }
        m_in_fragment = true;

        // call fragment on the fragmentable block (if any)
        if ( m_last_block != null )
        {
            BlockData bd = m_last_block;
            m_last_block = null;
            bd.m_generator.fragmentBlock( bd.m_buffer, bd.m_offset, bd.m_length,
                    m_tail.m_fPosition - bd.m_position, this, bd.m_cookie );
        }

        int endpos = len + m_head.m_fPosition - m_head.m_fLength;

        // call close on all the header blocks.
        HeaderData firstHeader = null;
        HeaderData tmp;

        while ( m_last_header != null )
        {
            m_last_header.m_generator.endMessage( m_last_header.m_buffer,
                    m_last_header.m_offset, m_last_header.m_length,
                    true, endpos - m_last_header.m_position, m_last_header.m_cookie );
            tmp = m_last_header.m_previous;
            m_last_header.m_previous = firstHeader;
            firstHeader = m_last_header;
            m_last_header = tmp;
        }

        // create the buffer which will be returned.

        Scrap mid;

        if ( m_tail.m_fPosition - m_tail.m_fLength < endpos )
        {
            mid = m_tail;
        }
        else
        {
            for ( mid = m_head; mid.m_fPosition < endpos; mid = mid.m_fNext )
            {
                // do nothing
            }
        }
        Scrap nexthead;
        if ( mid.m_fPosition == endpos )
        {
            nexthead = mid.m_fNext;
            mid.m_fNext = null;
        }
        else
        {
            int olap = mid.m_fPosition - endpos;
            nexthead = new Scrap();
            nexthead.m_fMode = Scrap.SCRAP_MODE_SHARED | mid.m_fMode;
            nexthead.m_fBuffer = mid.m_fBuffer;
            nexthead.m_fPosition = mid.m_fPosition;
            nexthead.m_fOffset = mid.m_fOffset + mid.m_fLength - olap;
            nexthead.m_fLength = olap;
            nexthead.m_fNext = mid.m_fNext;

            mid.m_fLength = mid.m_fLength - olap;
            mid.m_fPosition = endpos;
            mid.m_fNext = null;
            mid.m_fMode = Scrap.SCRAP_MODE_SHARED | mid.m_fMode;
        }

        mid = null;
        StorageBuffer fragment = new StorageBuffer( m_head, len );

        // now begin the next message.
        m_tail = new Scrap();
        m_head = m_tail;
        m_tail.m_fBuffer = new byte[ Scrap.SCRAP_SIZE_DEFAULT ];
        m_tail.m_fOffset = 0;
        m_tail.m_fLength = 0;
        m_tail.m_fMode = Scrap.SCRAP_MODE_NORMAL;
        m_tail.m_fPosition = endpos;

        while ( firstHeader != null )
        {
            firstHeader.m_generator.beginMessage( this, firstHeader.m_cookie );
            firstHeader = firstHeader.m_previous;
        }

        // invalidate any last buffer.
        prealloc();

        int addHeader = 0;

        // append the overlapping data.
        if ( nexthead != null )
        {
            addHeader = m_tail.m_fPosition + nexthead.m_fLength - nexthead.m_fPosition;

            if ( m_tail.m_fLength == 0 )
            {
                m_tail.m_fBuffer = nexthead.m_fBuffer;
                m_tail.m_fLength = nexthead.m_fLength;
                m_tail.m_fOffset = nexthead.m_fOffset;
                m_tail.m_fMode = nexthead.m_fMode;
                m_tail.m_fNext = nexthead.m_fNext;
                m_tail.m_fPosition += nexthead.m_fLength;
                nexthead = nexthead.m_fNext;
            }
            else
            {
                m_tail.m_fNext = nexthead;
            }

            // update the positions of the old buffers.

            while ( nexthead != null )
            {
                nexthead.m_fPosition += addHeader;
                m_tail = nexthead;
                nexthead = nexthead.m_fNext;
            }
        }

        BlockData bd = m_last_block;

        while ( bd != null )
        {
            bd.m_position += addHeader;
            bd = bd.m_previous;
        }

        m_in_fragment = false;

        // done, return the message fragment.
        return fragment;
    }

    /**
     * Return the last fragment. The size of the last fragment will be less
     * than the default MAX_FRAGMENT_SIZE. This also closes the buffer.
     */
    public StorageBuffer lastFragment()
    {
        if ( DEBUG_ENABLED && getLogger().isDebugEnabled() && Trace.isMedium() )
        {
            getLogger().debug( "Entering method 'lastFragment()'..." );
        }

        if ( !prealloc() )
        {
            return null;
        }
        if ( m_listener != null && !m_in_listener )
        {
            throw new IllegalStateException(
                    "Operation disallowed, must be called from listener. Call close operation" );
        }
        if ( m_last_block != null )
        {
            throw new IllegalStateException(
                    "Attempt to close buffer without closing all blocks" );
        }

        // fill in the headers.
        while ( m_last_header != null )
        {
            m_last_header.m_generator.endMessage( m_last_header.m_buffer,
                    m_last_header.m_offset, m_last_header.m_length,
                    false, m_tail.m_fPosition - m_last_header.m_position,
                    m_last_header.m_cookie );
            m_last_header = m_last_header.m_previous;
        }

        Scrap msghead = m_head;
        int len = m_tail.m_fPosition - m_head.m_fPosition + m_head.m_fLength;

        // go to closed state.
        m_tail = null;
        m_head = null;
        return new StorageBuffer( msghead, len );
    }

    /**
     * Interface HeaderGenerator.
     */
    public static interface HeaderGenerator
    {
        /**
         * Called when message is about to get sent. Modifications can be made
         * to the bytes allocated at the addHeader stage.
         *
         * @param buf buffer containing the reserved bytes. Not all of the buffer
         *            is considered to be read-write.
         * @param pos offset into buf of first modifiable byte.
         * @param len length of modifiable bytes.
         * @param fragment true if this is called as a response to the fragment
         *            operation.
         * @param length length in bytes between the position that addHeader was
         *            called and the end of the message.
         * @param cookie the cookie passed to the addHeader operation.
         */
        void endMessage( byte [] buf, int pos, int len,
                boolean fragment, int length, Object cookie );

        /**
         * Called to begin a new fragment. Writes may be made to the marshal buffer,
         * including adding a new header.
         *
         * @param buffer the buffer getting marshaled to.
         * @param cookie the cookie passed to the addHeader operation.
         */
        void beginMessage( MarshalBuffer buffer, Object cookie );
    }

    /**
     * Interface BlockGenerator.
     */
    public static interface BlockGenerator
    {
        /**
         * Called when endBlock operation is called.
         *
         * @param buf buffer containing the reserved bytes. Not all of the buffer
         *            is considered to be read-write.
         * @param pos offset into buf of first modifiable byte.
         * @param len length of modifiable bytes.
         *
         * @param length length in bytes between the position that beginBlock was
         *            called and the end of the block.
         * @param cookie the cookie passed to the addHeader operation.
         */
        void endBlock( byte [] buf, int pos, int len,
                              int length, Object cookie );

        /**
         * Called when fragment is called and a block will be fragmented. Writes
         * may be made to the MarshalBuffer, including begining a new block.
         *
         * @param buf buffer containing the reserved bytes. Not all of the buffer
         *            is considered to be read-write.
         * @param pos offset into buf of first modifiable byte.
         * @param len length of modifiable bytes.
         *
         * @param length length in bytes between the position that beginBlock was
         *            called and the end of the block.
         * @param buffer the marshal buffer.
         * @param cookie the cookie passed to the addHeader operation.
         */
        void fragmentBlock( byte [] buf, int pos, int len,
                                   int length, MarshalBuffer buffer, Object cookie );
    }

    /**
     * This interface is used by the MarshalBuffer to send important messages
     * to a registered listener.
     *
     * The messages are:
     * <ul>
     *     <li>availIncreased</li>
     *     <li>bufferClosed</li>
     *     <li>bufferCanceled</li>
     * </ul>
     */
    public static interface Listener
        extends java.util.EventListener
    {
        /**
         * Called whenever the size of the buffer increases or flush is called
         * while fragemntation is enabled.
         *
         * @param buffer The marshal buffer instance for which this method
         * is called.
         * @param available The number of bytes stored in the buffer.
         * @param cookie The cookie passed to the addHeader operation.
         */
        void availIncreaced( MarshalBuffer buffer, int available, Object cookie );

        /**
         * Called when the buffer is closed.
         *
         * @param buffer The marshal buffer instance for which this method
         * is called.
         * @param available The number of bytes stored in the buffer.
         * @param cookie The cookie passed to the addHeader operation.
         */
        void bufferClosed( MarshalBuffer buffer, int available, Object cookie );

        /**
         * Called when the marshal sequence is canceled by calling the cancel
         * operation. This should either throw a system exception or simply return
         * if the cancel will be handled later on.
         *
         * @param buffer The marshal buffer instance for which this method
         * is called.
         * @param ex The system exception to be thrown.
         * @param cookie The cookie passed to the addHeader operation.
         */
        void bufferCanceled( MarshalBuffer buffer,
                org.omg.CORBA.SystemException ex, Object cookie );
    }

    /**
     * The prealloc method is called for each method that allocates another
     * piece of buffer memory. It performs various checks that must be done for
     * each buffer operation in order not to destroy buffer consistency.
     * When the member m_cancel_exception is set the exception will be
     * thrown by this method.
     *
     * @return True when the method, from which prealloc is called, may
     * continue, false otherwise.
     */
    private boolean prealloc()
    {
        if ( m_cancel_exception != null )
        {
            throw m_cancel_exception;
        }

        // No head element has been allocated yet
        if ( m_head == null )
        {
            return false;
        }

        // Invalidate last allocated buffer
        if ( m_last_buffer != null )
        {
            m_last_buffer.value = null;
            m_last_buffer = null;
            postalloc();
        }

        return true;
    }

    /**
     * This method calls the Listener.availIncreaced method after the
     * buffer memory has been increased.
     */
    private void postalloc()
    {
        // check for fragment request
        if ( m_listener != null
              && m_allow_fragment
              && m_header_fragment
              && m_block_fragment
              && !m_in_fragment )
        {
            m_in_listener = true;
            try
            {
               m_listener.availIncreaced(
                     this,
                     m_tail.m_fPosition - m_head.m_fPosition + m_head.m_fLength,
                     m_listener_cookie );
            }
            finally
            {
               m_in_listener = false;
            }
        }
    }

    private static class HeaderData
    {
        private HeaderData m_previous;
        private boolean m_lastHeaderFragment;

        private int m_position;
        private HeaderGenerator m_generator;
        private Object m_cookie;

        private byte [] m_buffer;
        private int m_offset;
        private int m_length;
    }

    private static class BlockData
    {
        private BlockData m_previous;
        private boolean m_isFragment;

        private int m_position;
        private BlockGenerator m_generator;
        private Object m_cookie;

        private byte [] m_buffer;
        private int m_offset;
        private int m_length;
    }

    public static void main( String [] args )
    {
        MarshalBuffer mbuf = new MarshalBuffer();

        OctetSeqHolder buf = new OctetSeqHolder();
        IntHolder pos = new IntHolder();
        int len = 1;

        for ( int i = 0; i < 3000; i += len )
        {
            mbuf.alloc( buf, pos, len );

            for ( int j = 0; j < len; ++j )
            {
                buf.value[ pos.value + j ] = ( byte ) ( ( i + j ) % 121 );
            }
        }

        StorageBuffer sbuf = mbuf.lastFragment();

        System.out.println( "available = " + sbuf.available() );

        /*
        org.openorb.util.HexPrintStream hps = new org.openorb.util.HexPrintStream(
              System.out, HexPrintStream.FORMAT_MIXED );
        sbuf.mark();
        try
        {
            sbuf.writeTo( hps );
            hps.flush();
        }
        catch ( java.io.IOException ex )
        {
            ex.printStackTrace( System.out );
            System.out.println( ex );
        }
        sbuf.reset();
        */

        /*
        sbuf.mark();
        IntHolder rlen = new IntHolder();
        for ( int i = 0; i < 3000; )
        {
            rlen.value = 1024;
            len = sbuf.next( buf, pos, rlen );
            for ( int j = 0; j < len; ++j )
            {
                if ( buf.value[ pos.value + j ] != ( byte ) ( ( i + j ) % 121 ) )
                {
                    System.out.println( "Error at index " + ( i + j ) );
                }
                i += len;
            }
        }
        sbuf.reset();
        */

        System.out.println( "available = " + sbuf.available() );
        byte [] lin = sbuf.linearize();

        for ( int i = 0; i < lin.length; ++i )
        {
            if ( lin[ i ] != ( byte ) ( i % 121 ) )
            {
                System.out.println( "Error at index " + i );
            }
        }
    }
}

