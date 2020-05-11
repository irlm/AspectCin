/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import java.lang.reflect.Array;

import java.math.BigDecimal;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.ORB;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.OMGVMCID;
import org.omg.CORBA.NVList;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.CustomValue;
import org.omg.CORBA.portable.ValueBase;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.openorb.util.CharacterCache;
import org.openorb.util.ExceptionTool;
import org.openorb.util.NumberCache;
import org.openorb.util.RepoIDHelper;

import org.openorb.orb.core.MinorCodes;

import org.openorb.orb.util.Trace;

/**
 * This implementation of org.omg.CORBA_2_3.portable.InputStream uses a list
 * as it's backing store. All non primitive types are stored by reference.<p>
 *
 * The format of the stored types is a TCKind giving the type of the stored
 * value, followed by the stored value itself within an Object wrapper.<p>
 *
 * Arrays of primitive types are stored as a TCKind.tk_array, followed by
 * an ArrayBlock object containing the stored data. The array data is stored
 * by reference.<p>
 *
 * The format of all other types is noted.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/10 21:02:50 $
 */
public class ListOutputStream
    extends OutputStream
    implements ExtendedOutputStream
{
    private final List m_contents;
    private final ORB m_orb;
    private Logger m_logger;

    /**
     * Create new output stream.
     */
    public ListOutputStream( final ORB orb )
    {
        m_orb = orb;
        m_contents = new ArrayList();
    }

    /**
     * Create new output stream with destination list.
     */
    public ListOutputStream( final ORB orb, final List contents )
    {
        m_orb = orb;
        m_contents = contents;
    }


    /**
     * Get the orb associated with the stream.
     */
    public ORB orb()
    {
        return m_orb;
    }

    /**
     * Create a new input stream from the data inserted into this stream.
     * Both streams will share the list data.
     */
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        return new ListInputStream( m_orb, m_contents );
    }

    /**
     * Get the contents of the stream.
    private List getContents()
    {
        return m_contents;
    }
     */

    /**
     * Get the index into the source. This value will be meaningless outside
     * the context of this stream and should only be used to pass back to
     * setIndex on a stream created with create_input_stream.
    private int getIndex()
    {
        return m_contents.size();
    }
     */

    /**
     * Special write called from TypeCodeValueBoxHelper
     * read_value()
     * @see TypeCodeValueBoxHelper
     */
    void write_value_box( final TypeCodeValueBoxHelper box )
    {
        m_contents.add( TCKind.tk_value_box );
        m_contents.add( box );
    }

    //
    // org.omg.CORBA.portable.OutputStream methods
    //

    public void write_boolean( final boolean i )
    {
        m_contents.add( TCKind.tk_boolean );
        m_contents.add( ( i ? Boolean.TRUE : Boolean.FALSE ) );
    }

    public void write_boolean_array( final boolean[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_boolean, getLogger() ) );
    }

    public void write_octet( final byte i )
    {
        m_contents.add( TCKind.tk_octet );
        m_contents.add( NumberCache.getByte( i ) );
    }

    public void write_octet_array( final byte[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_octet, getLogger() ) );
    }

    public void write_short( final short i )
    {
        m_contents.add( TCKind.tk_short );
        m_contents.add( NumberCache.getShort( i ) );
    }

    public void write_short_array( final short[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_short, getLogger() ) );
    }

    public void write_ushort( final short i )
    {
        m_contents.add( TCKind.tk_ushort );
        m_contents.add( NumberCache.getShort( i ) );
    }

    public void write_ushort_array( final short[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_ushort, getLogger() ) );
    }

    public void write_long( final int i )
    {
        // NOTE: The JDK orb incorrectly writes sequence lengths as longs, rather
        // than ulongs. To avoid problems we write all longs as ulongs.
        m_contents.add( TCKind.tk_ulong );
        m_contents.add( NumberCache.getInteger( i ) );
    }

    public void write_long_array( final int[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_long, getLogger() ) );
    }

    public void write_ulong( final int i )
    {
        m_contents.add( TCKind.tk_ulong );
        m_contents.add( NumberCache.getInteger( i ) );
    }

    public void write_ulong_array( final int[] val, final int off, int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_ulong, getLogger() ) );
    }

    public void write_longlong( final long i )
    {
        m_contents.add( TCKind.tk_longlong );
        m_contents.add( NumberCache.getLong( i ) );
    }

    public void write_longlong_array( final long[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_longlong, getLogger() ) );
    }

    public void write_ulonglong( final long i )
    {
        m_contents.add( TCKind.tk_ulonglong );
        m_contents.add( NumberCache.getLong( i ) );
    }

    public void write_ulonglong_array( final long[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_ulonglong, getLogger() ) );
    }

    public void write_float( final float i )
    {
        m_contents.add( TCKind.tk_float );
        m_contents.add( NumberCache.getFloat( i ) );
    }

    public void write_float_array( final float[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_float, getLogger() ) );
    }

    public void write_double( final double i )
    {
        m_contents.add( TCKind.tk_double );
        m_contents.add( NumberCache.getDouble( i ) );
    }

    public void write_double_array( final double[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_double, getLogger() ) );
    }

    public void write_char( final char i )
    {
        m_contents.add( TCKind.tk_char );
        m_contents.add( CharacterCache.getCharacter( i ) );
    }

    public void write_char_array( final char[] val, final int off, final int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_char, getLogger() ) );
    }

    public void write_wchar( final char i )
    {
        m_contents.add( TCKind.tk_wchar );
        m_contents.add( CharacterCache.getCharacter( i ) );
    }

    public void write_wchar_array( final char [] val, int off, int len )
    {
        m_contents.add( TCKind.tk_array );
        m_contents.add( new ArrayBlock( val, off, len, TCKind.tk_wchar, getLogger() ) );
    }

    public void write_string( final String i )
    {
        m_contents.add( TCKind.tk_string );
        m_contents.add( i );
    }

    public void write_wstring( final String i )
    {
        m_contents.add( TCKind.tk_wstring );
        m_contents.add( i );
    }

    public void write_Object( final org.omg.CORBA.Object value )
    {
        m_contents.add( TCKind.tk_objref );
        m_contents.add( value );
    }

    public void write_TypeCode( final org.omg.CORBA.TypeCode value )
    {
        m_contents.add( TCKind.tk_TypeCode );
        m_contents.add( value );
    }

    /**
     * To copy the any's contents to the stream write it's typecode followed with
     * a call to any.write_value.
     */
    public void write_any( final org.omg.CORBA.Any value )
    {
        m_contents.add( TCKind.tk_any );
        m_contents.add( value );
    }

    /**
     * Contexts are written like an array of strings, with name and value pairs
     * for each context.
     */
    public void write_Context( final Context ctx, final ContextList contexts )
    {
        List allCtx = new ArrayList();
        for ( int i = 0; i < contexts.count(); i++ )
        {
            try
            {
                NVList list = ctx.get_values( "", 0, contexts.item( i ) );
                for ( int j = 0; j < list.count(); j++ )
                {
                    allCtx.add( list.item( j ).name() );
                    allCtx.add( list.item( j ).value().extract_string() );
                }
            }
            catch ( final Bounds ex )
            {
                org.openorb.orb.util.Trace.signalIllegalCondition( m_logger,
                      "Access to item #" + i +  " of " + contexts.count() + " items "
                      + "was interrupted by a Boune exception (" + ex + ")." );
            }
        }
        if ( allCtx.size() != 0 )
        {
            write_ulong( allCtx.size() );
            for ( int j = 0; j < allCtx.size(); j++ )
            {
                write_string( ( String ) allCtx.get( j ) );
            }
        }
        else if ( contexts.count() != 0 )
        {
            write_ulong( 0 );
        }
    }

    /**
    * @deprecated Deprecated by CORBA 2.2
    */
    public void write_Principal ( final org.omg.CORBA.Principal value )
    {
        m_contents.add( TCKind.tk_Principal );
        m_contents.add( value );
    }

    public void write_fixed( final java.math.BigDecimal value )
    {
        m_contents.add( TCKind.tk_fixed );
        m_contents.add( value );
    }

    /**
     * @throws BAD_PARAM Typecode is not fixed type or value out of range of type.
     */
    public void write_fixed( final BigDecimal val, final org.omg.CORBA.TypeCode tc )
    {
        try
        {
            write_fixed( val, tc.fixed_digits(), tc.fixed_scale() );
        }
        catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            getLogger().error( "Type mismatch with fixed type.", ex );

            throw ExceptionTool.initCause( new BAD_PARAM( "Type mismatch with fixed type ("
                  + ex + ")", MinorCodes.BAD_PARAM_FIXED_TYPE,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }
    }

    /**
     * @throws BAD_PARAM Value out of range of type.
     */
    public void write_fixed( BigDecimal val, final short digits, final short scale )
    {
        if ( scale >= 0 && val.scale() != scale )
        {
            val = val.setScale( scale, java.math.BigDecimal.ROUND_HALF_EVEN );
        }

        if ( digits >= 0 )
        {
            String strrep = val.toString();
            int strlen = strrep.length();

            if ( strrep.charAt( 0 ) == '-' )
            {
                --strlen;
            }

            if ( strrep.indexOf( '.' ) != -1 )
            {
                --strlen;
            }

            if ( strlen > digits )
            {
                throw new BAD_PARAM( "Type mismatch with fixed type",
                        MinorCodes.BAD_PARAM_FIXED_TYPE, CompletionStatus.COMPLETED_MAYBE );
            }
        }

        m_contents.add( TCKind.tk_fixed );
        m_contents.add( val );
    }

    /**
     * Valuetypes are stored as a TCKind.tk_value kind followed by a reference
     * to the value. Boxed valuetypes are stored as a TCKind.tk_value_box,
     * followed by the box helper, followed by the contents of the valuebox as
     * written by the helper.<p>
     *
     * This function will find the valuebox helper if the valuetype is a valuebox
     * type.
     */
    public void write_value( final Serializable value )
    {
        if ( null == value )
        {
            m_contents.add( TCKind.tk_value );
            m_contents.add( value );
            return;
        }

        if ( value instanceof StreamableValue
                || value instanceof CustomValue )
        {
            m_contents.add( TCKind.tk_value );
            m_contents.add( value );
            return;
        }

        if ( value instanceof ValueBase )
        {
            final String repositoryId =
                    ( ( ValueBase ) value )._truncatable_ids() [ 0 ];

            final BoxedValueHelper boxhelp =
                    locateBoxedValueHelper( repositoryId );

            m_contents.add( TCKind.tk_value_box );
            m_contents.add( boxhelp );
            return;
        }

        if ( value.getClass().isArray() )
        {
            throw new MARSHAL( "Unable to locate valuebox helper",
                    OMGVMCID.value | 1, CompletionStatus.COMPLETED_NO );
        }

        m_contents.add( TCKind.tk_value );
        m_contents.add( value );
    }

    /**
     * Valuetypes are stored as a TCKind.tk_value kind followed by a reference
     * to the value. Boxed valuetypes are stored as a TCKind.tk_value_box,
     * followed by the box helper, followed by the contents of the valuebox as
     * written by the helper.<p>
     *
     * This function will find the valuebox helper if the valuetype is a valuebox
     * type.
     */
    public void write_value( final Serializable value, final String repositoryId )
    {
        if ( value instanceof StreamableValue
                || value instanceof CustomValue )
        {
            m_contents.add( TCKind.tk_value );
            m_contents.add( value );
        }
        else if ( value instanceof ValueBase || value.getClass().isArray() )
        {
            final BoxedValueHelper boxhelp = locateBoxedValueHelper( repositoryId );

            m_contents.add( TCKind.tk_value_box );
            m_contents.add( boxhelp );
            boxhelp.write_value( this, value );
        }
        else
        {
            m_contents.add( TCKind.tk_value );
            m_contents.add( value );
        }
    }

    public void write_value( final Serializable value, final Class clz )
    {
        write_value( value );
    }

    private BoxedValueHelper locateBoxedValueHelper( final String repositoryId )
    {
        final String boxName =
                RepoIDHelper.idToClass( repositoryId, RepoIDHelper.TYPE_HELPER );

        try
        {
            return ( BoxedValueHelper ) loadClass( boxName ).newInstance();
        }
        catch ( final Exception e )
        {
            getLogger().error( "Unable to locate valuebox helper.", e );

            throw ExceptionTool.initCause( new MARSHAL(
                    "Unable to locate valuebox helper (" + e + ")",
                    OMGVMCID.value | 1, CompletionStatus.COMPLETED_NO ), e );
        }
    }

    /**
     * Boxed valuetypes are stored as a TCKind.tk_value_box, followed by the
     * box helper, followed by the contents of the valuebox as written by the
     * helper.<p>
     *
     * This function will find the valuebox helper if the valuetype is a valuebox
     * type.
     */
    public void write_value( final Serializable value, final BoxedValueHelper boxhelp )
    {
        m_contents.add( TCKind.tk_value_box );
        m_contents.add( boxhelp );
        boxhelp.write_value( this, value );
    }

    /**
     * Abstract interfaces are written as a TCKind.tk_abstract_interface kind,
     * followed by a TCKind.tk_objref and the reference for objects or a
     * TCKind.tk_value and the value for valuetypes.
     */
    public void write_abstract_interface( final Object object )
    {
        m_contents.add( TCKind.tk_abstract_interface );

        if ( null == object )
        {
            write_Object( null );
            return;
        }

        if ( object instanceof org.omg.CORBA.Object )
        {
            write_Object( ( org.omg.CORBA.Object ) object );
            return;
        }

        if ( object instanceof Serializable )
        {
            write_value( ( Serializable ) object );
            return;
        }

        throw new BAD_PARAM( "Attempt to marshal unknown interface type",
                MinorCodes.BAD_PARAM_ABSTRACT_CLASS, CompletionStatus.COMPLETED_MAYBE );
    }

    /**
     * Comparisons to ListInputStreams and ListOutputStreams are possible.
     */
    public boolean equals( final Object obj )
    {
        return create_input_stream().equals( obj );
    }

    /**
     * Arrays of primitive types are stored in the list within one of these
     * classes.
     */
    static class ArrayBlock
    {
        private final Logger m_logger;
        private final TCKind m_kind;
        private final int m_length;
        private Object m_array;
        private int m_offset;

        ArrayBlock( final Object array, final int off, final int len,
                final TCKind kind, final Logger logger )
        {
            this.m_logger = logger;
            this.m_array = array;
            this.m_offset = off;
            this.m_length = len;
            this.m_kind = kind;
        }

        /**
         * Length of the data.
         */
        public int getLength()
        {
            return m_length;
        }

        /**
         * Get the kind of the contents.
         */
        public int getOffset()
        {
            return m_offset;
        }

        /**
         * Get the kind of the contents.
         */
        public TCKind getContentKind()
        {
            return m_kind;
        }

        /**
         * Contents of the block. This may result in the contents being copied
         * if the original offset was not 0.
         */
        public Object getContents()
        {
            if ( m_offset != 0 )
            {
                final Object tmp = Array.newInstance(
                        m_array.getClass().getSuperclass(), m_length );
                System.arraycopy( m_array, m_offset, tmp, 0, m_length );
                m_array = tmp;
                m_offset = 0;
            }

            return m_array;
        }

        /**
         * Compare two ArrayBlocks.
         */
        public boolean equals( final Object o2 )
        {
            if ( !( o2 instanceof ArrayBlock ) )
            {
                return false;
            }

            ArrayBlock ab2 = ( ArrayBlock ) o2;

            if ( m_length != ab2.m_length || m_kind != ab2.m_kind )
            {
                return false;
            }

            switch ( m_kind.value() )
            {
                case TCKind._tk_boolean:
                    {
                        boolean [] a1 = ( boolean[] ) m_array;
                        boolean [] a2 = ( boolean[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_octet:
                    {
                        byte [] a1 = ( byte[] ) m_array;
                        byte [] a2 = ( byte[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_short:
                case TCKind._tk_ushort:
                    {
                        short [] a1 = ( short[] ) m_array;
                        short [] a2 = ( short[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_long:
                case TCKind._tk_ulong:
                    {
                        int [] a1 = ( int[] ) m_array;
                        int [] a2 = ( int[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_longlong:

                case TCKind._tk_ulonglong:
                    {
                        long [] a1 = ( long[] ) m_array;
                        long [] a2 = ( long[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_float:
                    {
                        float [] a1 = ( float[] ) m_array;
                        float [] a2 = ( float[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_double:
                    {
                        double [] a1 = ( double[] ) m_array;
                        double [] a2 = ( double[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                case TCKind._tk_char:
                case TCKind._tk_wchar:
                    {
                        char [] a1 = ( char[] ) m_array;
                        char [] a2 = ( char[] ) ab2.m_array;

                        for ( int i = 0; i < m_length; ++i )
                        {
                            if ( a1[ m_offset + i ] != a2[ ab2.m_offset + i ] )
                            {
                                return false;
                            }
                        }

                        return true;
                    }
            }

            throw Trace.signalIllegalCondition( m_logger, "Unexpected case kind.value()=="
                  + m_kind.value() + "." );
        }
    }

    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) orb() ).getLogger();
        }
        return m_logger;
    }

    private Class loadClass( final String className )
        throws ClassNotFoundException
    {
        return Thread.currentThread().getContextClassLoader().loadClass( className );
    }
}
