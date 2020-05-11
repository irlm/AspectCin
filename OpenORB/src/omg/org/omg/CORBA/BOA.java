/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * This class describes available operations for BOA.
 *
 * @author Jerome Daniel
 */
public abstract class BOA
{
    private static final String OPENORB_CLASS = "org.openorb.orb.core.ORB";

    /**
     * Initialize BOA
     */
    public static org.omg.CORBA.BOA init( org.omg.CORBA.ORB orb, java.lang.String [] args )
    {
        BOA boa = null;
        try
        {
            Class openorbclz = Thread.currentThread().getContextClassLoader().loadClass(
                  OPENORB_CLASS );
            if ( openorbclz.isAssignableFrom( orb.getClass() ) )
            {
                java.lang.reflect.Method meth = openorbclz.getMethod( "getFeature",
                      new Class[] { String.class } );
                boa = ( org.omg.CORBA.BOA ) meth.invoke( orb,
                      new java.lang.Object[] { new String( "BOA" ) } );
            }
        }
        catch ( Exception ex )
        {
            // do nothing, throw exception below
        }
        if ( boa == null )
        {
            throw new org.omg.CORBA.INITIALIZE(
                  "The BOA has not been initialized properly!" );
        }
        return boa;
    }

    /**
     * Connect an object to the BOA
     */
    public abstract void connect( org.omg.CORBA.portable.ObjectImpl obj );

    /**
     * Connect and object and provide its key
     */
    public abstract void connect( org.omg.CORBA.portable.ObjectImpl obj, String name );


    /**
     * This operation is used to forward an object.
     */
    public abstract void forward( org.omg.CORBA.portable.ObjectImpl objOld,
           org.omg.CORBA.portable.ObjectImpl objNew );

    /**
     * Disconnect an object.
     */
    public abstract void disconnect ( org.omg.CORBA.portable.ObjectImpl obj );

    /**
     * Run the BOA ( this operation never returns ).
     */
    public abstract void impl_is_ready();

    /**
     * Activate an object.
     */
    public abstract void obj_is_ready( org.omg.CORBA.portable.ObjectImpl obj );

    /**
     * Deactivate an object
     */
    public abstract void deactivate_obj( org.omg.CORBA.portable.ObjectImpl obj );

    /**
     * Stop the BOA
     */
    public abstract void deactivate_impl();
}
