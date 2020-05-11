/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter;

import org.omg.PortableInterceptor.ForwardRequest;

/**
 * Object adapters act as a contianer for servant objects and for child
 * adapters. Servants and adapters may be created by object adapters in
 * response to a particular request being initiated.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:45 $
 */
public interface ObjectAdapter
{
   /**
    * Expected lifetime of the adapter. Higher numbers are more likley to be
    * dropped from the lookup cache. If this returns 0 then the adapter should
    * never be dropped. This value should be stable throughout the lifetime of
    * the adapter. The highest byte will be used for determining the binding
    * priority.<p>
    * <PRE>
    * Suggested values:
    * = 0          Root adapters. Always keep.
    * < 0x1000000  Adpaters created directly. (these will never be
    *              temporarily etherialized)
    * < 0x2000000  Adapters created dynamicaly.
    * < 0x3000000  Objects created directly.
    * < 0x4000000  Objects created dynamicaly.
    * < 0          Never cache adapter, single invocation only.
    *             These should not be returned from find_adapter.
    * </PRE>
    */
    int cache_priority();

    /**
     * Adapter is single threaded. All non-local calls to single threaded
     * adapters are serialized.
     */
    boolean single_threaded();

    /**
     * Etherealize the adapter. When this function returns the adapter's memory
     * resident state should have been minimized. This function will always be
     * called before purging the adapter from the cache.
     *
     * @param cleanup_in_progress if true the adapter is being perminently
     *            deactivated and will no longer have to dispatch operations.
     */
    void etherealize( boolean cleanup_in_progress );

    /**
     * Queue manager for the adapter. This may return null for an adapter which
     * is always active. To create an adapter manager for an adapter use the
     * create_manager operation on the ServerManager.
     */
    org.openorb.orb.net.AdapterManager getAdapterManager();

    /**
     * Find an adapter or an ancestor adapter to serve requests to the specified
     * object key.
     *
     * If this adapter serves this object directly it should return
     * itself, if it can find a decendant adapter which serves the request
     * without entering user code the decendant is returned. If user code
     * associated with a decendant adapter which does not share this adapter's
     * adapter manager or is a single thread adapter must be excecuted in
     * order to create an adapter then that decendant adapter is returned.
     *
     * In essence the requirement that the adapter manager must be consulted
     * before excecuting user code is preserved.
     *
     * If a decendant adapter is in the process of being destroyed the
     * AdapterDestroyedException is thrown. The find operation can be re-tried
     * once the adapter has been destroyed.
     *
     * The object key passed to this argument will always be prefixed by the
     * adapter id as registered in the server manager. If only one registration
     * is present then the prefix does not need to be checked for a match with
     * the adapter id.
     */
    ObjectAdapter find_adapter( byte [] object_key )
        throws AdapterDestroyedException;

    /**
     * Return the adapter id. This should be a prefix of the object_key if the
     * object id is cacheable, it should be stable with respect to a given
     * object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    byte[] adapter_id( byte [] object_key );

    /**
     * Return the object id. This should should be a suffix of the object_key
     * if the object id is cacheable, it should be stable with respect to a
     * given object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    byte[] object_id( byte [] object_key );

    /**
     * Returns a PolicyList containing the Polices set for the
     * requested PolicyTypes. If the specified sequence is empty, all
     * Policy overrides will be returned. If none of the
     * requested PolicyTypes are overridden an empty sequence is returned.
     */
    org.omg.CORBA.Policy[] get_server_policies( int[] ts );

    // local dispatch operations

    /**
     * If this returns true then requests for the specified object id
     * must be sent through the network. This will be true for example
     * when using the DSI. This should return true if the object_key is
     * unknown to the adapter. The result should be stable with respect to a
     * given object ID.
     */
    boolean forced_marshal( byte [] object_key )
        throws AdapterDestroyedException;

    /**
     * Preinvoke a local operation. Always paired with a call to
     * servant_postinvoke.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * this throws an OBJECT_NOT_EXIST exception.
     */
    org.omg.CORBA.portable.ServantObject servant_preinvoke( byte [] object_key,
            String operation, Class expectedType )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * Close off a local operation. Always paired with a call to
     * servant_preinvoke
     */
    void servant_postinvoke( byte [] object_key,
            org.omg.CORBA.portable.ServantObject srvObject );

    /**
     * Respond to a local locate request. This returns true if the object
     * is located locally, false if the object is unknown and throws a forward
     * request for a location forward. This should not throw a system
     * exception.
     */
    boolean locate( byte [] object_key )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * is_a operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    boolean is_a( byte [] object_key, String repository_id )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * get_interface_def operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    org.omg.CORBA.Object get_interface_def( byte [] object_key )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * get_domain_manager operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    org.omg.CORBA.DomainManager [] get_domain_managers( byte [] object_key )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * get_componenent operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    org.omg.CORBA.Object get_component( byte [] object_key )
        throws ForwardRequest, AdapterDestroyedException;

    // dispatch operations.

    /**
     * Locate the servant object for a request. The returned object is
     * handed to the dispatch operation. This may throw a system exception or
     * respond with a forward request if one is indicated, in which case the
     * dispatch operation will not be called.
     *
     * @param req out parameter holding repository ids of all available
     * interfaces with the most derived interface appearing first.
     * @return the 'target' of the operation. This is simply passed to the
     * dispatch operation and is not interpreted in any way.
     */
    TargetInfo predispatch( org.openorb.orb.net.ServerRequest req )
        throws ForwardRequest, AdapterDestroyedException;

    /**
     * Dispatch a request from a client. This may throw a system exception at
     * any time or call the ResponseHandler interface on the request to create
     * a standard reply. If this returns without calling a response handler an
     * empty reply is constructed, this is the usual situation for a locate
     * request.
     */
    void dispatch( org.openorb.orb.net.ServerRequest req, TargetInfo target );

    /**
     * Cancel a dispatch. This may follow a predispatch or dispatch call to
     * indicate that the client no longer expects any reply from the request
     * and the server can stop expending effort towards completing it.
     */
    void cancel_dispatch( org.openorb.orb.net.ServerRequest req,
           TargetInfo target );
}

