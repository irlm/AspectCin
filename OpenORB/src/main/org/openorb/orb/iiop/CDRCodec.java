/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import org.openorb.orb.io.MarshalBuffer;
import org.openorb.orb.io.StorageBuffer;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

import org.openorb.util.ExceptionTool;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/08/12 12:54:23 $
 */
class CDRCodec
    extends org.openorb.orb.core.LoggableLocalObject
    implements org.omg.IOP.Codec
{
    private org.omg.CORBA.ORB m_orb;

    private org.omg.GIOP.Version m_version;

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

    /**
     * Creates new CDRCodec
     */
    public CDRCodec( org.omg.CORBA.ORB orb, org.omg.IOP.Encoding enc )
    {
        m_orb = orb;
        m_version = new org.omg.GIOP.Version( enc.major_version, enc.minor_version );
    }

    public byte[] encode( org.omg.CORBA.Any data )
        throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding
    {
        MarshalBuffer marshal = new MarshalBuffer();
        CDROutputStream os;

        try
        {
            Object [] args = new Object[ 3 ];
            Class [] cargs = new Class[ 3 ];
            args[ 0 ] = m_orb;
            cargs[ 0 ] = org.omg.CORBA.ORB.class;
            args[ 1 ] = m_version;
            args[ 2 ] = marshal;
            os = ( CDROutputStream ) ( ( org.openorb.orb.core.ORB )
                    m_orb ).getLoader().constructClass( "iiop.CDROutputStreamClass",
                    "org.openorb.orb.iiop.CDROutputStream", args, cargs );
            if ( LogEnabled.class.isAssignableFrom( os.getClass() ) )
            {
                ( ( LogEnabled ) os ).enableLogging( getLogger().getChildLogger( "os" ) );
            }
        }
        catch ( final Exception ex )
        {
            getLogger().error( "Unable to create CDROutputStream class.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Unable to create CDROutputStream class (" + ex + ")" ), ex );
        }

        try
        {
            os.write_boolean( false );
            os.write_TypeCode( data.type() );
            data.write_value( os );
        }
        catch ( final org.omg.CORBA.MARSHAL ex )
        {
            getLogger().error( "Invalid type during encoding the any.", ex );

            throw ( org.omg.IOP.CodecPackage.InvalidTypeForEncoding )
                    ExceptionTool.initCause(
                    new org.omg.IOP.CodecPackage.InvalidTypeForEncoding(), ex );
        }

        StorageBuffer buffer = marshal.lastFragment();

        return buffer.linearize();
    }

    public org.omg.CORBA.Any decode( byte[] data )
        throws org.omg.IOP.CodecPackage.FormatMismatch
    {
        StorageBuffer buffer = new StorageBuffer( data, 0, data.length );
        CDRInputStream is;

        try
        {
            Object [] args = new Object[ 4 ];
            Class [] cargs = new Class[ 4 ];
            args[ 0 ] = m_orb;
            cargs[ 0 ] = org.omg.CORBA.ORB.class;
            args[ 1 ] = Boolean.TRUE;
            cargs[ 1 ] = boolean.class;
            args[ 2 ] = m_version;
            args[ 3 ] = buffer;
            is = ( CDRInputStream ) ( ( org.openorb.orb.core.ORB )
                    m_orb ).getLoader().constructClass( "iiop.CDRInputStreamClass",
                    "org.openorb.orb.iiop.CDRInputStream", args, cargs );
            if ( LogEnabled.class.isAssignableFrom( is.getClass() ) )
            {
                ( ( LogEnabled ) is ).enableLogging( getLogger().getChildLogger( "is" ) );
            }
        }
        catch ( final Exception ex )
        {
            getLogger().error( "Unable to create CDRInputStream class.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Unable to create CDRInputStream class (" + ex + ")" ), ex );
        }

        org.omg.CORBA.Any ret = m_orb.create_any();

        try
        {
            is.bigEndian( !is.read_boolean() );
            ret.read_value( is, is.read_TypeCode() );
        }
        catch ( final org.omg.CORBA.MARSHAL ex )
        {
            getLogger().error( "Invalid format during decoding of an any.", ex );

            throw ( org.omg.IOP.CodecPackage.FormatMismatch )
                    ExceptionTool.initCause(
                    new org.omg.IOP.CodecPackage.FormatMismatch(), ex );
        }

        return ret;
    }

    public byte[] encode_value( org.omg.CORBA.Any data )
        throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding
    {
        MarshalBuffer marshal = new MarshalBuffer();
        marshal.enableLogging( getLogger().getChildLogger( "mb" ) );
        CDROutputStream os;

        try
        {
            Object [] args = new Object[ 3 ];
            Class [] cargs = new Class[ 3 ];
            args[ 0 ] = m_orb;
            cargs[ 0 ] = org.omg.CORBA.ORB.class;
            args[ 1 ] = m_version;
            args[ 2 ] = marshal;
            os = ( CDROutputStream ) ( ( org.openorb.orb.core.ORB )
                    m_orb ).getLoader().constructClass( "iiop.CDROutputStreamClass",
                    "org.openorb.orb.iiop.CDROutputStream", args, cargs );
            if ( LogEnabled.class.isAssignableFrom( os.getClass() ) )
            {
                ( ( LogEnabled ) os ).enableLogging( getLogger().getChildLogger( "os" ) );
            }
        }
        catch ( final Exception ex )
        {
            getLogger().error( "Unable to create CDROutputStream class.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Unable to create CDROutputStream class (" + ex + ")" ), ex );
        }

        try
        {
            os.write_boolean( false );
            data.write_value( os );
        }
        catch ( final org.omg.CORBA.MARSHAL ex )
        {
            getLogger().error( "Invalid type during encoding of a value.", ex );

            throw ( org.omg.IOP.CodecPackage.InvalidTypeForEncoding )
                    ExceptionTool.initCause(
                    new org.omg.IOP.CodecPackage.InvalidTypeForEncoding(), ex );
        }

        StorageBuffer buffer = marshal.lastFragment();

        return buffer.linearize();
    }

    public org.omg.CORBA.Any decode_value( byte[] data, org.omg.CORBA.TypeCode tc )
        throws org.omg.IOP.CodecPackage.FormatMismatch, org.omg.IOP.CodecPackage.TypeMismatch
    {
        StorageBuffer buffer = new StorageBuffer( data, 0, data.length );
        CDRInputStream is;

        try
        {
            Object [] args = new Object[ 4 ];
            Class [] cargs = new Class[ 4 ];
            args[ 0 ] = m_orb;
            cargs[ 0 ] = org.omg.CORBA.ORB.class;
            args[ 1 ] = Boolean.TRUE;
            cargs[ 1 ] = boolean.class;
            args[ 2 ] = m_version;
            args[ 3 ] = buffer;
            is = ( CDRInputStream ) ( ( org.openorb.orb.core.ORB )
                    m_orb ).getLoader().constructClass( "iiop.CDRInputStreamClass",
                    "org.openorb.orb.iiop.CDRInputStream", args, cargs );
            if ( LogEnabled.class.isAssignableFrom( is.getClass() ) )
            {
                ( ( LogEnabled ) is ).enableLogging( getLogger().getChildLogger( "is" ) );
            }
        }
        catch ( final Exception ex )
        {
            getLogger().error( "Unable to create CDRInputStream class.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Unable to create CDRInputStream class (" + ex + ")" ), ex );
        }

        org.omg.CORBA.Any ret = m_orb.create_any();

        try
        {
            is.bigEndian( !is.read_boolean() );
            ret.read_value( is, tc );
        }
        catch ( final org.omg.CORBA.MARSHAL ex )
        {
            getLogger().error( "Invalid format during decoding of a value.", ex );

            throw ( org.omg.IOP.CodecPackage.FormatMismatch )
                    ExceptionTool.initCause(
                    new org.omg.IOP.CodecPackage.FormatMismatch(), ex );
        }
        return ret;
    }
}

