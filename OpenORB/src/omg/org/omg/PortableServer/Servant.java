/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.PortableServer;

import org.omg.CORBA.ORB;

import java.lang.reflect.Method;

/**
 * Servant class.
 */
public abstract class Servant
{
    private int m_has_get_interface = -1;

    private static final Method INIT_CAUSE_METHOD;

    static
    {
        Method method;
        try
        {
            final Class[] parameterTypes = {Throwable.class};
            method = Throwable.class.getMethod( "initCause", parameterTypes );
        }
        catch ( final NoSuchMethodException e )
        {
            method = null;
        }
        INIT_CAUSE_METHOD = method;
    }

    private static Throwable initCause( final Throwable target, final Throwable cause )
    {
        if ( null == INIT_CAUSE_METHOD )
        {
            return target;
        }
        try
        {
            INIT_CAUSE_METHOD.invoke( target, new Object[] {cause} );
        }
        catch ( final Exception e )
        {
            // ignore as is only best effort
        }

        return target;
    }

    private static RuntimeException initCause( final RuntimeException target,
            final Throwable cause )
    {
        initCause( target, cause );
        return target;
    }

    public final org.omg.CORBA.Object _this_object()
    {
        return _get_delegate().this_object( this );
    }

    public final org.omg.CORBA.Object _this_object( ORB orb )
    {
        try
        {
            ( ( org.omg.CORBA_2_3.ORB ) orb ).set_delegate( this );
        }
        catch ( final ClassCastException ex )
        {
            throw initCause( new org.omg.CORBA.BAD_PARAM(
                  "POA Servant requires an instance of org.omg.CORBA_2_3.ORB ("
                  + ex + ")" ), ex );
        }

        return _this_object();
    }

    public final ORB _orb()
    {
        return _get_delegate().orb( this );
    }

    // No more standard, wait...
    public final void _orb( ORB orb )
    {
        try
        {
            ( ( org.omg.CORBA_2_3.ORB ) orb ).set_delegate( this );
        }
        catch ( final ClassCastException ex )
        {
            throw initCause( new org.omg.CORBA.BAD_PARAM(
                  "POA Servant requires an instance of org.omg.CORBA_2_3.ORB ("
                  + ex + ")" ), ex );
        }
    }

    public final POA _poa()
    {
        return _get_delegate().poa( this );
    }

    public final byte [] _object_id()
    {
        return _get_delegate().object_id( this );
    }

    public POA _default_POA()
    {
        return _get_delegate().default_POA( this );
    }

    public boolean _is_a( String repid )
    {
        return _get_delegate().is_a( this, repid );
    }

    public boolean _non_existent()
    {
        return _get_delegate().non_existent( this );
    }

    /**
     * @deprecated Deprecated by CORBA 2.4
     */
    public org.omg.CORBA.InterfaceDef _get_interface()
    {
        return org.omg.CORBA.InterfaceDefHelper.narrow(
              _get_delegate().get_interface_def( this ) );
    }

    public org.omg.CORBA.Object _get_interface_def()
    {
        // bit of mucking around here to ensure old overrides remain valid.
        if ( m_has_get_interface < 0 )
        {
            Class clz = getClass();
            m_has_get_interface = 0;
            while ( !clz.equals( Servant.class ) )
            {
                try
                {
                    clz.getDeclaredMethod( "_get_interface", null );
                    m_has_get_interface = 1;
                    break;
                }
                catch ( NoSuchMethodException ex )
                {
                    // ignore and try the super class
                }
                catch ( SecurityException ex )
                {
                    // ignore and try the super class
                }
                clz = clz.getSuperclass();
            }
        }

        if ( m_has_get_interface > 0 )
        {
            return _get_interface();
        }
        else
        {
            return _get_delegate().get_interface_def( this );
        }
    }

    // methods for which the skeleton or application programmer must
    // provide for an implementation

    public abstract String [] _all_interfaces( POA poa, byte [] objectId );

    private transient org.omg.PortableServer.portable.Delegate m_delegate = null;

    public final org.omg.PortableServer.portable.Delegate _get_delegate()
    {
        if ( m_delegate == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER(
                  "The Servant has not been associated with an ORB instance" );
        }
        return m_delegate;
    }

    public final void _set_delegate( org.omg.PortableServer.portable.Delegate delegate )
    {
        m_delegate = delegate;
    }
}
