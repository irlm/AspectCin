/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.URLClassLoader;

import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.TCKind;

import org.openorb.orb.core.typecode.TypeCodeBase;

import org.openorb.orb.io.MarshalBuffer;

import org.openorb.orb.rmi.RMIObjectStreamClass;
import org.openorb.orb.rmi.ValueHandlerImpl;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;
import org.openorb.util.IdentityKey;
import org.openorb.util.NumberCache;
import org.openorb.util.RepoIDHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

/**
 * This class implements CDR for IIOP
 *
 * @author Chris Wood
 * @version $Revision: 1.17 $ $Date: 2004/08/20 10:08:12 $
 */
public class CDROutputStream
    extends org.omg.CORBA_2_3.portable.OutputStream
    implements org.openorb.orb.io.ExtendedOutputStream, LogEnabled
{
    // --------------------------------------------------------------------------
    // Constant for Value Type marshalling / unmarshalling
    // --------------------------------------------------------------------------

    private static final int NO_CODEBASE = 0;
    private static final int CODEBASE = 1;
    private static final int NO_TYPE_INFORMATION = 0;
    private static final int SINGLE_TYPE_INFORMATION = 2;
    private static final int MULTIPLE_TYPE_INFORMATION = 6;
    private static final int CHUNK = 8;
    private static final int NO_CHUNK = 0;

    private static final int CUSTOM_MARSHAL = 1;
    private static final int STREAM_MARSHAL = 2;
    private static final int VALBOX_MARSHAL = 3;
    private static final int EXTENDED_MARSHAL = 4;

    // --------------------------------------------------------------------------
    // Internal data for marshalling management
    // --------------------------------------------------------------------------

    // lifetime data

    /**
     * Reference to orb.
     */
    private org.omg.CORBA.ORB m_orb;

    /**
     * CDR protocol version
     */
    private org.omg.GIOP.Version m_version;

    /**
     * Marshall buffer
     */
    private MarshalBuffer m_buf;

    /**
     * Encoding for char data.
     */
    private String m_char_enc = "ISO-8859-1";

    /**
     * Encoding for wchar data.
     */
    private String m_wchar_enc = "UnicodeBigUnmarked";

    /**
     * Alignment for wchar data.
     */
    private int m_wchar_align = 2;

    // transient data.

    /**
     * The current index in the buffer
     */
    private int m_index = 0;

    /**
     * pending alignment.
     */
    private int m_pending_align = 0;

    /**
     * Realignment value. Used in encapsulations
     */
    private int m_realign = 0;

    /**
     * Old realignment values.
     */
    private LinkedList m_old_realign;

    /**
     * Level of value type encoding.
     */
    private int m_value_level = 0;

    /**
     * True if value level one is chunked
     */
    private boolean m_in_chunked_value = false;

    /**
     * If true at least one valuetype has been marshalled and the below
     * Map objects have been created.
     */
    private boolean m_value_init = false;

    /**
     * Indexes of previously marshalled values
     */
    private Map m_value_idx;

    /**
     * Indexes of previously marshalled urls
     */
    private Map m_url_idx;

    /**
     * Indexes of previously marshalled typecode lists (hashed on value class)
     */
    private Map m_typecode_list_idx;

    /**
     * Indexes of previously marshalled typecodes
     */
    private Map m_typecode_idx;

    /**
     * pending levels of value chunk closure.
     */
    private int m_pending_value_closes = 0;

    /**
     * pending reopen of value chunk. Always align on before setting this
     * to be true (automatic if following a close)
     */
    private boolean m_pending_value_open = false;

    /**
     * tempory vars. here to avoid creating new every time
     */
    private OctetSeqHolder m_tmp_buf = new OctetSeqHolder();
    private IntHolder m_tmp_off = new IntHolder();

    /** Caching of the ORB string property openorb.URLCodeBase */
    private String m_system_urls = null;

    /**
     * Reference to the ValueHandler.
     */
    private static ValueHandler s_handler;

    /**
     * Info about the currently being written value.
     */
    private RMIObjectStreamClass m_current_value;

    /**
     * The logger instance.
     */
    private Logger m_logger;

    //====================================================================
    // LogEnabled
    //====================================================================

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    protected Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }

    // ----------------------------------------------------------------------
    // Internal singleton classes for controling encapsulations and
    // value chunks
    // ----------------------------------------------------------------------

    private static final MarshalBuffer.BlockGenerator ENCAPS_GEN =
        new MarshalBuffer.BlockGenerator()
        {
            /**
             * Called when endBlock operation is called.
             *
             * @param buf buffer containing the reserved bytes. Not all of the buffer
             *            is considered to be read-write.
             * @param pos offset into buf of first modifiable byte.
             * @param len length of modifiable bytes.
             * @param size length in bytes between the position that beginBlock was
             *            called and the end of the block.
             * @param cookie the cookie passed to the addHeader operation.
             */
            public void endBlock( byte [] buf, int pos, int len,
                                  int size, Object cookie )
            {
                size -= 4;
                buf[ pos ] = ( byte ) ( size >>> 24 );
                buf[ pos + 1 ] = ( byte ) ( size >>> 16 );
                buf[ pos + 2 ] = ( byte ) ( size >>> 8 );
                buf[ pos + 3 ] = ( byte ) size;
            }

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
            public void fragmentBlock( byte [] buf, int pos, int len,
                                       int length, MarshalBuffer buffer, Object cookie )
            {
                org.openorb.orb.util.Trace.signalIllegalCondition( null,
                        "Unable to fragment encapsulation." );
            }
        };

    private static final MarshalBuffer.BlockGenerator CHUNK_GEN =
        new MarshalBuffer.BlockGenerator()
        {
            /**
             * Called when endBlock operation is called.
             *
             * @param buf buffer containing the reserved bytes. Not all of the buffer
             *            is considered to be read-write.
             * @param pos offset into buf of first modifiable byte.
             * @param len length of modifiable bytes.
             * @param size length in bytes between the position that beginBlock was
             *            called and the end of the block.
             * @param cookie the cookie passed to the addHeader operation.
             */
            public void endBlock( byte [] buf, int pos, int len,
                                  int size, Object cookie )
            {
                size -= 4;

                if ( !( size > 0 ) )
                {
                    org.openorb.orb.util.Trace.signalIllegalCondition( null,
                            "No bytes left in block." );
                }

                buf[ pos ] = ( byte ) ( size >>> 24 );
                buf[ pos + 1 ] = ( byte ) ( size >>> 16 );
                buf[ pos + 2 ] = ( byte ) ( size >>> 8 );
                buf[ pos + 3 ] = ( byte ) size;
            }

            /**
             * Called when fragment is called and a block will be fragmented. Writes
             * may be made to the MarshalBuffer, including begining a new block.
             *
             * @param buf buffer containing the reserved bytes. Not all of the buffer
             *            is considered to be read-write.
             * @param off offset into buf of first modifiable byte.
             * @param len length of modifiable bytes.
             * @param size length in bytes between the position that beginBlock was
             *            called and the end of the block.
             * @param buffer the marshal buffer.
             * @param cookie the cookie passed to the addHeader operation.
             */
            public void fragmentBlock( byte [] buf, int off, int len,
                                       int size, MarshalBuffer buffer, Object cookie )
            {
                size -= 4;

                if ( !( size > 0 ) )
                {
                    org.openorb.orb.util.Trace.signalIllegalCondition( null,
                            "No bytes left in block." );
                }

                buf[ off ] = ( byte ) ( size >>> 24 );
                buf[ off + 1 ] = ( byte ) ( size >>> 16 );
                buf[ off + 2 ] = ( byte ) ( size >>> 8 );
                buf[ off + 3 ] = ( byte ) size;

                // Set pending chunk header
                ( ( CDROutputStream ) cookie ).m_pending_value_open = true;
            }
        };

    // --------------------------------------------------------------------------
    // Constructors
    // --------------------------------------------------------------------------

    /**
     * Constructor used by message level, fragments may be sent as
     * elements are appended to the stream, so create_input_stream is
     * disallowed. Extending classes must implement a constructor with this exact
     * signature.
     */
    public CDROutputStream( org.omg.CORBA.ORB orb, org.omg.GIOP.Version version, MarshalBuffer buf )
    {
        m_orb = orb;
        m_version = version;
        m_buf = buf;
        if ( m_version.minor == 0 )
        {
            m_char_enc = "ISO-8859-1";
            m_wchar_enc = null;
        }

        // get a value handler instance
        s_handler = ValueHandlerImpl.createValueHandler(
              getLogger().getChildLogger( "vh" ) );
    }

    public MarshalBuffer getMarshalBuffer()
    {
        return m_buf;
    }

    public void setCodesets( int tcsc, int tcsw )
    {
        if ( tcsc != 0 )
        {
            m_char_enc = CodeSetDatabase.getNameFromId( tcsc );

            if ( m_char_enc == null || CodeSetDatabase.getAlignmentFromId( tcsc ) > 1 )
            {
                m_buf.cancel( new org.omg.CORBA.CODESET_INCOMPATIBLE() );
                return;
            }
        }

        if ( tcsw == 0 )
        {
            m_wchar_enc = null;
        }
        else
        {
            m_wchar_enc = CodeSetDatabase.getNameFromId( tcsw );
            m_wchar_align = CodeSetDatabase.getAlignmentFromId( tcsw );

            if ( m_wchar_enc == null || ( m_version.minor == 1 && m_wchar_align != 2 ) )
            {
                m_buf.cancel( new org.omg.CORBA.CODESET_INCOMPATIBLE() );
                return;
            }
        }

        if ( getLogger().isDebugEnabled() && Trace.isHigh() )
        {
            getLogger().debug( "New encodings for output stream set to ["
                  + m_char_enc + "] and [" + m_wchar_enc + "]." );
        }
    }

    /**
     * Used to insert header blocks
     */
    void addHeader( MarshalBuffer.HeaderGenerator gen, int len, boolean frag, Object cookie )
    {
        m_buf.addHeader( gen, len, frag, cookie );
        m_index += len;
    }

    /**
     * Allow fragmentation
     */
    void allowFragment()
    {
        m_buf.setAllowFragment( true );
    }

    /**
     * Get the orb associated with the stream.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
      * Return the CDR protocol version
      */
    public org.omg.GIOP.Version version()
    {
        return m_version;
    }

    /**
     * Set alignment into the buffer
     */
    public void alignment( int align )
    {
        // do pending chunk closes
        if ( m_pending_value_closes > 0 )
        {
            value_end_block();

            if ( m_in_chunked_value )
            {
                m_pending_value_open = true;
            }
        }

        // reopen chunk if neccicary.
        if ( m_pending_value_open )
        {
            m_pending_value_open = false;
            value_begin_block();
        }

        // Check for alignment
        if ( align > 1 || m_pending_align > 0 )
        {
            if ( align < m_pending_align )
            {
                align = m_pending_align;
                m_pending_align = 0;
            }

            int tmp = ( m_index - m_realign ) % align;

            if ( tmp != 0 )
            {
                int delta = align - tmp;
                m_buf.pad( delta );
                m_index += delta;
            }
        }
    }

    /**
     * The next call to align will result in aligning to the specified boundary
     * if the reuested alignment is smaller.
     */
    public void pending_alignment( int align )
    {
        m_pending_align = align;
    }

    /**
     * Current index into the buffer.
     */
    public int index()
    {
        return m_index;
    }

    /**
     * Begin an encapsulated block. This must be paired with an end_encapsulation
     * later in the buffer.
     */
    public void begin_encapsulation()
    {
        alignment( 4 );

        m_buf.beginBlock( ENCAPS_GEN, 4, false, null );
        m_index += 4;

        if ( m_old_realign == null )
        {
            m_old_realign = new LinkedList();
        }
        m_old_realign.addLast( NumberCache.getInteger( m_realign ) );

        m_realign = ( m_index - m_realign ) % 8;

        write_boolean( false );
    }

    /**
     * End an encapsulated block.
     */
    public void end_encapsulation()
    {
        if ( m_old_realign == null || m_old_realign.isEmpty() )
        {
            throw new IllegalStateException( "No current encapsulation" );
        }
        m_buf.endBlock();

        m_realign = ( ( Integer ) m_old_realign.removeLast() ).intValue();
    }

    // --------------------------------------------------------------------------
    // java.io.OutputStream interface implementation
    // --------------------------------------------------------------------------

    /**
     * Write a simple value
     */
    public void write( int val )
    {
        write_octet( ( byte ) val );
    }

    /**
     * Write a buffer into the stream
     */
    public void write( byte[] buf )
    {
        write_octet_array( buf, 0, buf.length );
    }

    /**
     * Write a buffer into the stream
     */
    public void write( byte[] buf, int off, int len )
    {
        write_octet_array( buf, off, len );
    }

    /**
     * Complete marshaling a message. Once this is called the message will be sent.
     */
    public void close()
    {
        if ( m_old_realign != null && !m_old_realign.isEmpty() )
        {
            m_buf.cancel( new org.omg.CORBA.MARSHAL(
                    "closed stream without closing all encapsulation layers",
                    IIOPMinorCodes.MARSHAL_ENCAPS, CompletionStatus.COMPLETED_MAYBE ) );
            return;
        }

        if ( m_value_level - m_pending_value_closes > 0 )
        {
            m_buf.cancel( new org.omg.CORBA.MARSHAL(
                    "closed stream before value completley marshalled",
                    IIOPMinorCodes.MARSHAL_VALUE, CompletionStatus.COMPLETED_MAYBE ) );
            return;
        }

        // write any pending end tags.
        if ( m_pending_value_closes > 0 )
        {
            value_end_block();
        }
        m_buf.close();
    }

    /**
     * Does nothing.
     */
    public void flush()
    {
    }

    // --------------------------------------------------------------------------
    // OuputStream interface implementation
    // --------------------------------------------------------------------------

    /**
     * This operation is not implemented. Use the
     * {@link org.openorb.orb.io.ListOutputStream} class if typed streaming is
     * required or read fragments from an underlying marshal buffer for byte
     * oriented data.
     */
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        m_buf.cancel( new org.omg.CORBA.NO_IMPLEMENT() );
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Cancel a marshal operation.
     */
    void cancel( org.omg.CORBA.SystemException ex )
    {
        m_buf.cancel( ex );
    }

    /**
     * Append boolean value
     */
    public void write_boolean( boolean val )
    {
        alignment( 1 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, 1 );
        m_index += 1;

        m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) ( val ? 1 : 0 );
    }

    /**
     * Add an IDL octet into the stream
     */
    public void write_octet( byte val )
    {
        alignment( 1 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, 1 );
        m_index += 1;

        m_tmp_buf.value[ m_tmp_off.value ] = val;
    }

    /**
     * Add an IDL char into the steam.
     */
    public void write_char( char val )
    {
        try
        {
            byte [] d = String.valueOf( val ).getBytes( m_char_enc );

            if ( d.length != 1 )
            {
                m_buf.cancel( new org.omg.CORBA.DATA_CONVERSION( "Bad char type",
                        org.omg.CORBA.OMGVMCID.value | 1,
                        org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE ) );
            }
            alignment( 1 );

            m_buf.append( d, 0, 1 );

            m_index += 1;
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }

    /**
     * Add an IDL wide char into the stream
     */
    public void write_wchar( char val )
    {
        if ( m_wchar_enc == null )
        {
            if ( m_version.minor == 0 )
            {
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }
            else
            {
                m_buf.cancel( new org.omg.CORBA.INV_OBJREF( "Missing wchar encoder.",
                        IIOPMinorCodes.INV_OBJREF_MISSING_ENCODER,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }
            return;
        }

        try
        {
            byte [] d;

            switch ( m_version.minor )
            {

            case 0:
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
                break;

            case 1:
                alignment( 2 );
                d = String.valueOf( val ).getBytes( m_wchar_enc );

                if ( d.length != 2 )
                {
                    m_buf.cancel( new org.omg.CORBA.DATA_CONVERSION( "Bad wchar type",
                            IIOPMinorCodes.MARSHAL_WCHAR, CompletionStatus.COMPLETED_MAYBE ) );
                }
                m_buf.append( d, 0, 2 );

                m_index += 2;

                break;

            case 2:
                alignment( 1 );

                d = String.valueOf( val ).getBytes( m_wchar_enc );

                m_buf.alloc( m_tmp_buf, m_tmp_off, d.length + 1 );

                m_index += d.length + 1;

                m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) d.length;

                System.arraycopy( d, 0, m_tmp_buf.value, m_tmp_off.value + 1, d.length );

                break;
            }
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }

    /**
     * Add an IDL short into the stream
     */
    public void write_short( short val )
    {
        alignment( 2 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, 2 );
        m_index += 2;

        m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) ( val >>> 8 );
        m_tmp_buf.value[ m_tmp_off.value + 1 ] = ( byte ) val;
    }

    /**
     * Add an IDL unsigned short into the stream
     */
    public void write_ushort( short val )
    {
        write_short( val );
    }

    /**
     * Add an IDL long into the stream
     */
    public void write_long( int val )
    {
        alignment( 4 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, 4 );
        m_index += 4;

        m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) ( val >>> 24 );
        m_tmp_buf.value[ m_tmp_off.value + 1 ] = ( byte ) ( val >>> 16 );
        m_tmp_buf.value[ m_tmp_off.value + 2 ] = ( byte ) ( val >>> 8 );
        m_tmp_buf.value[ m_tmp_off.value + 3 ] = ( byte ) val;
    }

    /**
     * Add a IDL unsigned long into the stream
     */
    public void write_ulong( int val )
    {
        write_long( val );
    }

    /**
     * Add an IDL long long into the stream
     */
    public void write_longlong( long val )
    {
        alignment( 8 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, 8 );
        m_index += 8;

        m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) ( val >>> 56 );
        m_tmp_buf.value[ m_tmp_off.value + 1 ] = ( byte ) ( val >>> 48 );
        m_tmp_buf.value[ m_tmp_off.value + 2 ] = ( byte ) ( val >>> 40 );
        m_tmp_buf.value[ m_tmp_off.value + 3 ] = ( byte ) ( val >>> 32 );
        m_tmp_buf.value[ m_tmp_off.value + 4 ] = ( byte ) ( val >>> 24 );
        m_tmp_buf.value[ m_tmp_off.value + 5 ] = ( byte ) ( val >>> 16 );
        m_tmp_buf.value[ m_tmp_off.value + 6 ] = ( byte ) ( val >>> 8 );
        m_tmp_buf.value[ m_tmp_off.value + 7 ] = ( byte ) val;
    }

    /**
     * Add an IDL unsigned long long into the stream
     */
    public void write_ulonglong( long val )
    {
        write_longlong( val );
    }

    /**
     * Add an IDL float into the stream
     */
    public void write_float( float val )
    {
        write_long( Float.floatToIntBits( val ) );
    }

    /**
     * Add an IDL double into the stream
     */
    public void write_double( double val )
    {
        write_longlong( Double.doubleToLongBits( val ) );
    }

    /**
     * Add an IDL string into the stream
     */
    public void write_string( String val )
    {
        if ( val == null )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Cannot marshal null string",
                    IIOPMinorCodes.BAD_PARAM_NULL_STRING, CompletionStatus.COMPLETED_MAYBE ) );
            return;
        }

        if ( val.length() == 0 )
        {
            write_ulong( 1 );
            write_octet( ( byte ) 0 );
            return;
        }

        try
        {
            byte [] buf = ( val + '\0' ).getBytes( m_char_enc );

            write_ulong( buf.length );
            m_buf.append( buf, 0, buf.length );
            m_index += buf.length;
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }

    /**
     * Add an IDL wstring into the stream
     */
    public void write_wstring( String val )
    {
        if ( val == null )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Cannot marshal null wstring",
                    IIOPMinorCodes.BAD_PARAM_NULL_STRING, CompletionStatus.COMPLETED_MAYBE ) );
            return;
        }

        if ( m_wchar_enc == null )
        {
            if ( m_version.minor == 0 )
            {
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }
            else
            {
                m_buf.cancel( new org.omg.CORBA.INV_OBJREF( "Missing wchar encoder.",
                        IIOPMinorCodes.INV_OBJREF_MISSING_ENCODER,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }

            return;
        }

        try
        {
            byte [] buf = null;

            switch ( m_version.minor )
            {

            case 0:
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
                return;

            case 1:
                buf = ( val + '\0' ).getBytes( m_wchar_enc );
                write_ulong( buf.length / 2 );
                break;

            case 2:
                buf = val.getBytes( m_wchar_enc );
                write_ulong( buf.length );
                break;
            }

            m_buf.append( buf, 0, buf.length );
            m_index += buf.length;
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }

    /**
     * Write an array of boolean values.
     */
    public void write_boolean_array( boolean[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                    IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 1 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len );
        m_index += len;

        for ( int i = 0; i < len; ++i )
        {
            m_tmp_buf.value[ m_tmp_off.value + i ] = ( byte ) ( val[ off + i ] ? 1 : 0 );
        }
    }

    /**
     * Write an array of char values.
     */
    public void write_char_array( char[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                    IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 1 );

        try
        {
            byte [] buf = ( new String( val, off, len ) ).getBytes( m_char_enc );

            if ( buf.length != len )
            {
                m_buf.cancel( new org.omg.CORBA.DATA_CONVERSION( "Bad char type",
                        org.omg.CORBA.OMGVMCID.value | 1,
                        org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE ) );
            }
            m_buf.append( buf, 0, len );

            m_index += len;
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }


    /**
     * Add an IDL wide char array
     */
    public void write_wchar_array( char[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( m_wchar_enc == null )
        {
            if ( m_version.minor == 0 )
            {
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }
            else
            {
                m_buf.cancel( new org.omg.CORBA.INV_OBJREF( "Missing wchar encoder.",
                        IIOPMinorCodes.INV_OBJREF_MISSING_ENCODER,
                        CompletionStatus.COMPLETED_MAYBE ) );
            }
            return;
        }

        try
        {
            switch ( m_version.minor )
            {

            case 0:
                m_buf.cancel( new org.omg.CORBA.BAD_OPERATION(
                        "IIOP 1.0 cannot marshal wchar types",
                        IIOPMinorCodes.BAD_OPERATION_IIOP_VERSION,
                        CompletionStatus.COMPLETED_MAYBE ) );
                return;

            case 1:
                if ( len == 0 )
                {
                    // don't add alignment, see bug #902049
                    return;
                }

                alignment( 2 );
                byte [] buf = ( new String( val, off, len ) ).getBytes( m_wchar_enc );

                if ( buf.length != 2 * len )
                {
                    m_buf.cancel( new org.omg.CORBA.DATA_CONVERSION( "Bad whar type",
                            IIOPMinorCodes.MARSHAL_WCHAR, CompletionStatus.COMPLETED_MAYBE ) );
                }
                m_buf.append( buf, 0, buf.length );

                m_index += buf.length;

                break;

            case 2:
                if ( len == 0 )
                {
                    // don't add alignment, see bug #902049
                    return;
                }

                alignment( 1 );

                byte [] d;

                if ( m_wchar_align == 0 )
                {
                    for ( int i = off; i < off + len; ++i )
                    {
                        d = String.valueOf( val[ i ] ).getBytes( m_wchar_enc );
                        m_buf.alloc( m_tmp_buf, m_tmp_off, d.length + 1 );
                        m_index += d.length + 1;

                        if ( m_tmp_buf != null )
                        {
                            m_tmp_buf.value[ m_tmp_off.value ] = ( byte ) d.length;
                            System.arraycopy( d, 0, m_tmp_buf.value,
                                    m_tmp_off.value + 1, d.length );
                        }
                    }
                }
                else
                {
                    m_buf.alloc( m_tmp_buf, m_tmp_off, len + len * m_wchar_align );
                    int pos = m_tmp_off.value;

                    for ( int i = off; i < off + len; ++i )
                    {
                        d = String.valueOf( val[ i ] ).getBytes( m_wchar_enc );

                        if ( m_tmp_buf != null )
                        {
                            m_tmp_buf.value[ pos ] = ( byte ) m_wchar_align;
                            System.arraycopy( d, 0, m_tmp_buf.value, pos + 1, d.length );
                        }

                        pos += m_wchar_align + 1;
                    }

                    m_index += len + len * m_wchar_align;
                }
            }
        }
        catch ( final UnsupportedEncodingException ex )
        {
            getLogger().error( "Unsupported encoding should be impossible.", ex );
        }
    }

    /**
     * Add an IDL octet array into the stream
     */
    public void write_octet_array( byte[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 1 );
        m_buf.append( val, off, len );
        m_index += len;
    }

    /**
     * Add an IDL short array
     */
    public void write_short_array( short[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 2 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len * 2 );
        m_index += len * 2;

        for ( int i = 0; i < len; ++i )
        {
            m_tmp_buf.value[ m_tmp_off.value + i * 2 ] = ( byte ) ( val[ off + i ] >>> 8 );
            m_tmp_buf.value[ m_tmp_off.value + i * 2 + 1 ] = ( byte ) val[ off + i ];
        }
    }

    /**
     * Add an IDL unsigned short array into the stream
     */
    public void write_ushort_array( short[] val, int offset, int length )
    {
        write_short_array( val, offset, length );
    }

    /**
     * Add an IDL long array into the stream
     */
    public void write_long_array( int[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 4 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len * 4 );
        m_index += len * 4;

        for ( int i = 0; i < len; ++i )
        {
            m_tmp_buf.value[ m_tmp_off.value + i * 4 ] = ( byte ) ( val[ off + i ] >>> 24 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 1 ] = ( byte ) ( val[ off + i ] >>> 16 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 2 ] = ( byte ) ( val[ off + i ] >>> 8 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 3 ] = ( byte ) val[ off + i ];
        }
    }

    /**
     * Add an IDL unsigned long array into the stream
     */
    public void write_ulong_array( int[] val, int off, int len )
    {
        write_long_array( val, off, len );
    }

    /**
     * Add an IDL long long array into the stream
     */
    public void write_longlong_array( long[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 8 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len * 8 );
        m_index += len * 8;

        for ( int i = 0; i < len; ++i )
        {
            final int tmpBugBaseIdx = m_tmp_off.value + i * 8;
            final int valIdx = off + i;
            m_tmp_buf.value[ tmpBugBaseIdx ] = ( byte ) ( val[ valIdx ] >>> 56 );
            m_tmp_buf.value[ tmpBugBaseIdx + 1 ] = ( byte ) ( val[ valIdx ] >>> 48 );
            m_tmp_buf.value[ tmpBugBaseIdx + 2 ] = ( byte ) ( val[ valIdx ] >>> 40 );
            m_tmp_buf.value[ tmpBugBaseIdx + 3 ] = ( byte ) ( val[ valIdx ] >>> 32 );
            m_tmp_buf.value[ tmpBugBaseIdx + 4 ] = ( byte ) ( val[ valIdx ] >>> 24 );
            m_tmp_buf.value[ tmpBugBaseIdx + 5 ] = ( byte ) ( val[ valIdx ] >>> 16 );
            m_tmp_buf.value[ tmpBugBaseIdx + 6 ] = ( byte ) ( val[ valIdx ] >>> 8 );
            m_tmp_buf.value[ tmpBugBaseIdx + 7 ] = ( byte ) val[ valIdx ];
        }
    }

    /**
     * Add an IDL unsigned long long array into the stream
     */
    public void write_ulonglong_array( long[] val, int off, int len )
    {
        write_longlong_array( val, off, len );
    }

    /**
     * Add an IDL float array into the stream
     */
    public void write_float_array( float[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 4 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len * 4 );
        m_index += len * 4;

        int v;

        for ( int i = 0; i < len; ++i )
        {
            v = Float.floatToIntBits( val[ off + i ] );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 ] = ( byte ) ( v >>> 24 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 1 ] = ( byte ) ( v >>> 16 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 2 ] = ( byte ) ( v >>> 8 );
            m_tmp_buf.value[ m_tmp_off.value + i * 4 + 3 ] = ( byte ) v;
        }
    }

    /**
     * Add an IDL double array into the stream
     */
    public void write_double_array( double[] val, int off, int len )
    {
        if ( off + len > val.length || len < 0 || off < 0 )
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Index Out Of Bounds",
                         IIOPMinorCodes.BAD_PARAM_ARRAY_INDEX, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        if ( len == 0 )
        {
            // don't add alignment, see bug #902049
            return;
        }

        alignment( 8 );
        m_buf.alloc( m_tmp_buf, m_tmp_off, len * 8 );
        m_index += len * 8;

        long v;

        for ( int i = 0; i < len; ++i )
        {
            v = Double.doubleToLongBits( val[ off + i ] );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 ] = ( byte ) ( v >>> 56 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 1 ] = ( byte ) ( v >>> 48 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 2 ] = ( byte ) ( v >>> 40 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 3 ] = ( byte ) ( v >>> 32 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 4 ] = ( byte ) ( v >>> 24 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 5 ] = ( byte ) ( v >>> 16 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 6 ] = ( byte ) ( v >>> 8 );
            m_tmp_buf.value[ m_tmp_off.value + i * 8 + 7 ] = ( byte ) v;
        }
    }

    private static final org.omg.IOP.IOR NULL_IOR =
            new org.omg.IOP.IOR( "", new org.omg.IOP.TaggedProfile[ 0 ] );

    /**
     * Add an CORBA Object reference to the stream.
     */
    public void write_Object( org.omg.CORBA.Object val )
    {
        //_factory.marshalObject(val);

        org.omg.IOP.IOR ior;

        if ( val == null )
        {
            ior = NULL_IOR;
        }
        else
        {
            try
            {
                ior = ( ( org.openorb.orb.core.Delegate )
                        ( ( org.omg.CORBA.portable.ObjectImpl ) val )._get_delegate() ).ior();
            }
            catch ( final org.omg.CORBA.SystemException ex )
            {
                m_buf.cancel( ex );
                return;
            }
        }

        org.omg.IOP.IORHelper.write( this, ior );
    }

    /**
     * Add an CORBA TypeCode to the stream.
     */
    public void write_TypeCode( org.omg.CORBA.TypeCode val )
    {
        write_TypeCodeValue( val, new HashMap() );
    }

    private void write_TypeCodeValue( org.omg.CORBA.TypeCode val, Map tc_list )
    {
        int kind = val.kind().value();

        try
        {
            switch ( kind )
            {

            case TCKind._tk_null:

            case TCKind._tk_void:

            case TCKind._tk_short:

            case TCKind._tk_long:

            case TCKind._tk_ushort:

            case TCKind._tk_ulong:

            case TCKind._tk_float:

            case TCKind._tk_double:

            case TCKind._tk_boolean:

            case TCKind._tk_char:

            case TCKind._tk_octet:

            case TCKind._tk_any:

            case TCKind._tk_TypeCode:

            case TCKind._tk_Principal:

            case TCKind._tk_longlong:

            case TCKind._tk_ulonglong:

            case TCKind._tk_longdouble:

            case TCKind._tk_wchar:
                write_long( kind );
                return;

            case TCKind._tk_string:

            case TCKind._tk_wstring:
                write_long( kind );
                write_ulong( val.length() );
                return;

            case TCKind._tk_fixed:
                write_long( kind );
                write_ushort( val.fixed_digits() );
                write_ushort( val.fixed_scale() );
                return;

            case TCKind._tk_array:

            case TCKind._tk_sequence:

            case TCKind._tk_abstract_interface:

            case TCKind._tk_native:

            case TCKind._tk_objref:

            case TCKind._tk_struct:

            case TCKind._tk_except:

            case TCKind._tk_union:

            case TCKind._tk_enum:

            case TCKind._tk_alias:

            case TCKind._tk_value:

            case TCKind._tk_value_box:
                //case TCKind._tk_home:
                //case TCKind._tk_component:
                break;

            default:
                throw new org.omg.CORBA.MARSHAL(
                    "Unknown type kind: " + kind,
                    0,
                    CompletionStatus.COMPLETED_NO );
            }

            Integer pos = ( Integer ) tc_list.get( val );

            if ( pos != null )
            {
                write_long( 0xffffffff );
                write_long( pos.intValue() - m_index );
                return;
            }

            alignment( 4 );
            pos = NumberCache.getInteger( m_index );
            tc_list.put( val, pos );

            write_long( kind );

            begin_encapsulation();

            switch ( kind )
            {

            case TCKind._tk_array:

            case TCKind._tk_sequence:
                write_TypeCodeValue( val.content_type(), tc_list );
                write_ulong( val.length() );
                break;

            case org.omg.CORBA.TCKind._tk_abstract_interface:

            case org.omg.CORBA.TCKind._tk_native:

            case org.omg.CORBA.TCKind._tk_objref:
                //case org.omg.CORBA.TCKind._tk_home:
                //case org.omg.CORBA.TCKind._tk_component:
                write_string( val.id() );
                write_string( val.name() );
                break;

            case org.omg.CORBA.TCKind._tk_struct:

            case org.omg.CORBA.TCKind._tk_except:
                write_string( val.id() );
                write_string( val.name() );
                write_ulong( val.member_count() );

                for ( int i = 0; i < val.member_count(); i++ )
                {
                    write_string( val.member_name( i ) );
                    write_TypeCodeValue( val.member_type( i ), tc_list );
                }

                break;

            case org.omg.CORBA.TCKind._tk_union:
                write_string( val.id() );
                write_string( val.name() );
                write_TypeCodeValue( val.discriminator_type(), tc_list );
                write_long( val.default_index() );
                write_ulong( val.member_count() );

                for ( int i = 0; i < val.member_count(); i++ )
                {
                    if ( i != val.default_index() )
                    {
                        val.member_label( i ).write_value( this );
                    }
                    else
                    {
                        org.omg.CORBA.TypeCode base = TypeCodeBase._base_type(
                                val.discriminator_type() );

                        switch ( base.kind().value() )
                        {

                        case org.omg.CORBA.TCKind._tk_short:
                            write_short( ( short ) 0 );
                            break;

                        case org.omg.CORBA.TCKind._tk_ushort:
                            write_ushort( ( short ) 0 );
                            break;

                        case org.omg.CORBA.TCKind._tk_long:
                            write_long( 0 );
                            break;

                        case org.omg.CORBA.TCKind._tk_ulong:
                            write_ulong( 0 );
                            break;

                        case org.omg.CORBA.TCKind._tk_boolean:
                            write_boolean( false );
                            break;

                        case org.omg.CORBA.TCKind._tk_char:
                            write_char( ( char ) 0 );
                            break;

                        case org.omg.CORBA.TCKind._tk_enum:
                            write_ulong( 0 );
                            break;

                        default:
                            throw new org.omg.CORBA.MARSHAL( "Unknown type kind" );
                        }
                    }

                    write_string( val.member_name( i ) );
                    write_TypeCodeValue( val.member_type( i ), tc_list );
                }

                break;

            case org.omg.CORBA.TCKind._tk_enum:
                write_string( val.id() );
                write_string( val.name() );
                write_ulong( val.member_count() );

                for ( int i = 0; i < val.member_count(); i++ )
                {
                    write_string( val.member_name( i ) );
                }
                break;

            case org.omg.CORBA.TCKind._tk_alias:
                write_string( val.id() );

                write_string( val.name() );

                write_TypeCodeValue( val.content_type(), tc_list );

                break;

            case org.omg.CORBA.TCKind._tk_value:
                write_string( val.id() );

                write_string( val.name() );

                write_short( val.type_modifier() );

                write_TypeCodeValue( val.concrete_base_type(), tc_list );

                write_ulong( val.member_count() );

                for ( int i = 0; i < val.member_count(); i++ )
                {
                    write_string( val.member_name( i ) );
                    write_TypeCodeValue( val.member_type( i ), tc_list );
                    write_short( val.member_visibility( i ) );
                }

                break;

            case org.omg.CORBA.TCKind._tk_value_box:
                write_string( val.id() );
                write_string( val.name() );
                write_TypeCodeValue( val.content_type(), tc_list );
                break;
            }

            end_encapsulation();
        }
        catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            getLogger().error( "BadKind during marshal.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                    "BadKind during marshal (" + ex + ")", 0,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        catch ( final org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            getLogger().error( "Bounds during marshal.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                    "Bounds during marshal (" + ex + ")", 0,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
    }

    /**
     * Add an CORBA any to the stream.
     */
    public void write_any( org.omg.CORBA.Any val )
    {
        write_TypeCode( val.type() );
        val.write_value( this );
    }

    /**
     * Write principal.
     */
    public void write_Principal( org.omg.CORBA.Principal val )
    {
        byte[] b = val.name();

        write_ulong( b.length );
        write_octet_array( b, 0, b.length );
    }

    /**
     * Write fixed value.
     *
     * @deprecated Loses scale and precision, see
     *              http://www.omg.org/issues/issue3431.txt
     */
    public void write_fixed( java.math.BigDecimal val )
    {
        write_fixed( val, ( short ) -1, ( short ) -1 );
    }

    /**
     * Write fixed value.
     */
    public void write_fixed( java.math.BigDecimal val, org.omg.CORBA.TypeCode tc )
    {
        try
        {
            write_fixed( val, tc.fixed_digits(), tc.fixed_scale() );
        }
        catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            m_buf.cancel( ExceptionTool.initCause( new org.omg.CORBA.BAD_PARAM(
                    "Typecode is not a fixed typecode",
                    IIOPMinorCodes.BAD_PARAM_FIXED_TYPE,
                    CompletionStatus.COMPLETED_NO ), ex ) );
        }
    }

    /**
     * Write fixed value.
     */
    public void write_fixed( java.math.BigDecimal val, short digits, short scale )
    {
        String v = val.abs().movePointRight( val.scale() ).toString();

        if ( scale >= 0 )
        {
            int scdiff = scale - val.scale();

            if ( scdiff < 0 )
            {
                // truncate
                v = v.substring( 0, v.length() + scdiff );
                scdiff = 0;
            }
            else if ( scdiff > 0 )
            {
                // pad
                StringBuffer plspad = new StringBuffer( v );

                while ( scdiff-- > 0 )
                {
                    plspad.append( '0' );
                }
                v = plspad.toString();
            }
        }

        if ( digits >= 0 && v.length() > digits )
        {
            m_buf.cancel( new org.omg.CORBA.MARSHAL( "value too large for type",
                    IIOPMinorCodes.MARSHAL_FIXED, CompletionStatus.COMPLETED_NO ) );
            return;
        }

        byte [] buf = new byte[ v.length() / 2 + 1 ];

        int i = 0, j = 0;

        if ( ( v.length() % 2 ) == 0 )
        {
            buf[ i++ ] = ( byte ) ( v.charAt( j++ ) - '0' );
        }
        while ( i < buf.length - 1 )
        {
            buf[ i++ ] = ( byte ) ( ( ( v.charAt( j++ ) - '0' )
                    << 4 ) | ( v.charAt( j++ ) - '0' ) );
        }
        buf[ i ] = ( byte ) ( ( ( v.charAt( j ) - '0' ) << 4 )
                            | ( ( val.signum() < 0 ) ? 0xD : 0xC ) );

        write_octet_array( buf, 0, buf.length );
    }

    /**
     * Write operation context list. Note that context use should be avoided.
     */
    public void write_Context( org.omg.CORBA.Context ctx, org.omg.CORBA.ContextList contexts )
    {
        java.util.Vector allCtx = new java.util.Vector();

        for ( int i = 0; i < contexts.count(); i++ )
        {
            try
            {
                org.omg.CORBA.NVList list = ctx.get_values( "", 0, contexts.item( i ) );

                for ( int j = 0; j < list.count(); j++ )
                {
                    allCtx.addElement( list.item( j ).name() );

                    allCtx.addElement ( list.item( j ).value().extract_string() );
                }

            }
            catch ( org.omg.CORBA.Bounds ex )
            {
                // TODO: ???
            }
        }

        if ( allCtx.size() != 0 )
        {
            write_ulong( allCtx.size() );

            for ( int j = 0; j < allCtx.size(); j++ )
            {
                write_string( ( String ) allCtx.elementAt( j ) );
            }
        }
        else
        {
            if ( contexts.count() != 0 )
            {
                write_ulong( 0 );
            }
        }
    }

    // --------------------------------------------------------------------------
    //
    // Value handling
    //
    // --------------------------------------------------------------------------

    private void initCurr( Class clz )
    {
        if ( clz != null && !org.omg.CORBA.portable.ValueBase.class.isAssignableFrom( clz ) )
        {
            try
            {
                m_current_value = RMIObjectStreamClass.lookup( clz );
            }
            catch ( java.io.InvalidClassException ex )
            {
                getLogger().error( "Lookup of the class '" + clz + "' failed!", ex );
                throw new org.omg.CORBA.MARSHAL( "Invalid class " + clz + " (" + ex + ")" );
            }
        }
    }

    /**
     * Write a value to a CDR stream
     */
    public void write_value( java.io.Serializable value )
    {
        if ( value != null )
        {
            initCurr( value.getClass() );
        }
        write_value( value, null, null );
    }

    /**
     * Write a value from a CDR stream
     */
    public void write_value( java.io.Serializable value, String arg_repo_id )
    {
        if ( value != null )
        {
            initCurr( value.getClass() );
        }
        write_value( value, arg_repo_id, null );
    }

    /**
     * Write a value to a CDR stream
     */
    public void write_value( java.io.Serializable value, Class clz )
    {
        if ( value != null )
        {
            initCurr( clz.isArray() ? clz : value.getClass() );
        }
        write_value( value, null, null );
    }

    /**
     * Write a value from a CDR stream
     */
    public void write_value( java.io.Serializable value,
            org.omg.CORBA.portable.BoxedValueHelper boxhelp )
    {
        if ( value != null )
        {
            initCurr( value.getClass() );
        }
        write_value( value, null, boxhelp );
    }

    private void value_begin_block()
    {
        alignment( 4 );
        m_buf.beginBlock( CHUNK_GEN, 4, true, this );
        m_index += 4;
    }

    private void value_end_block()
    {
        if ( !m_pending_value_open )
        {
            m_buf.endBlock();
        }
        m_value_level -= m_pending_value_closes;

        m_pending_value_closes = 0;

        write_long( -( m_value_level + 1 ) );
    }

    private void write_value( java.io.Serializable value, String arg_repo_id,
                              org.omg.CORBA.portable.BoxedValueHelper boxhelp )
    {
        // do any pending chunk closes
        if ( m_pending_value_closes > 0 )
        {
            value_end_block();
            // indirections and null values are contained within the enclosing
            // chunk (if there is one)
            if ( m_in_chunked_value )
            {
                m_pending_value_open = true;
            }
        }

        // Check if null value
        if ( value == null )
        {
            write_long( 0 );
            return;
        }

        if ( !m_value_init )
        {
            m_value_init = true;
            m_value_idx = new HashMap();
            m_url_idx = new HashMap();
            m_typecode_list_idx = new HashMap();
            m_typecode_idx = new HashMap();
        }

        java.io.Serializable replFrom = null;
        java.io.Serializable tmpV = value_extended_replace( value );
        if ( tmpV != value )
        {
            replFrom = value;
            value = tmpV;
        }

        // Check if the value is already marshalled
        {
            Integer offset = ( Integer ) m_value_idx.get( new IdentityKey( value ) );
            if ( offset != null )
            {
                write_long( 0xffffffff );
                write_long( offset.intValue() - m_index );
                return;
            }
        }

        m_pending_value_open = false;
        // Generate value tag
        int tag = 0x7fffff00;
        // find ids and marshall type
        String [] ids = null;
        /* 1 for streamable value, 2 for custom value, 3 for valuebox */
        int marshall_type = 0;

        if ( boxhelp != null )
        {
            marshall_type = VALBOX_MARSHAL;
            ids = new String[ 1 ];
            ids[ 0 ] = boxhelp.get_id();
        }
        else if ( value instanceof org.omg.CORBA.portable.StreamableValue )
        {
            marshall_type = STREAM_MARSHAL;
            ids = ( ( org.omg.CORBA.portable.ValueBase ) value )._truncatable_ids();
        }
        else if ( value instanceof org.omg.CORBA.portable.CustomValue )
        {
            marshall_type = CUSTOM_MARSHAL;
            ids = ( ( org.omg.CORBA.portable.ValueBase ) value )._truncatable_ids();
        }
        else if ( value instanceof org.omg.CORBA.portable.ValueBase )
        {
            // check for valuebox type.
            marshall_type = VALBOX_MARSHAL;
            ids = ( ( org.omg.CORBA.portable.ValueBase ) value )._truncatable_ids();
            String boxname = RepoIDHelper.idToClass( ids[ 0 ], RepoIDHelper.TYPE_HELPER );
            try
            {
                boxhelp = ( org.omg.CORBA.portable.BoxedValueHelper )
                        Thread.currentThread().getContextClassLoader().loadClass(
                        boxname ).newInstance();
            }
            catch ( final Exception ex )
            {
                m_buf.cancel( ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                        "Unable to load boxed value helper",
                        IIOPMinorCodes.MARSHAL_VALUE,
                        CompletionStatus.COMPLETED_MAYBE ), ex ) );
                return;
            }
        }
        else if ( value.getClass().isArray() && arg_repo_id != null
              && arg_repo_id.startsWith( "IDL:" ) )
        {
            marshall_type = VALBOX_MARSHAL;
            String boxname = RepoIDHelper.idToClass( arg_repo_id, RepoIDHelper.TYPE_HELPER );
            try
            {
                boxhelp = ( org.omg.CORBA.portable.BoxedValueHelper )
                        Thread.currentThread().getContextClassLoader().loadClass(
                        boxname ).newInstance();
            }
            catch ( final Exception ex )
            {
                m_buf.cancel( ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                        "Unable to load boxed value helper",
                        IIOPMinorCodes.MARSHAL_VALUE,
                        CompletionStatus.COMPLETED_MAYBE ), ex ) );
            }
        }
        else
        {
            marshall_type = EXTENDED_MARSHAL;
            if ( ( ids = value_extended_get_IDs( value ) ) == null )
            {
                m_buf.cancel( new org.omg.CORBA.BAD_PARAM(
                        "Unable to find IDs for valuetype: " + value,
                        IIOPMinorCodes.BAD_PARAM_VALUE_CLASS, CompletionStatus.COMPLETED_MAYBE ) );
                return;
            }
        }

        // find which id matches the formal argument type (if any)
        int last_id = ids.length;
        if ( arg_repo_id != null )
        {
            if ( ids.length == 1 && ids[ 0 ].equals( arg_repo_id ) )
            {
                last_id = 0;
            }
            else
            {
                for ( last_id = 0; last_id < ids.length; ++last_id )
                {
                    if ( arg_repo_id.equals( ids[ last_id ] ) )
                    {
                        ++last_id;
                        break;
                    }
                }
            }
        }

        if ( last_id == 0 )
        {
            tag = tag | NO_TYPE_INFORMATION;
        }
        else if ( last_id == 1 )
        {
            tag = tag | SINGLE_TYPE_INFORMATION;
        }
        else
        {
            tag = tag | MULTIPLE_TYPE_INFORMATION;
        }
        // find the codebase
        String url = getURLCodeBase( value );
        if ( url != null )
        {
            tag = tag | CODEBASE;
        }
        else
        {
            tag = tag | NO_CODEBASE;
        }
        boolean chunked = ( marshall_type == CUSTOM_MARSHAL )
              /*|| (marshall_type == EXTENDED_MARSHAL) */
              || ( last_id > 1 ) || m_in_chunked_value;
        boolean old_in_chunked_value = m_in_chunked_value;
        if ( chunked )
        {
            m_in_chunked_value = true;
            tag = tag | CHUNK;
        }
        else
        {
            tag = tag | NO_CHUNK;
        }
        // store offset for later use
        alignment( 4 );
        m_value_idx.put( new IdentityKey( value ), NumberCache.getInteger( m_index ) );
        if ( replFrom != null )
        {
            m_value_idx.put( new IdentityKey( replFrom ), NumberCache.getInteger( m_index ) );
        }

        // write the tag.
        write_long( tag );

        // write codebase URL
        if ( url != null )
        {
            Integer offset = ( Integer ) m_url_idx.get( url );
            if ( offset != null )
            {
                write_ulong( 0xffffffff );
                write_long( offset.intValue() - m_index );
            }
            else
            {
                alignment( 4 );
                m_url_idx.put( url, NumberCache.getInteger( m_index ) );
                write_string( url );
            }
        }

        // write repository id / ids
writeids:
        if ( last_id > 0 )
        {
            if ( last_id > 1 )
            {
                // multiple ids case.
                if ( last_id >= ids.length )
                {
                    // only attempt to indirect complete id lists Could do some
                    // more work to find indirections more often but there'll
                    // only be a small saving in marshall size so it's probably
                    // not worth it.
                    Integer offset = ( Integer ) m_typecode_list_idx.get( value.getClass() );
                    if ( offset != null )
                    {
                        write_ulong( 0xffffffff );
                        write_long( offset.intValue() - m_index );
                        break writeids;
                    }
                    alignment( 4 );
                    m_typecode_list_idx.put( value.getClass(), NumberCache.getInteger( m_index ) );
                }
                // write the id count
                write_long( last_id );
            }

            // now write out the ids.
            for ( int i = 0; i < last_id; ++i )
            {
                Integer offset = ( Integer ) m_typecode_idx.get( ids[ i ] );
                if ( offset != null )
                {
                    write_ulong( 0xffffffff );
                    write_long( offset.intValue() - m_index );
                }
                else
                {
                    alignment( 4 );
                    m_typecode_idx.put( ids[ i ], NumberCache.getInteger( m_index ) );
                    write_string( ids[ i ] );
                }
            }
        }

        m_value_level++;

        // chunk header
        if ( chunked )
        {
            m_pending_value_open = true;
        }
        // marshall
        switch ( marshall_type )
        {

        case STREAM_MARSHAL:
            ( ( org.omg.CORBA.portable.StreamableValue ) value )._write( this );
            break;

        case VALBOX_MARSHAL:
            boxhelp.write_value( this, value );
            break;

        case CUSTOM_MARSHAL:
            org.omg.CORBA.DataOutputStream outputStream =
                    new org.openorb.orb.core.DataOutputStream( this );
            ( ( org.omg.CORBA.portable.CustomValue ) value ).marshal( outputStream );
            break;

        case EXTENDED_MARSHAL:
            try
            {
                value_extended_marshal( value );
            }
            catch ( final org.omg.CORBA.SystemException ex )
            {
                m_buf.cancel( ex );
                return;
            }
            catch ( final Throwable ex )
            {
                getLogger().error( "Exception during extended marshal.", ex );
                m_buf.cancel( ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                        "Exception during extended marshal (" + ex + ")",
                        IIOPMinorCodes.MARSHAL_VALUE,
                        CompletionStatus.COMPLETED_MAYBE ), ex ) );
                return;
            }
            break;
        }

        if ( chunked )
        {
            m_pending_value_closes++;
            m_in_chunked_value = old_in_chunked_value;
        }
        else
        {
            // there may be pending closes, if there are then
            // they will end with a -1 close tag (rather than -2 which is also valid)
            m_value_level--;
        }
    }

    /**
     * Can be overloaded to allow write replacement of target.
     */
    protected java.io.Serializable value_extended_replace( java.io.Serializable value )
    {
        if ( value == null )
        {
            return null;
        }

        // if we don't have a current value yet, set it now
        if ( m_current_value == null )
        {
            initCurr( value.getClass() );
        }

        java.io.Serializable newValue;
        if ( m_current_value != null && m_current_value.forClass().isInstance( value )
              && ( s_handler instanceof ValueHandlerImpl ) )
        {
            newValue = ( ( ValueHandlerImpl ) s_handler ).writeReplaceExt( value, m_current_value );
        }
        else
        {
            newValue = s_handler.writeReplace( value );
        }

        if ( value != newValue )
        {
            if ( newValue != null )
            {
                initCurr( newValue.getClass() );
            }
        }
        return newValue;
    }

    /**
     * This function should be overloaded by base types to allow marshaling
     * of extended value types, RMI over IIOP for example.
     */
    protected String [] value_extended_get_IDs( java.io.Serializable value )
    {
        String repoID = m_current_value.getRepoID();
        return new String[] { repoID };
    }

    /**
     * This fuction should be overloaded by base types. It returns true if the
     * valuetype is custom marshalled and must have a chunked encoding.
     */
    protected boolean value_extended_custom( java.io.Serializable value )
    {
        return true;
    }

    /**
     * This function should be overloaded by base types to allow marshaling
     * of extended value types, RMI over IIOP for example.
     */
    protected void value_extended_marshal( java.io.Serializable value )
    {
        s_handler.writeValue( this, value );
    }

    /**
     * Write an abstract interface
     */
    public void write_abstract_interface( java.lang.Object obj )
    {
        if ( obj != null && obj instanceof org.omg.CORBA.Object )
        {
            write_boolean( true );
            write_Object( ( org.omg.CORBA.Object ) obj );
        }
        else if ( obj == null || obj instanceof java.io.Serializable )
        {
            write_boolean( false );
            write_value( ( java.io.Serializable ) obj );
        }
        else
        {
            m_buf.cancel( new org.omg.CORBA.BAD_PARAM( "Attempt to marshal unknown interface type",
                    IIOPMinorCodes.BAD_PARAM_ABSTRACT_CLASS, CompletionStatus.COMPLETED_MAYBE ) );
        }
    }

    // --------------------------------------------------------------------------
    // Implementation specific
    // --------------------------------------------------------------------------

    /**
     * Used by the value handler.
     */
    public RMIObjectStreamClass getObjectStreamClass()
    {
        RMIObjectStreamClass c = m_current_value;
        m_current_value = null;
        return c;
    }

    /**
     * This operation is used to get the URL CodeBase
     */
    private String getURLCodeBase( Object obj )
    {
        if ( m_system_urls == null )
        {
            m_system_urls = ( ( org.openorb.orb.core.ORB ) m_orb ).getURLCodeBase();
        }
        if ( m_system_urls != null && m_system_urls.length() != 0 )
        {
            return m_system_urls;
        }

        // if property not set see if the object is loaded by a URLClassLoader
        ClassLoader loader = obj.getClass().getClassLoader();

        if ( !( loader instanceof URLClassLoader ) )
        {
            return null;
        }
        URL [] urls = ( ( URLClassLoader ) loader ).getURLs();

        StringBuffer buf = new StringBuffer();

        for ( int i = 0; i < urls.length; ++i )
        {
            if ( !urls[ i ].getProtocol().equals( "file" ) )
            {
                buf.append( urls[ i ].toString() ).append( ' ' );
            }
        }
        if ( buf.length() > 0 )
        {
            return buf.toString();
        }
        return null;
    }
}

