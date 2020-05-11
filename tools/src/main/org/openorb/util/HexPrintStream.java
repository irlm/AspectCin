/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This filter stream is used to write binary data out as formatted hex digits
 * in one of four formats.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/11 08:46:37 $
 */
public class HexPrintStream
    extends FilterOutputStream
{
    /**
     * Simple format. Each input byte is transformed directly into it's hex
     * equivalent and written to the output stream.
     */
    public static final int FORMAT_SIMPLE = 0;

    /**
     * This format writes data out line by line, with breaks on word boundaries
     * boundaries. To complete writing the stream must be flushed.
     */
    public static final int FORMAT_HEXONLY = 1;

    /**
     * This format prints out the ASCII data in the left column and the
     * corresponding hex data in the right column, with breaks on word boundaries.
     * To complete writing a partial line the stream must be flushed.
     */
    public static final int FORMAT_MIXED = 2;

    /**
     * This format prints out ASCII chars above the hex codes. To complete
     * writing a partial line the stream must be flushed.
     */
    public static final int FORMAT_MIXED_TWOLINE = 3;

    private byte[] m_store = null;
    private int m_stoff;
    private int m_format;
    private byte[] m_hexbuf = null;

    private static final byte[] LINE_SEPARATOR = System.getProperty( "line.separator" ).getBytes();


    /**
     * Create new HexPrintStream. This uses FORMAT_SIMPLE
     */
    public HexPrintStream( OutputStream out )
    {
        this( out, FORMAT_SIMPLE );
    }

    /**
     * Create new HexPrintStream and select the format.
     */
    public HexPrintStream( OutputStream out, int format )
    {
        super( out );

        setFormat( format );
    }

    /**
     * Write a single byte to the stream.
     */
    public void write( int val )
        throws IOException
    {
        if ( m_store == null )
        {
            m_store = new byte[ 0 ];
            m_store[ 0 ] = ( byte ) val;
            m_stoff = 1;
            flush();
            m_store = null;
        }
        else
        {
            m_store[ ++m_stoff ] = ( byte ) val;

            if ( m_stoff >= m_store.length )
            {
                flush();
            }
        }
    }

    /**
     * Write an array of bytes to the stream.
     */
    public void write( byte[] buf )
        throws IOException
    {
        write( buf, 0, buf.length );
    }

    /**
     * Write an array of bytes to the stream.
     */
    public void write( byte[] buf, int off, int len )
        throws IOException
    {
        if ( off + len > buf.length )
        {
            throw new IOException( "Offset and length arguments are greater than the"
                     + " length of the buffer" );
        }

        if ( m_store == null )
        {
            if ( off == 0 )
            {
                m_store = buf;
            }
            else
            {
                m_store = new byte[ len ];
                System.arraycopy( buf, off, m_store, 0, len );
            }

            m_stoff = len;
            flush();
            m_store = null;
            return;
        }

        if ( len < m_store.length - m_stoff )
        {
            System.arraycopy( buf, off, m_store, m_stoff, len );
            m_stoff += len;
            return;
        }

        if ( m_stoff > 0 )
        {
            System.arraycopy( buf, off, m_store, m_stoff, m_store.length - m_stoff );
            off += m_store.length - m_stoff;
            len -= m_store.length - m_stoff;
            m_stoff = m_store.length;
            flush();
        }

        while ( len >= m_store.length )
        {
            System.arraycopy( buf, off, m_store, 0, m_store.length );
            off += m_store.length;
            len -= m_store.length;
            m_stoff = m_store.length;
            flush();
        }

        if ( len > 0 )
        {
            System.arraycopy( buf, off, m_store, 0, len );
            m_stoff = len;
        }
    }

    /**
     * Flush the stream. For some formats this causes a line break to occour.
     */
    public void flush()
        throws IOException
    {
        if ( m_stoff == 0 )
        {
            return;
        }
        if ( m_hexbuf == null || m_hexbuf.length < m_stoff * 2 )
        {
            m_hexbuf = new byte[ m_stoff * 2 ];
        }
        for ( int i = 0; i < m_stoff; ++i )
        {
            int lo = m_store[ i ] & 0xF;
            int hi = ( m_store[ i ] >>> 4 ) & 0xF;
            m_hexbuf[ i * 2 ] = ( byte ) ( ( hi < 0xA ) ? ( '0' + hi ) : ( 'A' + hi - 0xA ) );
            m_hexbuf[ i * 2 + 1 ] = ( byte ) ( ( lo < 0xA ) ? ( '0' + lo ) : ( 'A' + lo - 0xA ) );
        }

        switch ( m_format )
        {
            case FORMAT_MIXED:

                for ( int i = 0; i < m_stoff; ++i )
                {
                    if ( !isPrintable( ( byte ) ( m_store[ i ] & 0x7F ) ) )
                    {
                        m_store[ i ] = ( byte ) '.';
                    }
                }
                for ( int i = m_stoff; i < m_store.length; ++i )
                {
                    m_store[ i ] = ( byte ) ' ';
                }
                out.write( m_store, 0, 8 );
                out.write( ' ' );
                out.write( m_store, 8, 8 );
                out.write( ' ' );
                out.write( ' ' );

                // fallthrough

            case FORMAT_HEXONLY:
                for ( int i = 0; i < m_stoff - 3; i += 4 )
                {
                    out.write( m_hexbuf, i * 2, 8 );
                    out.write( ' ' );
                }
                if ( m_stoff % 4 != 0 )
                {
                    out.write( m_hexbuf, m_stoff / 4 * 8, m_stoff % 4 * 2 );
                }
                out.write( LINE_SEPARATOR );
                break;

            case FORMAT_MIXED_TWOLINE:
                for ( int i = 0; i < m_stoff; ++i )
                {
                    if ( Character.isISOControl( ( char ) ( m_store[ i ] & 0x7F ) ) )
                    {
                        out.write( '-' );
                        out.write( '-' );
                    }
                    else
                    {
                        out.write( m_store[ i ] );
                        out.write( ' ' );
                    }

                    if ( ( i + 1 ) % 4 == 0 )
                    {
                        out.write( ' ' );
                    }
                }
                out.write( LINE_SEPARATOR );
                for ( int i = 0; i < m_stoff - 3; i += 4 )
                {
                    out.write( m_hexbuf, i * 2, 8 );
                    out.write( ' ' );
                }
                if ( m_stoff % 4 != 0 )
                {
                    out.write( m_hexbuf, m_stoff / 4 * 8, m_stoff % 4 * 2 );
                }
                out.write( LINE_SEPARATOR );
                break;

            case FORMAT_SIMPLE:
            default:
                out.write( m_hexbuf, 0, 2 * m_stoff );
                break;
        }

        m_stoff = 0;
    }

    /**
     * Close the stream. Flush, then call close on the underlying stream.
     */
    public void close()
        throws IOException
    {
        flush();
        out.close();
    }

    public int getFormat()
    {
        return m_format;
    }

    /**
     * Change the output format. This causes a flush before the format is changed.
     */
    public void setFormat( int format )
    {
        try
        {
            flush();
        }
        catch ( IOException ex )
        {
            // ignore, we try to continue...
        }

        m_format = format;

        switch ( format )
        {
            case FORMAT_MIXED:
                m_store = new byte[ 16 ];
                break;

            case FORMAT_MIXED_TWOLINE:
            case FORMAT_HEXONLY:
                m_store = new byte[ 32 ];
                break;

            case FORMAT_SIMPLE:
            default:
                m_store = null;
                break;
        }
    }

    /**
     * Convert an integer value into hexadecimal string representation.
     *
     * @param val Integer value to convert.
     * @return String with the hexadecimal representation of the integer val.
     */
    public static String toHex( int val )
    {
        byte[] bytes = new byte[] {
                 ( byte ) ( val >>> 24 ),
                 ( byte ) ( val >>> 16 ),
                 ( byte ) ( val >>> 8 ),
                 ( byte ) ( val ) };
        char[] m_hexbuf = new char[ 4 * 2 ];

        for ( int i = 0; i < 4; ++i )
        {
            int lo = bytes[ i ] & 0xF;
            int hi = ( bytes[ i ] >>> 4 ) & 0xF;
            m_hexbuf[ i * 2 ] = ( char ) ( ( hi < 0xA ) ? ( '0' + hi ) : ( 'A' + hi - 0xA ) );
            m_hexbuf[ i * 2 + 1 ] = ( char ) ( ( lo < 0xA ) ? ( '0' + lo ) : ( 'A' + lo - 0xA ) );
        }

        return new String( m_hexbuf );
    }

    /**
     * Convert a short value into hexadecimal string representation.
     *
     * @param val Short value to convert.
     * @return String with the hexadecimal representation of the short val.
     */
    public static String toHex( short val )
    {
        byte[] bytes = new byte[] { ( byte ) ( val >>> 8 ), ( byte ) ( val ) };
        char[] m_hexbuf = new char[ 2 * 2 ];

        for ( int i = 0; i < 2; ++i )
        {
            int lo = bytes[ i ] & 0xF;
            int hi = ( bytes[ i ] >>> 4 ) & 0xF;
            m_hexbuf[ i * 2 ] = ( char ) ( ( hi < 0xA ) ? ( '0' + hi ) : ( 'A' + hi - 0xA ) );
            m_hexbuf[ i * 2 + 1 ] = ( char ) ( ( lo < 0xA ) ? ( '0' + lo ) : ( 'A' + lo - 0xA ) );
        }

        return new String( m_hexbuf );
    }

    /**
     * Convert a byte value into hexadecimal string representation.
     *
     * @param val Byte value to convert.
     * @return String with the hexadecimal representation of the byte val.
     */
    public static String toHex( byte val )
    {
        byte[] bytes = new byte[] { ( byte ) ( val ) };
        char[] m_hexbuf = new char[ 2 ];

        int lo = bytes[ 0 ] & 0xF;
        int hi = ( bytes[ 0 ] >>> 4 ) & 0xF;
        m_hexbuf[ 0 ] = ( char ) ( ( hi < 0xA ) ? ( '0' + hi ) : ( 'A' + hi - 0xA ) );
        m_hexbuf[ 1 ] = ( char ) ( ( lo < 0xA ) ? ( '0' + lo ) : ( 'A' + lo - 0xA ) );

        return new String( m_hexbuf );
    }

    /**
     * Convert a byte array into a printable representation.
     *
     * @param arr The array to convert.
     * @return The stringified representation of the array.
     */
    public static String toHex( byte[] arr )
    {
        String result = null;
        if ( arr != null && arr.length > 0 )
        {
            StringBuffer buf = new StringBuffer( arr.length + 1 );
            for ( int i = 0; i < arr.length; i++ )
            {
                if ( !Character.isISOControl( ( char ) arr[ i ] ) )
                {
                    buf.append( ( char ) arr[ i ] );
                }
                else
                {
                    buf.append( "." );
                }
            }
            result = buf.toString();
        }
        return result;
    }

    /**
     * This function was defined in the Tracing class, i've moved it to here. The isISOControl
     * function seems to work fairly well as a printable test anyhow. It seems that  the
     * isISOControl sometimes misses some characters that the shell interprets as control
     * characters. The isPrintable method does a positive check and it is thus more likely to filter
     * out non-printable characters.
     *
     * @param value The byte value to check.
     * @return True when value is printable, false otherwise.
     */
    public static boolean isPrintable( byte value )
    {
        switch ( Character.getType( ( char ) value ) )
        {
            case Character.COMBINING_SPACING_MARK:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.CURRENCY_SYMBOL:
            case Character.DASH_PUNCTUATION:
            case Character.DECIMAL_DIGIT_NUMBER:
            case Character.ENCLOSING_MARK:
            case Character.END_PUNCTUATION:
            case Character.FORMAT:
            case Character.LETTER_NUMBER:
            case Character.LINE_SEPARATOR:
            case Character.LOWERCASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.MODIFIER_SYMBOL:
            case Character.NON_SPACING_MARK:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.SPACE_SEPARATOR:
            case Character.START_PUNCTUATION:
            case Character.SURROGATE:
            case Character.TITLECASE_LETTER:
            case Character.UPPERCASE_LETTER:
                return true;

            case Character.CONTROL:
            case Character.OTHER_LETTER:
            case Character.OTHER_NUMBER:
            case Character.OTHER_PUNCTUATION:
            case Character.OTHER_SYMBOL:
            case Character.UNASSIGNED:
            case Character.PRIVATE_USE:
            default:
                return false;
        }
    }

    public static void main( String[] args )
    {
        try
        {
            int len = Integer.parseInt( args[ 0 ] );
            byte[] buf = new byte[ len ];
            for ( int i = 0; i < buf.length; ++i )
            {
                buf[ i ] = ( byte ) i;
            }
            HexPrintStream ps = new HexPrintStream( System.out );
            for ( int i = 0; i < 4; ++i )
            {
                System.out.println( "Format: " + i );
                ps.setFormat( i );
                ps.write( buf );
                ps.flush();
                System.out.println();
            }
        }
        catch ( IOException ex )
        {
            System.out.println( ex );
        }
    }
}

