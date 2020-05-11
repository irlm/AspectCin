/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.ORB;

import org.apache.avalon.framework.logger.Logger;

/**
 * This helper class can be passed to streams to extract and insert
 * value box types as anys.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:50 $
 */
public class TypeCodeValueBoxHelper
    implements org.omg.CORBA.portable.BoxedValueHelper
{
    private TypeCode m_tc;
    private ORB      m_orb;
    private Logger   m_logger = null;

    /**
     * @throws org.omg.CORBA.TypeCodePackage.BadKind The specified typecode is
     * not a valuebox type.
     */
    public TypeCodeValueBoxHelper( ORB orb, TypeCode tc )
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        m_orb = orb;
        m_tc = tc;
        if ( m_tc.kind() != TCKind.tk_value_box )
        {
            throw new org.omg.CORBA.TypeCodePackage.BadKind();
        }
    }

    public TypeCode getTypeCode()
    {
        return m_tc;
    }

    public java.io.Serializable read_value( org.omg.CORBA.portable.InputStream is )
    {
        Any any = m_orb.create_any();
        any.type( m_tc );
        ListOutputStream los = ( ListOutputStream ) any.create_output_stream();
        los.write_value_box( this );
        try
        {
            StreamHelper.copy_stream( m_tc.content_type(),
                    ( org.omg.CORBA_2_3.portable.InputStream ) is, los );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "BadKind exception should be impossible here.", ex );
            }
        }
        return any;
    }

    public void write_value( org.omg.CORBA.portable.OutputStream output,
                             java.io.Serializable obj )
    {
        org.omg.CORBA.Any any = ( org.omg.CORBA.Any ) obj;
        ListInputStream lis = ( ListInputStream ) any.create_input_stream();
        lis.read_value_box();
        try
        {
            StreamHelper.copy_stream( m_tc.content_type(), lis,
                    ( org.omg.CORBA_2_3.portable.OutputStream ) output );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "BadKind exception should be impossible here.", ex );
            }
        }
    }

    public java.lang.String get_id()
    {
        try
        {
            return m_tc.id();
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "BadKind exception should be impossible here.", ex );
            }
            return null;
        }
    }

    /**
     * Return current Logger
     */
    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

