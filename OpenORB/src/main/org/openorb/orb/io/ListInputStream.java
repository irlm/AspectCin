/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;

import org.openorb.orb.core.DataOutputStream;
import org.openorb.orb.core.MinorCodes;

import org.openorb.util.ExceptionTool;
import org.openorb.util.RepoIDHelper;

/**
 * This implementation of org.omg.CORBA_2_3.portable.InputStream uses a list
 * as it's backing store. All non primitive types are stored by reference.
 *
 * @author Chris Wood
 * @version $Revision: 1.9 $ $Date: 2004/02/10 21:02:49 $
 */

public class ListInputStream
    extends InputStream
    implements ExtendedInputStream
{
    private List m_source;
    private ListIterator m_iter = null;
    private int m_iter_pos = 0;

    private int m_mark_pos = 0;

    private org.omg.CORBA.ORB m_orb;
    private Logger m_logger = null;

    /**
     * Create new list input stream. This is normally called only by the
     * ListOutputStream, since the contents of the list are specialized.
     *
     * @param orb the owning orb.
     * @param source data source.
     */
    public ListInputStream( org.omg.CORBA.ORB orb, List source )
    {
        m_source = source;
        m_orb = orb;
    }

    /**
     * Get the orb associated with the stream.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Get codebase associated with stream. This will return the contents
     * of any TAG_JAVA_CODEBASE service context when unmarshaling, or otherwise
     * return null.
     *
     * Always returns null for list stream.
     */
    public String get_codebase()
    {
        return null;
    }

    /**
     * Get the list source of the data.
    private List getSource()
    {
        return m_source;
    }
     */

    /**
     * Get the sublist containing the remaining data.
    private List getSourceTail()
    {
        return m_source.subList( m_iter_pos, m_source.size() );
    }
     */

    /**
     * Get the index into the source. This value will be meaningless outside
     * the context of this stream and should only be used to pass back to
     * setIndex.
    private int getIndex()
    {
        return m_iter_pos;
    }
     */

    /**
     * Set the index into the source. This is used to reset the position to one
     * visited previously.
    private void setIndex( int index )
    {
        if ( index > m_source.size() || index < 0 )
        {
            throw new IndexOutOfBoundsException();
        }
        m_iter_pos = index;

        m_iter = null;
    }
     */

    public int getSourceSize()
    {
        return m_source.size();
    }

    //
    // java.io.InputStream methods
    //

    /**
     * This operation is not available as the stream does not deal with bytes.
     */
    public int skip()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * This operation is not available as the stream does not deal with bytes.
     */
    public int available()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Returns true.
     */
    public boolean markAvailable()
    {
        return true;
    }

    /**
     * Mark the position for later resetting.
     * @param ign ignored, stream does not deal with bytes.
     */
    public void mark( int ign )
    {
        m_mark_pos = m_iter_pos;
    }

    /**
     * Reset the position to one previously marked.
     */
    public void reset()
    {
        m_iter_pos = m_mark_pos;
        m_iter = null;
    }

    //
    // End of java.io.InputStream methods
    //

    private SystemException reportBufferPositionOfFormatProblem( final Throwable cause )
    {
        getLogger().error( "Buffer position or format problem.", cause );

        return ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                "Buffer position or format problem (" + cause + ")",
                MinorCodes.MARSHAL_BUFFER_POS,
                CompletionStatus.COMPLETED_MAYBE ), cause );
    }

    private SystemException reportOverreadListStream( final Throwable cause )
    {
        getLogger().warn( "Overread on list stream.", cause );

        return ExceptionTool.initCause( new org.omg.CORBA.MARSHAL(
                "Overread on list stream (" + cause + ")",
                MinorCodes.MARSHAL_BUFFER_OVERREAD,
                CompletionStatus.COMPLETED_MAYBE ), cause );
    }

    /**
     * Return the next element from the input stream
     * @return object from input stream
     */
    private java.lang.Object next()
    {
        while ( true )
        {
            try
            {
                if ( m_iter == null )
                {
                    m_iter = m_source.listIterator( m_iter_pos );
                }
                java.lang.Object n = m_iter.next();

                ++m_iter_pos;

                return n;
            }
            catch ( final ConcurrentModificationException ex )
            {
                getLogger().warn( "error in next()", ex );

                m_iter = null;
            }
            catch ( final NoSuchElementException ex )
            {
                throw reportOverreadListStream( ex );
            }
            catch ( final IndexOutOfBoundsException ex )
            {
                throw reportOverreadListStream( ex );
            }
        }
    }

    /**
     * Peeks at the stream and returns the next
     * object from the list.
     */
    private java.lang.Object peek()
    {
        try
        {
            return m_source.get( m_iter_pos );
        }
        catch ( final NoSuchElementException ex )
        {
            throw reportOverreadListStream( ex );
        }
        catch ( final IndexOutOfBoundsException ex )
        {
            throw reportOverreadListStream( ex );
        }
    }


    /**
     * Special read called from TypeCodeValueBoxHelper
     * write_value()
     * @see TypeCodeValueBoxHelper
     */
    TypeCodeValueBoxHelper read_value_box()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_value_box )
            {
                throw new org.omg.CORBA.MARSHAL( "Type mismatch",
                                                 MinorCodes.MARSHAL_TYPE_MISMATCH,
                                                 CompletionStatus.COMPLETED_MAYBE );
            }
            return ( ( TypeCodeValueBoxHelper ) next() );
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }


    /**
    * Read an array.
    * This handles any array written by ListOutputStream write_xx_array.
    * Note the an array may also be written "directly" meaning that
    * it was written element by element. This latter method is not
    * handled in this method.
    */
    private void read_array( Object dest, int off, int len,
                             TCKind typecode )
    {
        try
        {
            // the array is written by ListOutputStream as:
            // 1. TCKKind.tk_array
            // 2. ArrayBlock

            if ( ( TCKind ) next() != TCKind.tk_array )
            {
                throw new org.omg.CORBA.MARSHAL( "Type mismatch",
                                                 MinorCodes.MARSHAL_TYPE_MISMATCH,
                                                 CompletionStatus.COMPLETED_MAYBE );
            }
            ListOutputStream.ArrayBlock src = ( ListOutputStream.ArrayBlock ) next();

            if ( src.getContentKind() != typecode )
            {
                throw new org.omg.CORBA.MARSHAL( "Type mismatch",
                                                 MinorCodes.MARSHAL_TYPE_MISMATCH,
                                                 CompletionStatus.COMPLETED_MAYBE );
            }
            if ( src.getLength() != len )
            {
                throw new org.omg.CORBA.MARSHAL( "Bounds mismatch",
                                                 MinorCodes.MARSHAL_BOUNDS_MISMATCH,
                                                 CompletionStatus.COMPLETED_MAYBE );
            }
            System.arraycopy( src.getContents(), src.getOffset(), dest, off, len );
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
        catch ( final ArrayStoreException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    //
    // org.omg.portable.CORBA.InputStream Methods
    // org.omg.portable.CORBA_2_3.InputStream Methods
    //

    private SystemException reportTypeMismatch()
    {
        return new org.omg.CORBA.MARSHAL( "Type mismatch",
                MinorCodes.MARSHAL_TYPE_MISMATCH,
                CompletionStatus.COMPLETED_MAYBE );

    }

    /**
    * Read a boolean value.
    */
    public boolean read_boolean()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_boolean )
            {
                throw reportTypeMismatch();
            }
            return ( ( Boolean ) next() ).booleanValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    /**
    * Read a boolean array.
    */
    public void read_boolean_array( boolean [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_boolean );
        }
        else
        {
            // array has been written using write_boolean instead of write_boolean_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_boolean();
            }
        }
    }

    public byte read_octet()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_octet )
            {
                throw reportTypeMismatch();
            }
            return ( ( Byte ) next() ).byteValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_octet_array( byte [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_octet );
        }
        else
        {
            // array has been written using write_octet instead of write_octet_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_octet();
            }
        }
    }

    public short read_short()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_short )
            {
                throw reportTypeMismatch();
            }
            return ( ( Short ) next() ).shortValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_short_array( short [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_short );
        }
        else
        {
            // array has been written using write_short instead of write_short_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_short();
            }
        }
    }

    public short read_ushort()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_ushort )
            {
                throw reportTypeMismatch();
            }
            return ( ( Short ) next() ).shortValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_ushort_array( short [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_ushort );
        }
        else
        {
            // array has been written using write_ushort instead of write_ushort_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_ushort();
            }
        }
    }

    public int read_long()
    {
        try
        {
            // NOTE: The JDK orb incorrectly writes sequence lengths as longs, rather
            // than ulongs. To avoid problems we write all longs as ulongs.
            TCKind next = ( TCKind ) next();

            if ( next != TCKind.tk_ulong )
            {
                throw reportTypeMismatch();
            }
            return ( ( Integer ) next() ).intValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_long_array( int [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_long );
        }
        else
        {
            // array has been written using write_long instead of write_long_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_long();
            }
        }
    }

    public int read_ulong()
    {
        try
        {
            TCKind next = ( TCKind ) next();

            if ( next != TCKind.tk_ulong )
            {
                throw reportTypeMismatch();
            }

            return ( ( Integer ) next() ).intValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_ulong_array( int [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_ulong );
        }
        else
        {
            // array has been written using write_ulong instead of write_ulong_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_ulong();
            }
        }
    }

    public long read_longlong()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_longlong )
            {
                throw reportTypeMismatch();
            }
            return ( ( Long ) next() ).longValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_longlong_array( long [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_longlong );
        }
        else
        {
            // array has been written using write_longlong instead of write_longlong_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_longlong();
            }
        }
    }

    public long read_ulonglong()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_ulonglong )
            {
                throw reportTypeMismatch();
            }
            return ( ( Long ) next() ).longValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_ulonglong_array( long [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_ulonglong );
        }
        else
        {
            // array has been written using write_longlong instead of write_longlong_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_ulonglong();
            }
        }
    }

    public char read_char()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_char )
            {
                throw reportTypeMismatch();
            }
            return ( ( Character ) next() ).charValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_char_array( char [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_char );
        }
        else
        {
            // array has been written using write_char instead of write_char_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_char();
            }
        }
    }

    public char read_wchar()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_wchar )
            {
                throw reportTypeMismatch();
            }
            return ( ( Character ) next() ).charValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_wchar_array( char [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_wchar );
        }
        else
        {
            // array has been written using write_wchar instead of write_wchar_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_wchar();
            }
        }
    }

    public float read_float()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_float )
            {
                throw reportTypeMismatch();
            }
            return ( ( Float ) next() ).floatValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_float_array( float [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_float );
        }
        else
        {
            // array has been written using write_float instead of write_float_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_float();
            }
        }
    }

    public double read_double()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_double )
            {
                throw reportTypeMismatch();
            }
            return ( ( Double ) next() ).doubleValue();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public void read_double_array( double [] dest, int off, int len )
    {
        if ( peek() == TCKind.tk_array )
        {
            read_array( dest, off, len, TCKind.tk_double );
        }
        else
        {
            // array has been written using write_double instead of write_double_array
            for ( int ii = 0; ii < len; ii++ )
            {
                dest[off + ii] = read_double();
            }
        }
    }

    public String read_string()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_string )
            {
                throw reportTypeMismatch();
            }
            return ( String ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public String read_wstring()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_wstring )
            {
                throw reportTypeMismatch();
            }
            return ( String ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public org.omg.CORBA.Object read_Object()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_objref )
            {
                throw reportTypeMismatch();
            }
            return ( org.omg.CORBA.Object ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public org.omg.CORBA.Object read_Object( java.lang.Class clz )
    {
        org.omg.CORBA.portable.ObjectImpl src;

        try
        {
            if ( ( TCKind ) next() != TCKind.tk_objref )
            {
                throw reportTypeMismatch();
            }
            java.lang.Object n = next();

            if ( n == null || clz.isInstance( n ) )
            {
                return ( org.omg.CORBA.Object ) n;
            }
            src = ( org.omg.CORBA.portable.ObjectImpl ) n;
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }

        org.omg.CORBA.portable.ObjectImpl ret;

        try
        {
            ret = ( org.omg.CORBA.portable.ObjectImpl ) clz.newInstance();
        }
        catch ( final Exception ex )
        {
            getLogger().error( "Bad object type.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.BAD_PARAM(
                    "Bad object type (" + ex + ")",
                    MinorCodes.BAD_PARAM_OBJ_CLASS,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }

        ret._set_delegate( src._get_delegate() );
        return ret;
    }

    public org.omg.CORBA.TypeCode read_TypeCode()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_TypeCode )
            {
                throw reportTypeMismatch();
            }
            return ( org.omg.CORBA.TypeCode ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public org.omg.CORBA.Any read_any()
    {
        try
        {
            TCKind tc = ( TCKind ) next();

            if ( tc == TCKind.tk_any )
            {
                return ( org.omg.CORBA.Any ) next();
            }
            if ( tc == TCKind.tk_TypeCode )
            {
                org.omg.CORBA.Any any = m_orb.create_any();
                any.read_value( this, ( org.omg.CORBA.TypeCode ) next() );
                return any;
            }

            throw reportTypeMismatch();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public org.omg.CORBA.Context read_Context()
    {
        org.omg.CORBA.NVList nv = m_orb.create_list( 0 );

        int max = ( read_ulong() / 2 );

        for ( int i = 0; i < max; i++ )
        {
            org.omg.CORBA.Any a = m_orb.create_any();
            String name = read_string();
            a.insert_string( read_string() );
            nv.add_value( name, a, 0 );
        }

        org.omg.CORBA.Context context = new org.openorb.orb.core.dii.Context( "", null, m_orb );
        context.set_values( nv );

        return context;
    }

    /**
    * @deprecated Deprecated by CORBA 2.2
    */
    public org.omg.CORBA.Principal read_Principal()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_Principal )
            {
                throw reportTypeMismatch();
            }
            return ( org.omg.CORBA.Principal ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public java.math.BigDecimal read_fixed()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_fixed )
            {
                throw reportTypeMismatch();
            }
            return ( java.math.BigDecimal ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    /**
     * read a fixed.
     */
    public java.math.BigDecimal read_fixed( org.omg.CORBA.TypeCode type )
    {
        try
        {
            return read_fixed( type.fixed_digits(), type.fixed_scale() );
        }
        catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            getLogger().error( "Type mismatch with fixed type.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.BAD_PARAM(
                    "Type mismatch with fixed type (" + ex + ")",
                    MinorCodes.BAD_PARAM_FIXED_TYPE,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }
    }

    /**
     * read a fixed.
     */
    public java.math.BigDecimal read_fixed( short digits, short scale )
    {
        java.math.BigDecimal ret;

        try
        {
            if ( ( TCKind ) next() != TCKind.tk_fixed )
            {
                throw reportTypeMismatch();
            }
            ret = ( java.math.BigDecimal ) next();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }

        {
            String strrep = ret.toString();
            int strlen = strrep.length();

            if ( strrep.charAt( 0 ) == '-' )
            {
                --strlen;
            }
            if ( strrep.indexOf( '.' ) >= 0 )
            {
                --strlen;
            }
            if ( strlen > digits )
            {
                throw new org.omg.CORBA.BAD_PARAM( "Type mismatch with fixed type",
                                                   MinorCodes.BAD_PARAM_FIXED_TYPE,
                                                   CompletionStatus.COMPLETED_MAYBE );
            }
        }

        if ( ret.scale() != scale )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Type mismatch with fixed type",
                                               MinorCodes.BAD_PARAM_FIXED_TYPE,
                                               CompletionStatus.COMPLETED_MAYBE );
        }
        return ret;
    }

    public java.io.Serializable read_value()
    {
        try
        {
            TCKind type = ( TCKind ) next();

            if ( type == TCKind.tk_value )
            {
                return ( java.io.Serializable ) next();
            }
            if ( type == TCKind.tk_value_box )
            {
                org.omg.CORBA.portable.BoxedValueHelper boxhelp
                = ( org.omg.CORBA.portable.BoxedValueHelper ) next();
                return boxhelp.read_value( this );
            }
            throw reportTypeMismatch();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    private SystemException reportBadObjectType()
    {
        return new org.omg.CORBA.BAD_PARAM( "Bad object type",
                MinorCodes.BAD_PARAM_VALUE_CLASS,
                CompletionStatus.COMPLETED_MAYBE );
    }

    public java.io.Serializable read_value( java.lang.String rep_id )
    {
        try
        {
            TCKind type = ( TCKind ) next();
            if ( type == TCKind.tk_value )
            {
                java.io.Serializable val = ( java.io.Serializable ) next();
                if ( val != null )
                {
                    Class clz = null;
                    String clzname = RepoIDHelper.idToClass( rep_id );
                    try
                    {
                        clz = Thread.currentThread().getContextClassLoader().loadClass( clzname );
                    }
                    catch ( Exception ex )
                    {
                        org.openorb.orb.util.Trace.signalIllegalCondition( m_logger,
                              "Loading class '" + clzname + "' (RepoID=" + rep_id + ") failed! ("
                              + ex + ")" );
                    }
                    if ( clz == null || ( clz != null && !clz.isInstance( val ) ) )
                    {
                        throw reportBadObjectType();
                    }
                }
                return val;
            }

            if ( type == TCKind.tk_value_box )
            {
                org.omg.CORBA.portable.BoxedValueHelper boxhelp
                      = ( org.omg.CORBA.portable.BoxedValueHelper ) next();

                if ( !boxhelp.get_id().equals( rep_id ) )
                {
                    throw reportBadObjectType();
                }
                return boxhelp.read_value( this );
            }

            throw reportTypeMismatch();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public java.io.Serializable read_value( java.lang.Class clz )
    {
        java.io.Serializable val;

        try
        {
            TCKind type = ( TCKind ) next();

            if ( type == TCKind.tk_value )
            {
                val = ( java.io.Serializable ) next();
            }
            else if ( type == TCKind.tk_value_box )
            {
                org.omg.CORBA.portable.BoxedValueHelper boxhelp
                = ( org.omg.CORBA.portable.BoxedValueHelper ) next();
                val = boxhelp.read_value( this );
            }
            else
            {
                throw reportTypeMismatch();
            }
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }

        if ( clz != null && val != null && !clz.isInstance( val ) )
        {
            throw reportBadObjectType();
        }
        return val;
    }

    public java.io.Serializable read_value( org.omg.CORBA.portable.BoxedValueHelper factory )
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_value_box )
            {
                throw reportTypeMismatch();
            }
            org.omg.CORBA.portable.BoxedValueHelper boxhelp
                  = ( org.omg.CORBA.portable.BoxedValueHelper ) next();

            if ( !boxhelp.get_id().equals( factory.get_id() ) )
            {
                throw new org.omg.CORBA.BAD_PARAM( "Bad factory type",
                                                   MinorCodes.BAD_PARAM_VALUE_CLASS,
                                                   CompletionStatus.COMPLETED_MAYBE );
            }
            return factory.read_value( this );
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    /**
     * This function has a nonstandard implementation, it copies the data from
     * the contained valuetype into the given target. The CDR streams also have
     * a similar function.
     */
    public java.io.Serializable read_value( java.io.Serializable value )
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_value )
            {
                throw reportTypeMismatch();
            }
            if ( value instanceof org.omg.CORBA.portable.CustomValue )
            {
                org.omg.CORBA.portable.CustomValue src =
                        ( org.omg.CORBA.portable.CustomValue ) next();
                org.omg.CORBA.portable.CustomValue dst =
                        ( org.omg.CORBA.portable.CustomValue ) value;

                if ( !src._truncatable_ids() [ 0 ].equals( dst._truncatable_ids() [ 0 ] ) )
                {
                    throw new org.omg.CORBA.BAD_PARAM( "Bad target type",
                                                       MinorCodes.BAD_PARAM_VALUE_CLASS,
                                                       CompletionStatus.COMPLETED_MAYBE );
                }
                OutputStream os = ( OutputStream ) m_orb.create_output_stream();
                src.marshal( new org.openorb.orb.core.DataOutputStream( os ) );
                dst.unmarshal( new org.openorb.orb.core.DataInputStream(
                      os.create_input_stream() ) );
                return dst;
            }
            else if ( value instanceof org.omg.CORBA.portable.StreamableValue )
            {
                org.omg.CORBA.portable.StreamableValue src =
                        ( org.omg.CORBA.portable.StreamableValue ) next();
                org.omg.CORBA.portable.StreamableValue dst =
                        ( org.omg.CORBA.portable.StreamableValue ) value;
                String [] srcids = src._truncatable_ids();
                String dstid = dst._truncatable_ids() [ 0 ];

                for ( int i = 0; i < srcids.length; ++i )
                {
                    if ( srcids[ i ].equals( dstid ) )
                    {
                        srcids = null;
                        break;
                    }
                }
                if ( srcids != null )
                {
                    throw new org.omg.CORBA.BAD_PARAM( "Bad target type",
                                                       MinorCodes.BAD_PARAM_VALUE_CLASS,
                                                       CompletionStatus.COMPLETED_MAYBE );
                }
                OutputStream os = ( OutputStream ) m_orb.create_output_stream();

                src._write( os );

                dst._read( os.create_input_stream() );

                return dst;
            }
            else
            {
                throw reportTypeMismatch();
            }
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public java.lang.Object read_abstract_interface()
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_abstract_interface )
            {
                throw reportTypeMismatch();
            }
            TCKind type = ( TCKind ) peek();
            if ( type == TCKind.tk_objref )
            {
                return read_Object();
            }
            else if ( type == TCKind.tk_value )
            {
                return read_value();
            }
            throw reportTypeMismatch();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    public java.lang.Object read_abstract_interface( Class clz )
    {
        try
        {
            if ( ( TCKind ) next() != TCKind.tk_abstract_interface )
            {
                throw reportTypeMismatch();
            }
            TCKind type = ( TCKind ) peek();
            if ( type == TCKind.tk_objref )
            {
                return read_Object( clz );
            }
            else if ( type == TCKind.tk_value )
            {
                return read_value( clz );
            }
            throw reportTypeMismatch();
        }
        catch ( final ClassCastException ex )
        {
            throw reportBufferPositionOfFormatProblem( ex );
        }
    }

    /**
     * Compare this ListInputStream to another ListInputStream or ListOutputStream.
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof OutputStream )
        {
            return equals( ( ( OutputStream ) obj ).create_input_stream() );
        }

        if ( obj instanceof ListInputStream )
        {
            ListInputStream lis2 = ( ListInputStream ) obj;

            if ( m_source.size() - m_iter_pos != lis2.m_source.size() - lis2.m_iter_pos )
            {
                return false;
            }
            mark( 0 );

            lis2.mark( 0 );

            try
            {
                while ( m_iter_pos < m_source.size() )
                {
                    java.lang.Object o1 = next();
                    java.lang.Object o2 = lis2.next();

                    if ( ( o1 == null ) != ( o2 == null ) )
                    {
                        return false;
                    }
                    if ( o1 == null || o1 == o2 )
                    {
                        continue;
                    }
                    if ( o1 instanceof org.omg.CORBA.Object )
                    {
                        if ( !( o2 instanceof org.omg.CORBA.Object ) )
                        {
                            return false;
                        }
                        if ( ( ( org.omg.CORBA.Object ) o1 )._is_equivalent(
                                ( org.omg.CORBA.Object ) o2 ) )
                        {
                            continue;
                        }
                        return false;
                    }

                    if ( !o1.getClass().isInstance( o2 ) )
                    {
                        return false;
                    }
                    if ( o1 instanceof org.omg.CORBA.portable.BoxedValueHelper )
                    {
                        continue;
                    }
                    if ( o1 instanceof org.omg.CORBA.portable.StreamableValue )
                    {
                        if ( !( o2 instanceof org.omg.CORBA.portable.StreamableValue ) )
                        {
                            return false;
                        }
                        ListOutputStream os1 = new ListOutputStream( m_orb );

                        ListOutputStream os2 = new ListOutputStream( m_orb );

                        ( ( org.omg.CORBA.portable.StreamableValue ) o1 )._write( os1 );

                        ( ( org.omg.CORBA.portable.StreamableValue ) o2 )._write( os2 );

                        if ( os1.create_input_stream().equals( os2.create_input_stream() ) )
                        {
                            continue;
                        }
                        return false;
                    }

                    if ( o1 instanceof org.omg.CORBA.portable.CustomValue )
                    {
                        if ( !( o2 instanceof org.omg.CORBA.portable.CustomValue ) )
                        {
                            return false;
                        }
                        ListOutputStream os1 = new ListOutputStream( m_orb );

                        ListOutputStream os2 = new ListOutputStream( m_orb );

                        ( ( org.omg.CORBA.portable.CustomValue ) o1 ).marshal(
                                new DataOutputStream( os1 ) );

                        ( ( org.omg.CORBA.portable.CustomValue ) o2 ).marshal(
                                new DataOutputStream( os2 ) );

                        if ( os1.create_input_stream().equals( os2.create_input_stream() ) )
                        {
                            continue;
                        }
                        return false;
                    }

                    if ( !o1.equals( o2 ) )
                    {
                        return false;
                    }
                }

                if ( lis2.m_iter.hasNext() )
                {
                    return false;
                }
                return true;
            }
            finally
            {
                reset();
                lis2.reset();
            }
        }

        return false;
    }

    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

