/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

import org.apache.avalon.framework.logger.Logger;
import org.openorb.util.ExceptionTool;

/**
 * This class implements the OMG Class : Context.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:46:22 $
 */
public class Context
    extends org.omg.CORBA.Context
{
    /**
     * Context values
     */
    private final org.omg.CORBA.NVList m_liste;

    /**
     * Context name
     */
    private final String m_name;

    /**
     * Children contexts list
     */
    private final java.util.Vector m_child;

    /**
     * Reference to the parent context
     */
    private final Context m_parent;

    /**
     * Reference to the ORB
     */
    private final org.omg.CORBA.ORB m_orb;

    /**
     * The logger for this instance.
     */
    private final Logger m_logger;

    /**
     * Constructor
     */
    public Context( String name, Context parent, org.omg.CORBA.ORB orb )
    {
        m_name = name;
        m_parent = parent;

        m_liste = orb.create_list( 0 );
        m_child = new java.util.Vector();

        m_orb = orb;

        m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
    }

    /**
     * Return context name
     */
    public String context_name()
    {
        return m_name;
    }

    /**
     * Return parent context
     */
    public org.omg.CORBA.Context parent()
    {
        return m_parent;
    }

    /**
     * Create a new child context
     */
    public org.omg.CORBA.Context create_child( String child_ctx_name )
    {
        org.omg.CORBA.Context child = new Context( child_ctx_name, this, m_orb );
        m_child.addElement( child );
        return child;
    }

    /**
     * Set a context value
     */
    public void set_one_value( String propname, org.omg.CORBA.Any propvalue )
    {
        int index = propertyIndex( propname );
        if ( index == -1 )
        {
            m_liste.add_value( propname, propvalue, 0 );
        }
        else
        {
            try
            {
                ( ( org.openorb.orb.core.dii.NamedValue )
                      m_liste.item( index ) ).setNewValue( propvalue );
            }
            catch ( final org.omg.CORBA.Bounds ex )
            {
                getLogger().error( "index out of bounds for property: " + propname + ".", ex );
                throw ExceptionTool.initCause( new IndexOutOfBoundsException(), ex );
            }
        }
    }

    /**
     * Set a context values set
     */
    public void set_values( org.omg.CORBA.NVList values )
    {
        int index;
        for ( int i = 0; i < values.count(); i++ )
        {
            try
            {
                index = propertyIndex( values.item( i ).name() );

                if ( index == -1 )
                {
                    m_liste.add_value( values.item( i ).name(), values.item( i ).value(), 0 );
                }
                else
                {
                    ( ( org.openorb.orb.core.dii.NamedValue )
                          m_liste.item( index ) ).setNewValue( values.item( i ).value() );
                }
            }
            catch ( org.omg.CORBA.Bounds e )
            {
                // TODO: ???
            }
        }
    }

    /**
     * Delete context values
     */
    public void delete_values( String propname )
    {
        try
        {
            for ( int i = 0; i < m_liste.count(); i++ )
            {
                if ( matching_pattern( m_liste.item( i ).name(), propname ) )
                {
                    m_liste.remove( i );
                    i = 0;
                }
            }
        }
        catch ( org.omg.CORBA.Bounds ex )
        {
            // TODO: ???
        }
    }

    /**
     * Return a set of context values.
     */
    public org.omg.CORBA.NVList get_values( String start_scope, int op_flags, String pattern )
    {
        org.omg.CORBA.NVList nv = null;
        if ( !start_scope.equals( m_name ) )
        {
            if ( m_parent == null )
            {
                nv = m_orb.create_list( 0 );
            }
            else
            {
                nv = m_parent.get_values( start_scope, op_flags, pattern );
            }
        }
        else
        {
            nv = m_orb.create_list( 0 );
        }
        try
        {
            for ( int i = 0; i < m_liste.count(); i++ )
            {
                if ( matching_pattern( m_liste.item( i ).name(), pattern ) )
                {
                    ( ( org.openorb.orb.core.dii.NVList ) nv ).add( m_liste.item( i ) );
                }
            }
        }
        catch ( org.omg.CORBA.Bounds ex )
        {
            // TODO: ???
        }
        for ( int i = 0; i < m_child.size(); i++ )
        {
            nv = ( ( org.openorb.orb.core.dii.Context )
                  ( m_child.elementAt( i ) ) ).add_all_values( nv, pattern );
        }
        return nv;
    }

    /**
     * Return a set of values
     */
    private org.omg.CORBA.NVList add_all_values( org.omg.CORBA.NVList nv , String pattern )
    {
        try
        {
            for ( int i = 0; i < m_liste.count(); i++ )
            {
                if ( matching_pattern( m_liste.item( i ).name(), pattern ) )
                {
                    org.omg.CORBA.NamedValue val = m_liste.item( i );
                    nv.add_value( val.name(), val.value(), val.flags() );
                }
            }
        }
        catch ( org.omg.CORBA.Bounds ex )
        {
            // TODO: ???
        }
        for ( int i = 0; i < m_child.size(); i++ )
        {
            nv = ( ( org.openorb.orb.core.dii.Context )
                  ( m_child.elementAt( i ) ) ).add_all_values( nv, pattern );
        }
        return nv;
    }

    /**
     * Check matching between a word and a pattern
     */
    private boolean matching_pattern( String src, String ptn )
    {
        if ( ptn.equals( "*" ) )
        {
            return true;
        }
        int idx = ptn.indexOf( "*", 0 );

        if ( idx == -1 )
        {
            return src.equals( ptn );
        }
        else
        {
            if ( idx > src.length() )
            {
                return false;
            }
            String s1 = src.substring( 0, idx );
            String s2 = ptn.substring( 0, idx );
            return s1.equals( s2 );
        }
    }

    /**
     * Return a property
     */
    protected int propertyIndex( String name )
    {
        for ( int i = 0; i < m_liste.count(); i++ )
        {
            try
            {
                if ( m_liste.item( i ).name().equals( name ) )
                {
                    return i;
                }
            }
            catch ( final org.omg.CORBA.Bounds ex )
            {
                getLogger().error( "Index out of bounds for property: " + name + ".", ex );

                throw ExceptionTool.initCause( new IndexOutOfBoundsException(), ex );
            }
        }
        return -1;
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

