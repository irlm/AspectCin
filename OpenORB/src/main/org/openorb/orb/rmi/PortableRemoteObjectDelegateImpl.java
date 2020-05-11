/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.openorb.util.RepoIDHelper;

import org.apache.avalon.framework.logger.Logger;

/**
 * This class provides a default implementation for javax.rmi.PortableRemoteObjectDelegate
 *
 * @author Jerome Daniel
 */
public class PortableRemoteObjectDelegateImpl
    implements javax.rmi.CORBA.PortableRemoteObjectDelegate
{
    /**
     * A call to this method with no objects exported creates a non-daemon thread wich keeps the
     * java virtual machine alive until all exported objects have been unexported by calling
     * unexportObject.
     *
     * @param obj The remote object to export.
     * @throws java.rmi.RemoteException When obj is already exported or obj is a Stub.
     */
    public void exportObject( java.rmi.Remote obj )
        throws java.rmi.RemoteException
    {
        if ( obj != null )
        {
            if ( obj instanceof Stub )
            {
                throw new java.rmi.server.ExportException( "Attempted to export a stub class" );
            }
            Tie tie = Util.getTie( obj );
            if ( tie != null )
            {
                throw new java.rmi.server.ExportException( "Object already exported" );
            }
            tie = loadTie( obj );
            tie.orb( DefaultORB.getORB() );
            Util.registerTarget( tie, obj );
        }
    }

    /**
     * This method takes a server implementation object and returns a stubobject that can
     * be used to access that server object. The argument object must currently be exported,
     * either because it is a subclass f PortableRemoteObject or be virtue of a previous call
     * to PortableRemoteObject.exportObject.
     *
     * @param obj The object to create a stub for.
     * @return The stub for the object obj.
     * @throws java.rmi.NoSuchObjectException When no Tie could be found for obj.
     */
    public java.rmi.Remote toStub( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException
    {
        if ( obj instanceof Stub )
        {
            return obj;
        }
        else
        {
            Tie tie = null;
            if ( obj instanceof Tie )
            {
                tie = ( Tie ) obj;
                obj = tie.getTarget();
            }
            else
            {
                tie = Util.getTie( obj );
            }
            if ( tie == null )
            {
                throw new java.rmi.NoSuchObjectException( "Object not exported" );
            }
            org.omg.CORBA.Object thisObject = tie.thisObject();
            if ( thisObject instanceof java.rmi.Remote )
            {
                return ( java.rmi.Remote ) thisObject;
            }
            org.omg.CORBA.portable.Delegate deleg =
                    ( ( org.omg.CORBA.portable.ObjectImpl ) thisObject )._get_delegate();
            RMIRemoteStreamClass rsc = RMIRemoteStreamClass.lookup( obj.getClass() );
            int total = rsc.countStubs();
            // we always succeed to load some stub for the object in this
            // operation. We try to get a stub which implements a value
            // close to the target, but anything will do.
            Stub stub = null;
            for ( int i = 0; i < total; ++i )
            {
                try
                {
                    stub = rsc.createStub( i );
                    break;
                }
                catch ( ClassNotFoundException ex )
                {
                    // do nothing, handle below
                }
                catch ( IncompatibleClassChangeError ex )
                {
                    // do nothing, handle below
                }
            }
            if ( stub == null )
            {
                stub = new _Remote_Stub();
            }
            stub._set_delegate( deleg );
            return ( java.rmi.Remote ) stub;
        }
    }

    /**
     * This method is used to deregister a currently exported server object from the ORB
     * runtimes, allowing the object to become available for garbage collection.
     *
     * @param obj The object to unexport.
     * @throws java.rmi.NoSuchObjectException When no Tie could be found for obj.
     */
    public void unexportObject( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException
    {
        Tie tie = Util.getTie( obj );
        if ( tie == null )
        {
            throw new java.rmi.NoSuchObjectException( "Object not exported" );
        }
        Util.unexportObject( obj );
    }

    /**
     * This method takes an object reference or an object of an RMI/IDL abstract interface type
     * and attemps to narrow it to conform to the given newClass RMI/IDL type.
     *
     * @param obj The object to narrow.
     * @param newClass The type to which to narrow the object to.
     * @return The narrowed object.
     * @throws ClassCastException When obj can't be narrowed.
     */
    public Object narrow( Object obj, Class newClass )
        throws ClassCastException
    {
        return narrowExt( obj, newClass, true );
    }

    /**
     * This method takes an object reference or an object of an RMI/IDL abstract interface type
     * and attemps to narrow it to conform to the given newClass RMI/IDL type. This operation
     * always succeeds because no _is_a is sent to the server.
     *
     * @param obj The object to narrwo.
     * @param newClass The type to narrwo the object to.
     * @return The narrowed object.
     * @throws ClassCastException When obj can't be narrowed.
     */
    public Object uncheckedNarrow( Object obj, Class newClass )
        throws ClassCastException
    {
        return narrowExt( obj, newClass, false );
    }

    static Object narrowExt( Object obj, Class newClass, boolean check )
        throws ClassCastException
    {
        if ( obj == null )
        {
            return null;
        }
        // Get the object class
        java.lang.Class objClass = obj.getClass();
        // Check if objects are assignable
        if ( newClass.isAssignableFrom( objClass ) )
        {
            return obj;
        }
        // So, objects are not assignable
        if ( !newClass.isInterface()
              || !( obj instanceof org.omg.CORBA.portable.ObjectImpl ) )
        {
            throw new ClassCastException( "Class of " + obj + " is ( " + obj.getClass().getName()
                  + " ), i.e. is neither an interface nor an instance of"
                  + " 'org.omg.CORBA.portable.ObjectImpl'" );
        }
        org.omg.CORBA.portable.ObjectImpl oimpl = ( org.omg.CORBA.portable.ObjectImpl ) obj;
        Logger logger = null;
        org.omg.CORBA.ORB orb = oimpl._orb();
        if ( orb instanceof org.openorb.orb.core.ORBSingleton )
        {
            logger = ( ( org.openorb.orb.core.ORBSingleton ) orb ).getLogger();
            if ( logger != null && logger.isErrorEnabled() )
            {
                logger = logger.getChildLogger( "rmi" );
            }
        }
        org.omg.CORBA.portable.Delegate deleg;
        try
        {
            deleg = oimpl._get_delegate();
        }
        catch ( org.omg.CORBA.BAD_OPERATION ex )
        {
            if ( logger != null && logger.isErrorEnabled() )
            {
                logger.error( "Can't narrow unconnected target.", ex );
            }
            throw new IllegalArgumentException( "Can't narrow unconnected target (" + ex + ")" );
        }
        String codebase = null;
        if ( oimpl instanceof org.omg.CORBA_2_3.portable.ObjectImpl )
        {
            codebase = ( ( org.omg.CORBA_2_3.portable.ObjectImpl ) oimpl )._get_codebase();
        }
        org.omg.CORBA.portable.ObjectImpl stub;

        if ( java.rmi.Remote.class.isAssignableFrom( newClass ) )
        {
            RMIRemoteStreamClass rsc = RMIRemoteStreamClass.lookup( newClass );
            String [] ids = rsc.getRepoIDs();
            if ( check && !oimpl._is_a( ids[ 0 ] ) )
            {
                throw new ClassCastException( "The _is_a('" + ids[ 0 ] + "') on the following"
                      + " object failed: " + oimpl );
            }
            try
            {
                stub = rsc.createStub( 0 );
            }
            catch ( ClassNotFoundException ex )
            {
                if ( logger != null && logger.isErrorEnabled() )
                {
                    logger.error( "Stub creation failed.", ex );
                }
                throw new ClassCastException( "Stub creation failed (" + ex + ")" );
            }
        }
        else
        {
            String [] str = RepoIDHelper.extractClassName( newClass );
            String stubName = ( ( str[ 0 ].length() == 0 ) ? "_"
                  : ( str[ 0 ] + "._" ) ) + str[ 1 ] + "_Stub";
            try
            {
                Class helper = Util.loadClass( stubName, codebase, newClass.getClassLoader() );
                stub = ( org.omg.CORBA.portable.ObjectImpl ) helper.newInstance();
            }
            catch ( ClassNotFoundException ex )
            {
                if ( logger != null && logger.isErrorEnabled() )
                {
                    logger.error( "Helper class " + stubName + " not found.", ex );
                }
                throw new ClassCastException( "Helper class " + stubName
                      + " not found (" + ex + ")" );
            }
            catch ( InstantiationException ex )
            {
                if ( logger != null && logger.isErrorEnabled() )
                {
                    logger.error( "Instantiation of class " + stubName + " failed.", ex );
                }
                throw new ClassCastException( "Instantiation of class " + stubName
                      + " failed (" + ex + ")" );
            }
            catch ( IllegalAccessException ex )
            {
                if ( logger != null && logger.isErrorEnabled() )
                {
                    logger.error( "Illegal access while creating class " + stubName + ".", ex );
                }
                throw new ClassCastException( "Illegal access while creating class " + stubName
                      + " (" + ex + ")" );
            }
            String [] ids = stub._ids();
            if ( check && !oimpl._is_a( ids[ 0 ] ) )
            {
                throw new ClassCastException( "The _is_a('" + ids[ 0 ] + "') on the following"
                      + " object failed: " + oimpl );
            }
        }
        stub._set_delegate( deleg );
        return stub;
    }

    /**
     * This method makes the remote object target ready for remote communication using the
     * same communications runtime as source.
     *
     * @param target The target object to connect to the source's ORB.
     * @param source The source object to take the ORB from.
     * @throws java.rmi.RemoteException When the source object is not exported or
     * the target is already exported.
     */
    public void connect( java.rmi.Remote target, java.rmi.Remote source )
        throws java.rmi.RemoteException
    {
        org.omg.CORBA.ORB orb = null;
        // Get ORB from source
        if ( source instanceof org.omg.CORBA.portable.ObjectImpl )
        {
            try
            {
                orb = ( ( org.omg.CORBA.portable.ObjectImpl ) source )._orb();
            }
            catch ( org.omg.CORBA.BAD_OPERATION ex )
            {
                // do nothing, handle below
            }
        }
        else
        {
            Tie tie = Util.getTie( source );
            if ( tie != null )
            {
                orb = tie.orb();
            }
        }
        if ( orb == null )
        {
            throw new java.rmi.RemoteException( "Source object not exported" );
        }
        if ( target instanceof Stub )
        {
            ( ( Stub ) target ).connect( orb );
        }
        else
        {
            Tie tie = Util.getTie( target );
            if ( tie != null )
            {
                throw new java.rmi.server.ExportException( "Object already exported" );
            }
            tie = loadTie( target );
            tie.orb( orb );
            Util.registerTarget( tie, target );
        }
    }

    static Tie loadTie( java.rmi.Remote obj )
        throws java.rmi.server.ExportException
    {
        RMIRemoteStreamClass rsc = RMIRemoteStreamClass.lookup( obj.getClass() );
        if ( rsc == null )
        {
            throw new java.rmi.server.ExportException(
                    "Target object is not compatable with RMI/IIOP" );
        }
        return rsc.createTie();
    }
}

