/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import org.openorb.orb.adapter.ObjectAdapter;

/**
 * This class is the controller for most server side operations. It is
 * responsible for registration and thread management for server protocols
 * and channels, dispatch and thread management for requests, object
 * reference construction and generates the adapter managers allowing flow
 * control for adapters.
 *
 * @author Unknown
 */
public interface ServerManager
{
    /**
     * @return orb associated with the server manager.
     */
    org.omg.CORBA.ORB orb();

    // policy settings

    /**
     * Set thread pool size ranges. The thread pool size will be somewhere
     * within this range, depending on load.
     *
     * @param min minimum size for thread pool. Must be greater than 0 and less
     * than max.
     * @param max maximum thread pool size. If this is 0 then requests will
     * only be processed when the orb.perform_work function is called. Must
     * be greater than 0.
     */
    void setThreadPoolLimits( int min, int max );

    /**
     * Set maximum queue size. If the queue grows to this size then new
     * incoming requests will be rejected with a TRANSIENT exception.
     * Changing this value downwards will not result in queued requests
     * being destroyed.
     *
     * @param maxQueueSize maximum queue size. Must be greater than 1. Values
     * should be several times the maximum thread pool size.
     */
    void setMaxQueueSize( int maxQueueSize );

    /**
     * Maximum number of requests which may be held by an adapter in the
     * HOLDING state. Changes to this value will only affect newly created
     * object adapters.
     *
     * @param maxManagerHeldRequests ceiling on adapter held requests.
     * Individial adapter managers may modify this value downwards. Use 0 to
     * dissallow the adapter holding state, and Integer.MAX_VALUE for
     * no limit.
     */
    void setMaxManagerHeldRequests( int maxManagerHeldRequests );

    // object id creation and manipulation.

    /**
     * This function is used by adapters to generate a key which will
     * participate in adapter lookup short-circuiting.
     */
    byte [] create_cacheable_object_key( boolean use_suid,
                                                byte [][] parts );

    /**
     * Extract the component parts of a cacheable object_key. Returns null if
     * is_cacheable_object_key would return false.
     */
    byte [][] extract_cacheable_object_key( byte [] object_key );

    /**
     * Returns true if the given object_key can be used in the cache.
     */
    boolean is_cacheable_object_key( byte [] object_key );

    /**
     * Returns true if the given object key has a persistent target.
     */
    boolean is_suid_object_key( byte [] object_key );

    /**
     * Returns true if the given object_key is cacheable and was
     * created by this server.
     */
    boolean is_local_cacheable_object_key( byte [] object_key );

    // communication registration + thread management.

    /**
     * Register a server protocol. This registers the protocol for purposes of
     * constructing IORs. When the protocol begins listening it should call
     * protocol_listening.<p/>
     *
     * If multiple protocols are registered with the same tag value, or a
     * profile is registered with multiple tag values then the constructed
     * IOR will contain multiple profiles, one from each distinct registered
     * protocol/tag pair. Registering the same protocol with the same tag more
     * than once will not result in multiple profiles.
     *
     * @param profile_tag Profile tag of IOR consturcted with the protocol.
     * @param protocol The server protocol.
     */
    void register_protocol( int profile_tag, ServerProtocol protocol );

    /**
     * Return the array of registered protocols.
     */
    Object[] get_protocol_ids();

    /**
     * This is called when a server protocol begins listening. Thread resources
     * are allocated to call the listen function regularly.
     *
     * @param protocol The server protocol.
     */
    void protocol_listening( ServerProtocol protocol );

    /**
     * This is called when a server protocol is no longer listening. It stops
     * getting serviced by the pool thread.
     *
     * @param protocol The server protocol.
     * @param paused If true when the server side is shut down close will still
     * be called on the protocol.
     */
    void protocol_not_listening( ServerProtocol protocol,
                                        boolean paused );

    /**
     * This operation occours when a protocol accepts a new incoming channel.
     */
    void register_channel( ServerChannel channel );

    /**
     * Called when a channel enters the closed state. Returns once
     * all channel threads have completed their work cycles.
     */
    void unregister_channel( ServerChannel channel );

    /**
     * This fuction runs in a separate thread and cleans up
     * any channels which are not used for a long time.
     */
    void channel_reaper();

    // adapter location, registration and unregistration.

    /**
     * This function finds an adapter for the specified object id. If an
     * adapter for an object with the given object id cannot be found this
     * returns null. This varient of the find_adapter function is used by
     * local bindings. It ignores the state of the adapter manager and any
     * single threading policies, requests are held only if the target
     * adapter or one of it's ancestors is in the process of being destroyed.
     *
     * @return The adapter specified in the object key.
     */
    ObjectAdapter find_adapter( byte [] object_key );

    /**
     * Register an adapter. If the specified adapter is already
     * registered no change occours. This will occour automaticaly for
     * child adapters of some root adapter.
     */
    void register_adapter( byte [] aid, ObjectAdapter adapter );

    /**
     * Unregister an adapter. If the specified adapter is not registered
     * no change occours. If an adapter throws an AdapterDestroyed exception
     * it must eventualy call this function to continue processing the held
     * requests.
     */
    void unregister_adapter( byte [] aid );

    /**
     * Create an adapter manager.
     */
    AdapterManager create_adapter_manager();


    // queue management

    /**
     * Begin dispatch sequence for incoming request. This will enqueue the
     * request and return immediatly.
     */
    void enqueue_request( ServerRequest request );

    /**
     * Returns true if there are requests waiting in the incomming request
     * queue for resources.
     */
    boolean work_pending();

    /**
     * This is called from orb.perform_work, it serves a single request.
     *
     * @return true if a request was served, false if the queue is empty
     */
    boolean serve_request( boolean wait );

    /**
     * Start up the server.
     *
     * @param block true if this funtion should not return until
     * the server is shut down.
     * @param allowPool true if the thread pool is allowed. Once a call
     * the thread pool has been started it cannot be dissallowed
     * in the future by calling this with a false.
     */
    void startup( boolean block, boolean allowPool );

    /**
     * Shutdown the server side. All new incoming requests will be rejected.
     *
     * @param wait_for_complete If true wait until all channels have closed
     * and incomming requests have finished processing. If false all
     * active requests will be canceled and the function will return once
     * connections have shutdown.
     */
    void shutdown( boolean wait_for_complete );

}

