/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;

import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.portable.StreamableValue;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;

import org.openorb.orb.core.typecode.TypeCodeBase;

import org.openorb.orb.io.StreamHelper;
import org.openorb.orb.io.TypeCodeValueBoxHelper;

import org.openorb.util.CharacterCache;
import org.openorb.util.NumberCache;
import org.openorb.util.RepoIDHelper;

import org.apache.avalon.framework.logger.Logger;

/**
 * Implements the Any class. This implementation uses the
 * {@link org.openorb.orb.io.ListInputStream} and {@link org.openorb.orb.io.ListOutputStream}
 * classes for it's data storage. Non-primitive types inserted/extracted into
 * this implementation are generaly stored by reference.
 *
 * @author Chris Wood
 * @version $Revision: 1.12 $ $Date: 2004/02/17 22:13:54 $
 */
public class Any
    extends org.omg.CORBA.Any
{
    private ORB              m_orb        = null;
    private TypeCode         m_type       = null;
    private TypeCode         m_basetype   = null;
    private java.lang.Object m_value      = null;
    private BoxedValueHelper m_boxhelp    = null;
    private Logger           m_logger     = null;
    private Throwable        m_unknown_ex = null;

    Any( org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
        m_basetype = m_orb.get_primitive_tc( TCKind.tk_void );
        m_type = m_basetype;
        m_value = null;
    }

    public Throwable getUnknownException()
    {
        return m_unknown_ex;
    }

    public void setUnknownException( Throwable ue )
    {
        m_unknown_ex = ue;
    }

    public boolean equal( org.omg.CORBA.Any a )
    {
        if ( a == null )
        {
            return false;
        }
        if ( a == this )
        {
            return true;
        }
        Any a2 = ( Any ) a;
        if ( m_value == a2.m_value )
        {
            return true;
        }
        if ( m_value == null || a2.m_value == null )
        {
            return false;
        }
        if ( !m_type.equal( a2.m_type ) )
        {
            return false;
        }
        if ( m_value instanceof OutputStream || m_value instanceof Streamable
                || a2.m_value instanceof OutputStream || a2.m_value instanceof Streamable )
        {
            return create_input_stream().equals( a2.create_input_stream() );
        }
        switch ( m_basetype.kind().value() )
        {
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
        case TCKind._tk_Principal:
        case TCKind._tk_string:
        case TCKind._tk_longlong:
        case TCKind._tk_ulonglong:
        case TCKind._tk_longdouble:
        case TCKind._tk_wchar:
        case TCKind._tk_wstring:
        case TCKind._tk_fixed:
        case TCKind._tk_enum:
            return m_value.equals( a2.m_value );

        case TCKind._tk_TypeCode:
            return ( ( TypeCode ) m_value ).equal( ( TypeCode ) a2.m_value );

        case TCKind._tk_objref:
            return ( ( org.omg.CORBA.Object ) m_value )._is_equivalent(
                  ( org.omg.CORBA.Object ) a2.m_value );

        case TCKind._tk_abstract_interface:
            if ( m_value instanceof org.omg.CORBA.Object
                  != a2.m_value instanceof org.omg.CORBA.Object )
            {
                return false;
            }
            if ( m_value instanceof org.omg.CORBA.Object )
            {
                return ( ( org.omg.CORBA.Object ) m_value )._is_equivalent( ( org.omg.CORBA.Object )
                      a2.m_value );
            }
            // fallthrough

        case TCKind._tk_value:
        case TCKind._tk_value_box:
            // only custom valuetypes would get to here. compare the marshalled values
            return create_input_stream().equals( a2.create_input_stream() );

        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_sequence:
        case TCKind._tk_array:
        case TCKind._tk_except:
        case TCKind._tk_null:
        case TCKind._tk_void:
            // impossible
            org.openorb.orb.util.Trace.signalIllegalCondition( null,
                  "Any.equal() received invalid TCKind value: _tk_void" );

        case TCKind._tk_alias:
        case TCKind._tk_native:
        default:
            // impossible
            org.openorb.orb.util.Trace.signalIllegalCondition( null,
                 "Any.equal() received invalid TCKind value: " + m_basetype.kind().value() );
        }
        return false;
    }

    public org.omg.CORBA.TypeCode type()
    {
        return m_type;
    }

    public void type( org.omg.CORBA.TypeCode t )
    {
        if ( t.kind() == TCKind.tk_native )
        {
            throw new org.omg.CORBA.MARSHAL( "Attempt to insert native type into any",
                  MinorCodes.MARSHAL_NATIVE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( !m_type.equivalent( t ) || m_value instanceof OutputStream
                || ( m_value instanceof Streamable && m_type.kind() != TCKind.tk_alias ) )
        {
            // do not null m_value (m_value=null).
            // (tk_void still needs the empty InputStream)
            m_unknown_ex = null;
        }

        m_boxhelp = null;
        m_type = t;
        m_basetype = TypeCodeBase._base_type( m_type );
    }

    public void read_value( org.omg.CORBA.portable.InputStream is, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.MARSHAL
    {
        type( t );
        if ( m_value instanceof Streamable && !( m_value instanceof StreamableValue ) )
        {
            ( ( Streamable ) m_value )._read( is );
            return;
        }
        switch ( m_basetype.kind().value() )
        {

        case TCKind._tk_null:
        case TCKind._tk_void:
            // do not null m_value (m_value=null).
            // (tk_void still needs the empty InputStream)
            return;

        case TCKind._tk_short:
            m_value = NumberCache.getShort( is.read_short() );
            return;

        case TCKind._tk_long:
            m_value = NumberCache.getInteger( is.read_long() );
            return;

        case TCKind._tk_ushort:
            m_value = NumberCache.getShort( is.read_ushort() );
            return;

        case TCKind._tk_ulong:
            m_value = NumberCache.getInteger( is.read_ulong() );
            return;

        case TCKind._tk_float:
            m_value = NumberCache.getFloat( is.read_float() );
            return;

        case TCKind._tk_double:
            m_value = NumberCache.getDouble( is.read_double() );
            return;

        case TCKind._tk_boolean:
            m_value = is.read_boolean() ? Boolean.TRUE : Boolean.FALSE;
            return;

        case TCKind._tk_char:
            m_value = CharacterCache.getCharacter( is.read_char() );
            return;

        case TCKind._tk_octet:
            m_value = NumberCache.getByte( is.read_octet() );
            return;

        case TCKind._tk_TypeCode:
            m_value = is.read_TypeCode();
            return;

        case TCKind._tk_Principal:
            m_value = is.read_Principal();
            return;

        case TCKind._tk_objref:

            if ( m_value instanceof org.omg.CORBA.Object )
            {
                m_value = is.read_Object( m_value.getClass() );
            }
            else
            {
                m_value = is.read_Object();
            }
            return;

        case TCKind._tk_string:
            m_value = is.read_string();
            try
            {
                int len = m_basetype.length();
                if ( len > 0 && len < ( ( String ) m_value ).length() )
                {
                    throw new org.omg.CORBA.MARSHAL( "String length too long for type",
                          MinorCodes.MARSHAL_SEQ_BOUND, CompletionStatus.COMPLETED_MAYBE );
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "BadKind occured in length() operation.", ex );
                }
            }

            return;

        case TCKind._tk_longlong:
            m_value = NumberCache.getLong( is.read_longlong() );
            return;

        case TCKind._tk_ulonglong:
            m_value = NumberCache.getLong( is.read_ulonglong() );
            return;

        case TCKind._tk_longdouble:
            throw new org.omg.CORBA.NO_IMPLEMENT();

        case TCKind._tk_wchar:
            m_value = CharacterCache.getCharacter( is.read_wchar() );
            return;

        case TCKind._tk_wstring:
            m_value = is.read_wstring();
            try
            {
                int len = m_basetype.length();
                if ( len > 0 && len < ( ( String ) m_value ).length() )
                {
                    throw new org.omg.CORBA.MARSHAL( "String length too long for type",
                          MinorCodes.MARSHAL_SEQ_BOUND, CompletionStatus.COMPLETED_MAYBE );
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "BadKind occured in length() operation.", ex );
                }
            }
            return;

        case TCKind._tk_any:
            if ( m_value == null )
            {
                m_value = m_orb.create_any();
            }
            ( ( Any ) m_value ).read_value( is, is.read_TypeCode() );
            return;

        case TCKind._tk_abstract_interface:
            if ( m_value != null )
            {
                m_value = ( ( InputStream ) is ).read_abstract_interface( m_value.getClass() );
            }
            else
            {
                m_value = ( ( InputStream ) is ).read_abstract_interface();
            }
            return;

        case TCKind._tk_fixed:
            if ( is instanceof org.openorb.orb.io.ExtendedInputStream )
            {
                m_value = ( ( org.openorb.orb.io.ExtendedInputStream )
                       is ).read_fixed( m_basetype );
            }
            else
            {
                try
                {
                    java.math.BigDecimal bi = is.read_fixed();
                    m_value = bi.movePointLeft( m_basetype.fixed_scale() );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
                {
                    if ( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "BadKind occured in fixed_scale() operation.", ex );
                    }
                }
            }
            return;

        case TCKind._tk_value:
            if ( m_value != null )
            {
                m_value = ( ( InputStream ) is ).read_value( m_value.getClass() );
            }
            else
            {
                m_value = ( ( InputStream ) is ).read_value();
            }
            return;

        case TCKind._tk_value_box:
            if ( loadBoxHelper() )
            {
                m_value = ( ( InputStream ) is ).read_value( m_boxhelp );
            }
            else if ( m_boxhelp.get_id().startsWith( "IDL:" ) )
            {
                // unmarshal as a stream.
                Any any = ( Any ) ( ( InputStream ) is ).read_value( m_boxhelp );
                m_value = any.m_value;
            }
            else
            {
                // attempt to unmarshal value using extended unmarshal
                m_value = ( ( InputStream ) is ).read_value( m_boxhelp.get_id() );
            }
            return;

        case TCKind._tk_enum:
            // value will be null
            m_value = NumberCache.getInteger( is.read_ulong() );
            return;

        case TCKind._tk_except:
            m_unknown_ex = null;
            // fallthrough

        case TCKind._tk_struct:

        case TCKind._tk_union:

        case TCKind._tk_sequence:

        case TCKind._tk_array:
            // value will be null
            m_value = new org.openorb.orb.io.ListOutputStream( m_orb );
            StreamHelper.copy_stream( m_type, ( InputStream ) is, ( OutputStream ) m_value );
            return;

        case TCKind._tk_alias:

        case TCKind._tk_native:

        default:
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Invalid TCKind " + m_basetype.kind().value(), new Error() );
            }
            return;
        }
    }

    public void write_value( org.omg.CORBA.portable.OutputStream os )
    {
        if ( m_value instanceof Streamable && !( m_value instanceof StreamableValue ) )
        {
            ( ( Streamable ) m_value )._write( os );
            return;
        }

        if ( m_value instanceof OutputStream )
        {
            InputStream source = ( InputStream ) ( ( OutputStream ) m_value ).create_input_stream();
            StreamHelper.copy_stream( m_type, source, ( OutputStream ) os );
            return;
        }

        switch ( m_basetype.kind().value() )
        {
        case TCKind._tk_null:
        case TCKind._tk_void:
            return;

        case TCKind._tk_short:
            os.write_short( ( ( Short ) m_value ).shortValue() );
            return;

        case TCKind._tk_ushort:
            os.write_ushort( ( ( Short ) m_value ).shortValue() );
            return;

        case TCKind._tk_long:
            os.write_long( ( ( Integer ) m_value ).intValue() );
            return;

        case TCKind._tk_ulong:
            os.write_ulong( ( ( Integer ) m_value ).intValue() );
            return;

        case TCKind._tk_float:
            os.write_float( ( ( Float ) m_value ).floatValue() );
            return;

        case TCKind._tk_double:
            os.write_double( ( ( Double ) m_value ).doubleValue() );
            return;

        case TCKind._tk_boolean:
            os.write_boolean( ( ( Boolean ) m_value ).booleanValue() );
            return;

        case TCKind._tk_char:
            os.write_char( ( ( Character ) m_value ).charValue() );
            return;

        case TCKind._tk_octet:
            os.write_octet( ( ( Byte ) m_value ).byteValue() );
            return;

        case TCKind._tk_TypeCode:
            os.write_TypeCode( ( TypeCode ) m_value );
            return;

        case TCKind._tk_Principal:
            os.write_Principal( ( org.omg.CORBA.Principal ) m_value );
            return;

        case TCKind._tk_objref:
            os.write_Object( ( org.omg.CORBA.Object ) m_value );
            return;

        case TCKind._tk_string:
            os.write_string( ( String ) m_value );
            return;

        case TCKind._tk_longlong:
            os.write_longlong( ( ( Long ) m_value ).longValue() );
            return;

        case TCKind._tk_ulonglong:
            os.write_ulonglong( ( ( Long ) m_value ).longValue() );
            return;

        case TCKind._tk_longdouble:
            throw new org.omg.CORBA.NO_IMPLEMENT();

        case TCKind._tk_wchar:
            os.write_wchar( ( ( Character ) m_value ).charValue() );
            return;

        case TCKind._tk_wstring:
            os.write_wstring( ( String ) m_value );
            return;

        case TCKind._tk_any:
            os.write_TypeCode( ( ( Any ) m_value ).type() );
            ( ( Any ) m_value ).write_value( os );
            return;

        case TCKind._tk_abstract_interface:
            ( ( OutputStream ) os ).write_abstract_interface( m_value );
            return;

        case TCKind._tk_value:
            ( ( OutputStream ) os ).write_value( ( java.io.Serializable ) m_value );
            return;

        case TCKind._tk_value_box:
            // must be a real value to get to here, not a stream
            // if we can't find the helper try writing anyhow.
            if ( loadBoxHelper() )
            {
                ( ( OutputStream ) os ).write_value( ( java.io.Serializable ) m_value, m_boxhelp );
            }
            else
            {
                ( ( OutputStream ) os ).write_value( ( java.io.Serializable ) m_value );
            }
            return;

        case TCKind._tk_enum:
            os.write_ulong( ( ( Integer ) m_value ).intValue() );
            return;

        case TCKind._tk_fixed:
            ( ( org.openorb.orb.io.ExtendedOutputStream ) os ).write_fixed(
                  ( java.math.BigDecimal ) m_value, m_basetype );
            return;

        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_except:
        case TCKind._tk_sequence:
        case TCKind._tk_array:
            // impossible
            org.openorb.orb.util.Trace.signalIllegalCondition( null,
                  "Invalid state, should be streamable or stream" );

        case TCKind._tk_alias:
        case TCKind._tk_native:
        default:
            // impossible
            org.openorb.orb.util.Trace.signalIllegalCondition( null,
                  "Any.equal() received invalid TCKind value: " + m_basetype.kind().value() );
        }
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        //m_value = null;
        m_unknown_ex = null;
        org.omg.CORBA.portable.OutputStream ret = new org.openorb.orb.io.ListOutputStream( m_orb );
        m_value = ret;
        return ret;
    }

    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        if ( m_value instanceof OutputStream )
        {
            return ( ( OutputStream ) m_value ).create_input_stream();
        }
        OutputStream ret = new org.openorb.orb.io.ListOutputStream( m_orb );
        write_value( ret );
        return ret.create_input_stream();
    }

    public short extract_short()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_short )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_short();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getShort( create_input_stream().read_short() );
        }
        return ( ( Short ) m_value ).shortValue();
    }

    public void insert_short( short s )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_short );
        m_basetype = m_type;
        m_value = NumberCache.getShort( s );
    }

    public short extract_ushort()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_ushort )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_ushort();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getShort( create_input_stream().read_ushort() );
        }
        return ( ( Short ) m_value ).shortValue();
    }

    public void insert_ushort( short s )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_ushort );
        m_basetype = m_type;
        m_value = NumberCache.getShort( s );
    }

    public int extract_long()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_long )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_long();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getInteger( create_input_stream().read_long() );
        }
        return ( ( Integer ) m_value ).intValue();
    }

    public void insert_long( int i )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_long );
        m_basetype = m_type;
        m_value = NumberCache.getInteger( i );
    }

    public int extract_ulong()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_ulong
                && m_basetype.kind() != TCKind.tk_enum )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias
              || ( m_basetype.kind() == TCKind.tk_enum && !( m_value instanceof Integer ) ) )
        {
            return create_input_stream().read_ulong();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getInteger( create_input_stream().read_ulong() );
        }
        return ( ( Integer ) m_value ).intValue();
    }

    public void insert_ulong( int i )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_ulong );
        m_basetype = m_type;
        m_value = NumberCache.getInteger( i );
    }

    public long extract_longlong()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_longlong )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_longlong();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getLong( create_input_stream().read_longlong() );
        }
        return ( ( Long ) m_value ).longValue();
    }

    public void insert_longlong( long l )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_longlong );
        m_basetype = m_type;
        m_value = NumberCache.getLong( l );
    }

    public long extract_ulonglong()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_ulonglong )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_ulonglong();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getLong( create_input_stream().read_ulonglong() );
        }
        return ( ( Long ) m_value ).longValue();
    }

    public void insert_ulonglong( long l )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_ulonglong );
        m_basetype = m_type;
        m_value = NumberCache.getLong( l );
    }

    public float extract_float()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_float )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_float();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getFloat( create_input_stream().read_float() );
        }
        return ( ( Float ) m_value ).floatValue();
    }

    public void insert_float( float f )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_float );
        m_basetype = m_type;
        m_value = NumberCache.getFloat( f );
    }

    public double extract_double()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_double )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_double();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getDouble( create_input_stream().read_double() );
        }
        return ( ( Double ) m_value ).doubleValue();
    }

    public void insert_double( double d )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_double );
        m_basetype = m_type;
        m_value = NumberCache.getDouble( d );
    }

    public boolean extract_boolean()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_boolean )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_boolean();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_boolean() ? Boolean.TRUE : Boolean.FALSE;
        }
        return ( ( Boolean ) m_value ).booleanValue();
    }

    public void insert_boolean( boolean b )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_boolean );
        m_basetype = m_type;
        m_value = b ? Boolean.TRUE : Boolean.FALSE;
    }

    public char extract_char()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_char )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_char();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = CharacterCache.getCharacter( create_input_stream().read_char() );
        }
        return ( ( Character ) m_value ).charValue();
    }

    public void insert_char( char c )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_char );
        m_basetype = m_type;
        m_value = CharacterCache.getCharacter( c );
    }

    public char extract_wchar()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_wchar )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_wchar();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = CharacterCache.getCharacter( create_input_stream().read_wchar() );
        }
        return ( ( Character ) m_value ).charValue();
    }

    public void insert_wchar( char c )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_wchar );
        m_basetype = m_type;
        m_value = CharacterCache.getCharacter( c );
    }

    public byte extract_octet()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_octet )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_octet();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = NumberCache.getByte( create_input_stream().read_octet() );
        }
        return ( ( Byte ) m_value ).byteValue();
    }

    public void insert_octet( byte b )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_octet );
        m_basetype = m_type;
        m_value = NumberCache.getByte( b );
    }

    public String extract_string()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_string )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_string();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_string();
        }
        return ( String ) m_value;
    }


    public void insert_string( String s )
        throws org.omg.CORBA.DATA_CONVERSION, org.omg.CORBA.MARSHAL
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_string );
        m_basetype = m_type;
        m_value = s;
    }

    public String extract_wstring()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_wstring )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_wstring();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_wstring();
        }
        return ( String ) m_value;
    }

    public void insert_wstring( String s )
        throws org.omg.CORBA.MARSHAL
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_wstring );
        m_basetype = m_type;
        m_value = s;
    }

    public TypeCode extract_TypeCode()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_TypeCode )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_TypeCode();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_TypeCode();
        }
        return ( TypeCode ) m_value;
    }

    public void insert_TypeCode( TypeCode value )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_TypeCode );
        m_basetype = m_type;
        this.m_value = value;
    }

    /**
     * @deprecated
     */
    public org.omg.CORBA.Principal extract_Principal()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_Principal )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_Principal();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_Principal();
        }
        return ( Principal ) m_value;
    }

    /**
     * @deprecated
     */
    public void insert_Principal( org.omg.CORBA.Principal p )
    {
        if ( m_basetype.kind() != TCKind.tk_Principal )
        {
            m_type = m_orb.get_primitive_tc( TCKind.tk_Principal );
            m_basetype = m_type;
        }
        m_value = p;
    }


    public java.math.BigDecimal extract_fixed()
    {
        if ( m_basetype.kind() != TCKind.tk_fixed )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return ( ( org.openorb.orb.io.ExtendedInputStream )
                  create_input_stream() ).read_fixed( m_basetype );
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = ( ( org.openorb.orb.io.ExtendedInputStream )
                  create_input_stream() ).read_fixed( m_basetype );
        }
        return ( java.math.BigDecimal ) m_value;
    }

    public void insert_fixed( java.math.BigDecimal f, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        if ( TypeCodeBase._base_type( t ).kind() != TCKind.tk_fixed )
        {
            throw new org.omg.CORBA.BAD_PARAM();
        }
        type( t );
        m_value = f;
    }

    public org.omg.CORBA.Any extract_any()
        throws org.omg.CORBA.BAD_OPERATION
    {
        if ( m_basetype.kind() != TCKind.tk_any )
        {
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_type.kind() == TCKind.tk_alias )
        {
            return create_input_stream().read_any();
        }
        if ( m_value instanceof OutputStream )
        {
            m_value = create_input_stream().read_any();
        }
        return ( Any ) m_value;
    }

    public void insert_any( org.omg.CORBA.Any a )
    {
        m_type = m_orb.get_primitive_tc( TCKind.tk_any );
        m_basetype = m_type;
        m_value = a;
    }

    public org.omg.CORBA.Object extract_Object()
        throws org.omg.CORBA.BAD_OPERATION
    {
        switch ( m_basetype.kind().value() )
        {

        case TCKind._tk_objref:

            if ( m_value instanceof Streamable )
            {
                return create_input_stream().read_Object();
            }
            if ( m_value instanceof OutputStream )
            {
                m_value = create_input_stream().read_Object();
            }
            break;

        case TCKind._tk_abstract_interface:
            if ( m_value instanceof OutputStream
                  || ( m_value instanceof Streamable
                  && !( m_value instanceof java.io.Serializable ) ) )
            {
                java.lang.Object contents =
                      ( ( InputStream ) create_input_stream() ).read_abstract_interface();
                if ( !( contents instanceof Object ) )
                {
                    throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                          MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
                }
                if ( m_value instanceof Streamable )
                {
                    return ( org.omg.CORBA.Object ) contents;
                }
                m_value = contents;
            }
            else if ( !( m_value instanceof org.omg.CORBA.Object ) )
            {
                throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                      MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
            }
            break;

        default:
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( org.omg.CORBA.Object ) m_value;
    }

    public void insert_Object( org.omg.CORBA.Object obj )
    {
        insert_Object( obj, m_orb.get_primitive_tc( TCKind.tk_objref ) );
    }

    public void insert_Object( org.omg.CORBA.Object obj, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.BAD_PARAM
    {
        switch ( TypeCodeBase._base_type( t ).kind().value() )
        {

        case TCKind._tk_objref:

        case TCKind._tk_abstract_interface:
            break;

        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }

        type( t );
        m_value = obj;
    }

    public java.io.Serializable extract_Value()
        throws org.omg.CORBA.BAD_OPERATION
    {
        switch ( m_basetype.kind().value() )
        {

        case TCKind._tk_value_box:

            if ( m_value instanceof Streamable && !( m_value instanceof java.io.Serializable ) )
            {
                if ( loadBoxHelper() )
                {
                    throw new org.omg.CORBA.MARSHAL( "Unable to locate valuebox helper",
                          MinorCodes.MARSHAL_VALUEBOX_HELPER, CompletionStatus.COMPLETED_MAYBE );
                }
                return ( ( InputStream ) create_input_stream() ).read_value( m_boxhelp );
            }

            if ( m_value instanceof OutputStream || !( m_value instanceof java.io.Serializable ) )
            {
                if ( loadBoxHelper() )
                {
                    throw new org.omg.CORBA.MARSHAL( "Unable to locate valuebox helper",
                          MinorCodes.MARSHAL_VALUEBOX_HELPER, CompletionStatus.COMPLETED_MAYBE );
                }
                m_value = ( ( InputStream ) create_input_stream() ).read_value( m_boxhelp );
            }

            break;

        case TCKind._tk_value:

            if ( m_value instanceof Streamable && !( m_value instanceof java.io.Serializable ) )
            {
                return ( ( InputStream ) create_input_stream() ).read_value();
            }
            if ( m_value instanceof OutputStream || !( m_value instanceof java.io.Serializable ) )
            {
                m_value = ( ( InputStream ) create_input_stream() ).read_value();
            }
            break;

        case TCKind._tk_abstract_interface:
            if ( m_value instanceof OutputStream
                  ||  ( m_value instanceof Streamable
                  && !( m_value instanceof java.io.Serializable ) ) )
            {
                java.lang.Object contents =
                      ( ( InputStream ) create_input_stream() ).read_abstract_interface();

                if ( !( contents instanceof java.io.Serializable ) )
                {
                    throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                          MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
                }
                if ( m_value instanceof Streamable )
                {
                    return ( java.io.Serializable ) contents;
                }
                m_value = contents;
            }
            else if ( !( m_value instanceof java.io.Serializable ) )
            {
                throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                      MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
            }
            break;

        default:
            throw new org.omg.CORBA.BAD_OPERATION( "Wrong type for extraction",
                  MinorCodes.BAD_OPERATION_ANY_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }

        return ( java.io.Serializable ) m_value;
    }

    public void insert_Value( java.io.Serializable v )
    {
        insert_Value( v, m_orb.get_primitive_tc( TCKind.tk_value ) );
    }

    public void insert_Value( java.io.Serializable v, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.MARSHAL
    {
        switch ( TypeCodeBase._base_type( t ).kind().value() )
        {

        case TCKind._tk_value:

        case TCKind._tk_value_box:

        case TCKind._tk_abstract_interface:
            break;

        default:
            throw new org.omg.CORBA.BAD_PARAM( "Attempt to set typecode to non value type",
                  MinorCodes.BAD_PARAM_VALUE_TYPE, CompletionStatus.COMPLETED_MAYBE );
        }

        type( t );
        m_value = v;
    }


    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        if ( !( m_value instanceof org.omg.CORBA.portable.Streamable ) )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER(
                  "Extract streamable attempted on non streamable contents",
                  MinorCodes.BAD_INV_ORDER_NOT_STREAMABLE, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( org.omg.CORBA.portable.Streamable ) m_value;
    }

    public void insert_Streamable( org.omg.CORBA.portable.Streamable s )
    {
        type( s._type() );
        m_value = s;
    }

    private boolean loadBoxHelper()
    {
        if ( m_boxhelp == null )
        {
            try
            {
                String repo_id = m_basetype.id();

                try
                {
                    String boxname = RepoIDHelper.idToClass( repo_id, RepoIDHelper.TYPE_HELPER );
                    m_boxhelp = ( org.omg.CORBA.portable.BoxedValueHelper )
                          Thread.currentThread().getContextClassLoader().loadClass(
                          boxname ).newInstance();
                    return true;
                }
                catch ( Exception ex )
                {
                    m_boxhelp = new TypeCodeValueBoxHelper( m_orb, m_basetype );
                    return false;
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Exception during loading the BoxedValueHelper class.", ex );
                }
            }
        }

        return !( m_boxhelp instanceof TypeCodeValueBoxHelper );
    }

    /**
     * Return logger for current object.
     * Protected for use in SystemExceptionHelper's static methods
     */
    protected Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

