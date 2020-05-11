/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;

import org.omg.PortableServer.POAManagerPackage.State;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.OMGVMCID;

import org.openorb.orb.policy.OPENORB_VPVID;

import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.AdapterDestroyedException;

import org.openorb.orb.util.Trace;
import org.openorb.util.MergeStack;
import org.openorb.util.ExceptionTool;
import org.openorb.util.NumberCache;

/**
 * This class is the controler for most server side operations. It is
 * responsible for registration and thread management for server protocols
 * and channels, dispatch and thread management for requests, object
 * reference construction and generates the adapter managers allowing flow
 * control for adapters.
 *
 * @author Unknown
 */
public class ServerManagerImpl
    implements ServerManager
{
    /**
     * Separates parts of object keys.
     */
    private static final byte SEP_VAL = ( byte ) 0xFE;

    /**
     * Escape character for allowing {@link #SEP_VAL} as well as itself to be
     * inserted into object keys.
     */
    private static final byte ESC_VAL = ( byte ) 0xFD;

    /**
     * Procees unique identifier flag. This flag appears in the byte at index 3
     * to specify the id is process unique rather than server unique.
     */
    private static final byte FLAG_PUID = 0x01;

    /**
     * This is a byte sequence which is unique to this orb instance, the process
     * unique identifier. It is very unlikly that two orbs will share the same
     * puid, hostname, and listening port, thus nonpersistance of references
     * encapsulating this value is guaranteed.
     */
    private byte [] m_process_uid;

    /**
     * This is a byte sequence which is unique to this server, it can be
     * set with the -ORBServerAlias flag, or if unset defaults to an empty
     * sequence. Processes with the same suid can create persistent references
     * which are valid outside process boundaries.
     */
    private byte [] m_server_uid;

    /**
     * Reference to the orb object.
     */
    private org.omg.CORBA.ORB m_orb;

    /**
     * Thread group of thread which called the constructor.
     */
    private ThreadGroup m_root_group;

    /**
     * Return the orb.
     * @return orb associated with the server manager.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    // exceptions for various request failure conditions.

    private static SystemException createQueueFullException()
    {
        return new TRANSIENT( OPENORB_VPVID.value | 1, CompletionStatus.COMPLETED_NO );
    }

    private static SystemException createDiscardException()
    {
        return new TRANSIENT( OMGVMCID.value | 1, CompletionStatus.COMPLETED_NO );
    }

    private static SystemException createInactiveException()
    {
        return new OBJ_ADAPTER( OMGVMCID.value | 1, CompletionStatus.COMPLETED_NO );
    }

    private static SystemException createShutdownException()
    {
        return new BAD_INV_ORDER( OMGVMCID.value | 4, CompletionStatus.COMPLETED_NO );
    }

    // channel and protocol registration.

    /**
     * Synchronization for the protocol and channel sets
     */
    protected final Object m_sync_io = new Object();

    /**
     * Thread group containing IO threads.
     */
    private ThreadGroup m_io_threads;

    /**
     * Static thread group. Used in tests to avoid creating large numbers of
     * empty thread groups.
     */
    private static ThreadGroup s_static_io_threads;

    /**
     * If true the static thread groups will be used in preference to the
     * normal ones.
     */
    private boolean m_use_static_thread_group;

    /**
     * Array of registered protocols and their profile IDs.
     */
    private Object [] m_protocol_profile_ids = new Object[ 0 ];

    /**
     * Active server protocols. This maps the protocol to the thread if
     * the protocol is listening, or to null if the protocol is not listening
     * but still requires a close on shutdown.
     */
    private Map m_protocol_to_worker = new HashMap();

    /**
     * Active server channels. This maps the channels to the worker threads
     * using the channel.
     */
    protected Map m_channels = new HashMap();

    /**
     * shuts down channels which have been quiet for a while
     */
    private Thread m_channel_reaper = null;

    /**
     * Starts up new Pool threads if required.
     */
    private Thread m_pool_thread_manager = null;

    /**
     * Minimum time for a channel to remain quiet before it's channel is
     * shut down. This is also used in thread pool size optimization.
     */
    private int m_channel_closing_time;
    private static final int DEFAULT_CLOSE_TIME = 10 * 60 * 1000;

    /**
     * Minimum time for a thread waiting for a request before it shuts down.
     */
    private static final int DEFAULT_THREAD_POOL_WAIT_TIME = 5 * 60 * 1000;

    /**
     * this syncronizes all the work queues, the states of the adapter
     * managers, and the adapter cache. A fairly course grained lock
     * although this avoids many deadlock and oversynchronization problems.
     */
    private Object m_sync_state = new Object();

    private volatile boolean m_shutdown = false;
    private volatile boolean m_running = false;
    private volatile boolean m_io_complete = false;

    /**
     * This will be a special adapter which redirects all requests to a
     * reference resolved from resolve_initial_references.
     */
    private ObjectAdapter m_default_adapter = null;

    /**
     * Map containing all adapters which have served a request.
     * In future this may be cleared out occasionaly of adapters which can
     * be recreated easily.
     */
    private Map m_adapter_cache = new HashMap();

    /**
     * Held requests. Requests waiting for some type of event. This counter
     * is kept more for checking than anything.
     */
    private volatile int m_holding_requests = 0;

    /**
     * Synchronization queue for thread pool and request queue. All
     * variables below sync on this, up to misc.
     */
    private SyncQueue m_sync_queue = new SyncQueue();

    /**
     * Requests queued ready for processing. All variables in this section
     * synchronize on this.
     */
    private MergeStack m_requests = new MergeStack();

    /**
     * Maximum queue length. This is the total number of requests which may be
     * queued before new incoming requests begin being rejected.
     */
    private int m_max_queue_size = 0;

    /**
     * Maximum number of manager held requests before new requests begin being
     * rejected.
     */
    private int m_max_mgr_held_requests = 0;

    /**
     * The current single threaded thread. There is only ever one.
     */
    private volatile Thread m_single_thread = null;

    /**
     * Head of request queue for requests held waiting for single thread
     * availability.
     */
    private MergeStack m_waiting_requests = new MergeStack();

    /**
     * Current thread pool size. This can change dynamicaly.
     */
    private int m_thread_pool_size = 0;

    /**
     * Unique thread id. Used as thread name for pooled threads.
     */
    private int m_next_tid = 0;

    /**
     * This set includes all server threads, both pool threads and threads
     * passed through serve_request.
     */
    private Set m_server_threads = new HashSet();

    /**
     * Allow use of thread pool.
     */
    private boolean m_allow_pool = false;

    /**
     * Thread group for thread pool threads
     */
    private ThreadGroup m_pool_threads;

    /**
     * Static thread group. Used in tests to avoid creating large numbers of
     * empty thread groups.
     */
    private static ThreadGroup s_static_pool_threads;

    /**
     * Maximum thread pool size. Thread pool will not grow beyond this size.
     */
    private int m_max_thread_pool_size = 5;

    /**
     * Minimum thread pool size. Thread pool will not shrink beyond this size.
     */
    private int m_min_thread_pool_size = 1;

    /**
     * Used For Logging
     */
    private Logger m_logger;

    /**
     * Construct new server manager. There should be one of these objects
     * per orb.
     * @param orb The controling orb.
     */
    public ServerManagerImpl( org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
        m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        org.openorb.orb.config.ORBLoader loader =
            ( ( org.openorb.orb.core.ORB ) orb ).getLoader();

        // initialize policies.
        try
        {
            byte [] ssuid = loader.getStringProperty( "openorb.server.alias",
                "" ).getBytes( "UTF-8" );
            m_server_uid = new byte[ ssuid.length + 1 ];
            System.arraycopy( ssuid, 0, m_server_uid, 0, ssuid.length );
            m_server_uid[ ssuid.length ] = SEP_VAL;
        }
        catch ( final java.io.UnsupportedEncodingException ex )
        {
            getLogger().error( "Encoding not supported.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Encoding not supported (" + ex + ")" ), ex );
        }

        m_max_queue_size = loader.getIntProperty(
            "openorb.server.maxQueueSize", Integer.MAX_VALUE );
        m_max_mgr_held_requests = loader.getIntProperty(
            "openorb.server.maxManagerHeldRequests", Integer.MAX_VALUE );
        m_min_thread_pool_size = loader.getIntProperty(
            "openorb.server.minThreadPoolSize", 1 );
        m_max_thread_pool_size = loader.getIntProperty(
            "openorb.server.maxThreadPoolSize", 10 );

        m_root_group = Thread.currentThread().getThreadGroup();

        long time = System.currentTimeMillis();

        m_process_uid = new byte[ 8 ];
        m_process_uid[ 7 ] = ( byte ) ( time >>> 56 );
        m_process_uid[ 6 ] = ( byte ) ( time >>> 48 );
        m_process_uid[ 5 ] = ( byte ) ( time >>> 40 );
        m_process_uid[ 4 ] = ( byte ) ( time >>> 32 );
        m_process_uid[ 3 ] = ( byte ) ( time >>> 24 );
        m_process_uid[ 2 ] = ( byte ) ( time >>> 16 );
        m_process_uid[ 1 ] = ( byte ) ( time >>> 8 );
        m_process_uid[ 0 ] = ( byte ) time;

        m_channel_closing_time = loader.getIntProperty(
            "openorb.server.reapCloseDelay", DEFAULT_CLOSE_TIME );
        m_use_static_thread_group = loader.getBooleanProperty(
            "openorb.useStaticThreadGroup", false );
    }

    /**
     * Set thread pool size ranges. The thread pool size will be somewhere
     * within this range, depending on load.
     *
     * @param min minimum size for thread pool. Must be greater than 0 and
     * less than max.
     * @param max maximum thread pool size. If this is 0 then requests
     * will only be processed when the orb.perform_work function is
     * called. Must be greater than 0.
     */
    public void setThreadPoolLimits( int min, int max )
    {
        if ( min < 0 || max < 0 || min > max )
        {
            throw new java.lang.IllegalArgumentException(
                "Invalid thread pool size range" );
        }
        synchronized ( m_sync_state )
        {
            m_min_thread_pool_size = min;
            m_max_thread_pool_size = max;
        }
    }

    /**
     * Set maximum queue size. If the queue grows to this size then new
     * incoming requests will be rejected with a TRANSIENT exception.
     * Changing this value downwards will not result in queued requests
     * being destroyed.
     *
     * @param maxQueueSize maximum queue size. Must be greater than 1. Values
     *         should be several times the maximum thread pool size.
     */
    public void setMaxQueueSize( int maxQueueSize )
    {
        if ( maxQueueSize < 1 )
        {
            throw new java.lang.IllegalArgumentException(
                "Invalid max queue size" );
        }
        synchronized ( m_sync_state )
        {
            m_max_queue_size = maxQueueSize;
        }
    }

    /**
     * Maximum number of requests which may be held by an adapter in the HOLDING
     * state. Changes to this value will only affect newly created object
     * adapters.
     *
     * @param maxManagerHeldRequests ceiling on adapter held requests.
     * Individial adapter managers may modify this value downwards. Use 0 to
     * dissallow the adapter holding state, and Integer.MAX_VALUE for
     * no limit.
     */
    public void setMaxManagerHeldRequests( int maxManagerHeldRequests )
    {
        if ( maxManagerHeldRequests < 0 )
        {
            throw new java.lang.IllegalArgumentException(
                "Invalid max held requests" );
        }
        synchronized ( m_sync_state )
        {
            m_max_mgr_held_requests = maxManagerHeldRequests;
        }
    }

    /**
     * This function is used by adapters to generate a key which will
     * participate in adapter lookup short-circuiting.
     */
    public byte [] create_cacheable_object_key( boolean use_suid,
                                                byte [][] parts )
    {
        // header + puid/suid + sep-chars
        int len = 4 + ( use_suid ? m_server_uid.length : m_process_uid.length )
              + parts.length - 1;

        // count the number of bits needing escapes
        int escs = 0;
        for ( int i = 0; i < parts.length; ++i )
        {
            len += parts[ i ].length;
            for ( int j = 0; j < parts[ i ].length; ++j )
            {
                if ( parts[ i ][ j ] == SEP_VAL || parts[ i ][ j ] == ESC_VAL )
                {
                    ++escs;
                }
            }
        }

        len += escs;
        byte [] object_key = new byte[ len ];

        // construct the header.
        object_key[ 0 ] = 0;
        object_key[ 1 ] = ( byte ) 'O';
        object_key[ 2 ] = ( byte ) 'O';
        int pos;
        if ( use_suid )
        {
            object_key[ 3 ] = 0;
            System.arraycopy( m_server_uid, 0, object_key, 4, m_server_uid.length );
            pos = 4 + m_server_uid.length;
        }
        else
        {
            object_key[ 3 ] = FLAG_PUID;
            System.arraycopy( m_process_uid, 0, object_key, 4, m_process_uid.length );
            pos = 4 + m_process_uid.length;
        }

        int p = 0;
        while ( p < parts.length && escs > 0 )
        {
            for ( int i = 0; i < parts[ p ].length; ++i, ++pos )
            {
                if ( parts[ p ][ i ] == SEP_VAL || parts[ p ][ i ] == ESC_VAL )
                {
                    object_key[ pos++ ] = ESC_VAL;
                    escs--;
                }
                object_key[ pos ] = parts[ p ][ i ];
            }
            if ( pos < object_key.length )
            {
                object_key[ pos++ ] = SEP_VAL;
            }
            ++p;
        }

        for ( ; p < parts.length; ++p )
        {
            System.arraycopy( parts[ p ], 0, object_key, pos,
                parts[ p ].length );
            pos += parts[ p ].length;
            if ( pos < object_key.length )
            {
                object_key[ pos++ ] = SEP_VAL;
            }
        }
        return object_key;
    }

    /**
     * Extract the component parts of a cacheable object_key. Returns null if
     * is_cacheable_object_key would return false.
     */
    public byte [][] extract_cacheable_object_key( byte [] object_key )
    {
        int [] offsets = find_object_key_offsets( object_key );
        if ( offsets == null )
        {
            return null;
        }
        byte [][] parts = new byte[ offsets.length ][];
        int end = object_key.length;
        for ( int i = parts.length - 1; i >= 0; --i )
        {
            int begin = offsets[ i ];
            // count the escapes
            int escs = 0;
            for ( int j = begin; j < end; ++j )
            {
                if ( object_key[ j ] == ESC_VAL )
                {
                    ++j;
                    ++escs;
                }
            }
            // extract the object_key part.
            parts[ i ] = new byte[ end - begin - escs ];
            if ( escs == 0 )
            {
                System.arraycopy( object_key, begin, parts[ i ], 0,
                    end - begin );
            }
            else
            {
                for ( int j = begin, k = 0; j < end; ++j, ++k )
                {
                    if ( object_key[ j ] == ESC_VAL )
                    {
                        ++j;
                    }
                    parts[ i ][ k ] = object_key[ j ];
                }
            }
            end = begin - 1;
        }
        return parts;
    }

    private int [] find_object_key_offsets( byte [] object_key )
    {
        if ( !is_local_cacheable_object_key( object_key ) )
        {
            return null;
        }
        int start = 4;

        if ( ( object_key[ 3 ] & FLAG_PUID ) == FLAG_PUID )
        {
            start += m_process_uid.length;
        }
        else
        {
            start += m_server_uid.length;
        }
        // count parts
        int parts = 1;

        for ( int i = start; i < object_key.length; ++i )
        {
            if ( object_key[ i ] == SEP_VAL )
            {
                ++parts;
            }
            else if ( object_key[ i ] == ESC_VAL )
            {
                ++i;
            }
        }
        // generate the offsets.
        int [] offsets = new int[ parts ];

        offsets[ 0 ] = start;

        int upto = 1;

        for ( int i = start; i < object_key.length && upto < offsets.length; ++i )
        {
            if ( object_key[ i ] == SEP_VAL )
            {
                offsets[ upto++ ] = i + 1;
            }
            else if ( object_key[ i ] == ESC_VAL )
            {
                ++i;
            }
        }
        return offsets;
    }

    /**
     * Returns true if the given object_key can be used in the cache.
     */
    public boolean is_cacheable_object_key( byte [] object_key )
    {
        return ( object_key.length >= 4
              && object_key[ 0 ] == 0
              && object_key[ 1 ] == ( byte ) 'O'
              && object_key[ 2 ] == ( byte ) 'O' );
    }

    /**
     * Returns true if the given object key has a persistent target.
     */
    public boolean is_suid_object_key( byte [] object_key )
    {
        return ( object_key.length >= 4
              && ( object_key[ 3 ] & FLAG_PUID ) == 0 );
    }

    /**
     * Returns true if the given object_key is cacheable and was
     * created by this server.
     */
    public boolean is_local_cacheable_object_key( byte [] object_key )
    {
        if ( !is_cacheable_object_key( object_key ) )
        {
            return false;
        }
        if ( ( object_key[ 3 ] & FLAG_PUID ) == FLAG_PUID )
        {
            if ( object_key.length - 4 < m_process_uid.length )
            {
                return false;
            }
            for ( int i = 0; i < m_process_uid.length; ++i )
            {
                if ( object_key[ i + 4 ] != m_process_uid[ i ] )
                {
                    return false;
                }
            }
        }
        else
        {
            if ( object_key.length - 4 < m_server_uid.length )
            {
                return false;
            }
            for ( int i = 0; i < m_server_uid.length; ++i )
            {
                if ( object_key[ i + 4 ] != m_server_uid[ i ] )
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Register a server protocol. This registers the protocol for purposes of
     * constructing IORs. When the protocol begins listening it should call
     * protocol_listening.
     *
     * If multiple protocols are registered with the same tag value, or
     * a profile registered with multiple tag values then the constructed
     * IOR will contain multiple profiles, one from each distinct registered
     * protocol/tag pair. Registering the same protocol with the same tag more
     * than once will not result in multiple profiles.
     *
     * @param profile_tag Profile tag of IOR consturcted with the protocol.
     * @param protocol The server protocol.
     */
    public void register_protocol( int profile_tag, ServerProtocol protocol )
    {
        if ( protocol == null )
        {
            return;
        }
        for ( int i = 0; i < m_protocol_profile_ids.length; ++i )
        {
            if ( m_protocol_profile_ids[ i ] == protocol
                  && profile_tag == ( ( Integer )
                  m_protocol_profile_ids[ i + 1 ] ).intValue() )
            {
                return;
            }
        }
        Object [] tmp = new Object[ m_protocol_profile_ids.length + 2 ];

        System.arraycopy( m_protocol_profile_ids, 0, tmp, 0, m_protocol_profile_ids.length );

        tmp[ m_protocol_profile_ids.length ] = protocol;

        tmp[ m_protocol_profile_ids.length + 1 ] = NumberCache.getInteger( profile_tag );

        m_protocol_profile_ids = tmp;
    }

    /**
     * Return the array of registered protocols.
     */
    public Object[] get_protocol_ids()
    {
        return m_protocol_profile_ids;
    }

    /**
     * This is called when a server protocol begins listening. Thread resources
     * are allocated to call the listen function regularly.
     *
     * @param protocol The server protocol.
     */
    public void protocol_listening( ServerProtocol protocol )
    {
        synchronized ( m_sync_io )
        {
            if ( m_shutdown )
            {
                return;
            }
            if ( m_io_threads == null || m_io_threads.isDestroyed() )
            {
                if ( m_use_static_thread_group )
                {
                    if ( s_static_io_threads == null || s_static_io_threads.isDestroyed() )
                    {
                        s_static_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                    }
                    m_io_threads = s_static_io_threads;
                }
                else
                {
                    m_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                }
            }

            // TODO: do somthing better than this. Have a thread pool for io.
            Thread p = new Thread( m_io_threads, new ProtocolRunner( protocol ),
                    "Worker for " + protocol.toString() );

            p.setDaemon( true );

            p.start();

            m_protocol_to_worker.put( protocol, p );
        }
    }

    private class ProtocolRunner implements Runnable
    {
        public ProtocolRunner( ServerProtocol svrproto )
        {
            m_server_protocol = svrproto;
        }

        private ServerProtocol m_server_protocol;
        public void run()
        {
            // this returns on interrupt or shutdown
            if ( wait_for_startup() )
            {
                m_server_protocol.run_listen();
            }
        }
    }

    /**
     * This is called when a server protocol is no longer listening. It stops
     * getting serviced by the pool thread.
     *
     * @param protocol The server protocol.
     * @param paused If true when the server side is shut down close will still
     *         be called on the protocol.
     */
    public void protocol_not_listening( ServerProtocol protocol, boolean paused )
    {
        Thread thread;

        synchronized ( m_sync_io )
        {
            if ( paused )
            {
                thread = ( Thread ) m_protocol_to_worker.get( protocol );

                if ( thread != null )
                {
                    m_protocol_to_worker.put( protocol, null );
                }
            }
            else
            {
                thread = ( Thread ) m_protocol_to_worker.remove( protocol );
            }
        }

        if ( thread != null )
        {
            Thread curr = Thread.currentThread();
            thread.interrupt();

            while ( thread != curr && thread.isAlive() )
            {
                try
                {
                    thread.join();
                }
                catch ( InterruptedException ex )
                {
                    // TODO: ???
                }
            }
        }
    }

    /**
     * This operation occours when a protocol accepts a new incoming channel.
     */
    public void register_channel( ServerChannel channel )
    {
        synchronized ( m_sync_io )
        {
            if ( m_shutdown )
            {
                return;
            }
            if ( m_channels.containsKey( channel ) )
            {
                return;
            }
            if ( m_io_threads == null || m_io_threads.isDestroyed() )
            {
                if ( m_use_static_thread_group )
                {
                    if ( s_static_io_threads == null || s_static_io_threads.isDestroyed() )
                    {
                        s_static_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                    }
                    m_io_threads = s_static_io_threads;
                }
                else
                {
                    m_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                }
            }

            Thread thread = new Thread( m_io_threads, new ChannelRecvRunner( channel ),
                   "Receive Worker for " + channel.toString() );
            thread.setDaemon( true );
            thread.start();

            m_channels.put( channel, thread );
        }
    }

    /**
     * This is the runner thread for receiving incoming requests.
     */
    private class ChannelRecvRunner implements Runnable
    {
        public ChannelRecvRunner( ServerChannel chan )
        {
            m_server_channel = chan;
        }

        private ServerChannel m_server_channel;
        public void run()
        {
            // this returns on interrupt or shutdown
            if ( wait_for_startup() )
            {
                m_server_channel.run_recv();
            }
        }
    }

    /**
     * Called when a channel enters the closed state. Returns once
     * all channel threads have completed their work cycles.
     */
    public void unregister_channel( ServerChannel channel )
    {
        Thread thread;

        synchronized ( m_sync_io )
        {
            thread = ( Thread ) m_channels.remove( channel );
        }

        if ( thread != null )
        {
            Thread curr = Thread.currentThread();
            thread.interrupt();

            while ( thread != curr && thread.isAlive() )
            {
                try
                {
                    thread.join();
                }
                catch ( InterruptedException ex )
                {
                    // TODO: ???
                }
            }
        }
    }

    /**
     * This function runs in a separate thread and cleans up
     * any channels which are not used for a long time.
     */
    public void channel_reaper()
    {
        int lasttime = RequestIDAllocator.get_request_id();
        ServerChannel [] chans = new ServerChannel[ 0 ];
        while ( !m_shutdown )
        {
            try
            {
                Thread.sleep( m_channel_closing_time );
            }
            catch ( InterruptedException ex )
            {
                return;
            }
            if ( m_shutdown )
            {
                return;
            }
            synchronized ( m_sync_io )
            {
                if ( m_channels.isEmpty() )
                {
                    continue;
                }
                chans = ( ServerChannel[] ) m_channels.keySet().toArray( chans );
            }
            for ( int i = 0; i < chans.length && chans[ i ] != null; ++i )
            {
                if ( chans[ i ].channel_age() < lasttime )
                {
                    chans[ i ].soft_close( false );
                }
            }
            lasttime = RequestIDAllocator.get_request_id();
        }
    }

    /**
     * This function finds an adapter for the specified object id. If an adapter
     * for an object with the given object id cannot be found this returns null.
     * This varient of the find_adapter function is used by local bindings. It
     * ignores the state of the adapter manager and any single threading policies,
     * If the target adapter or one of it's ancestors is in the process of being
     * destroyed this will wait until the destruction is complete.
     *
     * @return The adapter specified in the object key.
     */
    public ObjectAdapter find_adapter( byte [] object_key )
    {
        return find_adapter( object_key, null );
    }

    /**
     * This function finds an adapter for the specified object id. If an adapter
     * for an object with the given object id cannot be found this returns null.
     * This varient of the find_adapter function is used by remote bindings. It
     * takes into account the state of any adapter managers when accessing user
     * code, throwing an AdapterHoldingException if they are in the holding state.
     *
     * @param object_key the object key from the target.
     * @param request the server request. This will be null for local invocations.
     * @return The adapter found.
     */
    private ObjectAdapter find_adapter( byte [] object_key, ServerRequest request )
    {
        // see if the object_key refers to a cacheable object.
        if ( !is_cacheable_object_key( object_key ) )
        {
            return m_default_adapter;
        }
        if ( !is_local_cacheable_object_key( object_key ) )
        {
            // cancel the request.
            if ( request != null )
            {
                request.server_cancel(
                        new org.omg.CORBA.OBJECT_NOT_EXIST(
                        org.omg.CORBA.OMGVMCID.value | 2,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ) );
            }
            return null;
        }

        int [] offsets = find_object_key_offsets( object_key );
        ProgressiveAIDKey aid_key = new ProgressiveAIDKey( object_key, offsets );

        while ( true )
        {
            // see if the adapter, or one of it's parents is in the cache.
            // since cache misses are relitivly fast to calculate, we try from most
            // specific to most general.
            AdapterValue ada_val = null;

            synchronized ( m_sync_state )
            {
                aid_key.useHash( offsets.length - 1 );
                ada_val = ( AdapterValue ) m_adapter_cache.get( aid_key );

                if ( ada_val != null )
                {
                    // direct target, return it without messing around
                    ada_val.incHits();
                    return ada_val.adapter();
                }

                for ( int i = offsets.length - 2; i >= 0 && ada_val == null; --i )
                {
                    aid_key.useHash( i );
                    ada_val = ( AdapterValue ) m_adapter_cache.get( aid_key );
                }
            }

            if ( ada_val == null )
            {
                // cancel the request.
                if ( request != null )
                {
                    request.server_cancel(
                            new org.omg.CORBA.OBJECT_NOT_EXIST(
                            org.omg.CORBA.OMGVMCID.value | 2,
                            org.omg.CORBA.CompletionStatus.COMPLETED_NO ) );
                }
                return null;
            }

            ObjectAdapter adapter = ada_val.adapter();

            // check for direct child
            if ( adapter.object_id( object_key ) != null )
            {
                ada_val.incHits();
                return adapter;
            }

            boolean is_st_thread = false;
            boolean made_st_thread = false;

            // user code must be entered to create an adapter.
            try
            {
                while ( true )
                {
                    // check the adapter's state.
                    AdapterManagerImpl manager = null;

                    if ( request != null )
                    {
                        if ( !is_st_thread && adapter.single_threaded() )
                        {
                            synchronized ( m_sync_queue )
                            {
                                if ( m_single_thread == Thread.currentThread() )
                                {
                                    is_st_thread = true;
                                }
                                else if ( m_single_thread == null )
                                {
                                    m_single_thread = Thread.currentThread();
                                    made_st_thread = true;
                                    is_st_thread = true;
                                }
                                else
                                {
                                    // queue request in st queue
                                    m_waiting_requests.addLast( request );
                                    ++m_holding_requests;
                                    return null;
                                }
                            }
                        }
                        manager = ( AdapterManagerImpl ) adapter.getAdapterManager();

                        if ( manager != null && !manager.begin_request( request ) )
                        {
                            return null;
                        }
                    }

                    // find a child adapter.
                    try
                    {
                        adapter = adapter.find_adapter( object_key );
                    }
                    catch ( AdapterDestroyedException ex )
                    {
                        // one of the adapters in the chain has been destroyed.
                        byte [] aid = ex.getAdapterID();

                        int [] aoff = find_object_key_offsets( aid );
                        int offset = aoff[ aoff.length - 1 ];

                        AdapterValue child_ada_val
                        = new AdapterValue( aid, offset, ex.getObjectAdapter() );

                        synchronized ( m_sync_state )
                        {
                            child_ada_val = ( AdapterValue ) m_adapter_cache.get( child_ada_val );
                            // child adapter already destroyed.
                            // run the whole method again from the start
                            if ( child_ada_val == null )
                            {
                                break;
                            }
                            if ( request != null )
                            {
                                // hold request and wait for adapter to be detroyed.
                                if ( child_ada_val.getDestroyRequests() == null )
                                {
                                    child_ada_val.setDestroyRequests( new MergeStack() );
                                }
                                child_ada_val.getDestroyRequests().addLast( request );

                                ++m_holding_requests;

                                return null;
                            }
                            else
                            {
                                // local request, just wait, don't hold.
                                child_ada_val.setWaitingForDestroy( true );
                                boolean interrupted = false;

                                while ( child_ada_val.getWaitingForDestroy() )
                                {
                                    try
                                    {
                                        m_sync_state.wait();
                                    }
                                    catch ( InterruptedException iex )
                                    {
                                        interrupted = true;
                                    }
                                }

                                if ( interrupted )
                                {
                                    Thread.currentThread().interrupt();
                                }
                                // we need to start the entire process from the beginning
                                // the current adapter may be the one which has been destroyed.
                                break;
                            }
                        }
                    }
                    finally
                    {
                        if ( manager != null )
                        {
                            manager.complete_request( request );
                        }
                    }

                    if ( adapter == null )
                    {
                        // cancel the request.
                        if ( request != null )
                        {
                            request.server_cancel(
                                    new org.omg.CORBA.OBJECT_NOT_EXIST(
                                    org.omg.CORBA.OMGVMCID.value | 2,
                                    org.omg.CORBA.CompletionStatus.COMPLETED_NO ) );
                        }
                        return null;
                    }

                    if ( adapter.object_id( object_key ) != null )
                    {
                        return adapter;
                    }
                }
            }
            finally
            {
                if ( made_st_thread )
                {
                    synchronized ( m_sync_queue )
                    {
                        m_single_thread = null;
                    }
                }
            }
        }
    }

    /**
     * Register an adapter. If the specified adapter is already
     * registered no change occours. This will occour automaticaly for
     * child adapters of some root adapter.
     */
    public void register_adapter( byte [] aid, ObjectAdapter adapter )
    {
        if ( aid.length == 0 )
        {
            // default adapter.
            if ( m_default_adapter == null )
            {
                m_default_adapter = adapter;
            }
            return;
        }

        if ( !is_local_cacheable_object_key( aid ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( 0, CompletionStatus.COMPLETED_NO );
        }
        int offset = ( find_object_key_offsets( aid ) ) [ 0 ];

        AdapterValue ada_val = new AdapterValue( aid, offset, adapter );

        AdapterManagerImpl manager = ( AdapterManagerImpl ) adapter.getAdapterManager();

        synchronized ( m_sync_state )
        {
            if ( m_adapter_cache.containsKey( ada_val ) )
            {
                return;
            }
            if ( manager != null )
            {
                manager.addManagedAdapter( adapter );
            }
            m_adapter_cache.put( ada_val, ada_val );
        }
    }

    /**
     * Unregister an adapter. If the specified adapter is not registered
     * no change occours. If an adapter throws an AdapterDestroyed exception
     * it must eventualy call this function to continue processing the held
     * requests.
     */
    public void unregister_adapter( byte [] aid )
    {
        if ( aid.length == 0 )
        {
            m_default_adapter = null;
            return;
        }

        if ( !is_local_cacheable_object_key( aid ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( 0, CompletionStatus.COMPLETED_NO );
        }
        int offset = ( find_object_key_offsets( aid ) ) [ 0 ];

        AdapterValue ada_val = new AdapterValue( aid, offset, null );

        synchronized ( m_sync_state )
        {
            ada_val = ( AdapterValue ) m_adapter_cache.remove( ada_val );

            if ( ada_val == null )
            {
                return;
            }
            if ( ada_val.getWaitingForDestroy() )
            {
                ada_val.setWaitingForDestroy( false );
                m_sync_state.notifyAll();
            }
        }

        // unregister the adapter from it's manager
        AdapterManagerImpl manager = ( AdapterManagerImpl ) ada_val.adapter().getAdapterManager();

        if ( manager != null )
        {
            manager.removeManagedAdapter( ada_val.adapter() );
        }
        // return any requests blocked waiting for destruction to the queue.
        if ( ada_val.getDestroyRequests() != null && !ada_val.getDestroyRequests().isEmpty() )
        {
            synchronized ( m_requests )
            {
                m_holding_requests -= ada_val.getDestroyRequests().size();
                m_requests.append( ada_val.getDestroyRequests() );
            }
            m_sync_queue.notifyAllPoolThread();
        }
    }

    /**
     * Create an adapter manager.
     */
    public AdapterManager create_adapter_manager()
    {
        return new AdapterManagerImpl();
    }

    /**
     * Begin dispatch sequence for incoming request. This will enqueue the
     * request and return immediatly.
     */
    public void enqueue_request( ServerRequest request )
    {
        // TODO: add per-client queue size checks.
        if ( m_shutdown )
        {
            request.server_cancel( createShutdownException() );
        }
        if ( m_max_queue_size == 0 || m_requests.size() < m_max_queue_size )
        {
            synchronized ( m_requests )
            {
                m_requests.addLast( request );
            }
            m_sync_queue.notifyPoolThread();
            return;
        }
        request.server_cancel( createQueueFullException() );
    }

    public boolean work_pending()
    {
        if ( m_shutdown )
        {
            return false;
        }
        if ( !m_running )
        {
            synchronized ( m_sync_io )
            {
                return !m_channels.isEmpty();
            }
        }
        return !m_requests.isEmpty();
    }

    /**
     * If this value is passed to the next_request function
     * the function will serve a single request, waiting if one is
     * not available on the queue, and return.
     */
    private static final int POOL_ID_WAIT = -1;

    /**
     * If this value is passed to the next_request function
     * the function will serve a single request, not waiting if one is
     * not available on the queue, and return.
     */
    private static final int POOL_ID_NO_WAIT = -2;

    /**
     * This is called from orb.perform_work, it serves a single request.
     *
     * @return true if a request was served, false if the queue is empty
     */
    public boolean serve_request( boolean wait )
    {
        if ( !m_running )
        {
            startup( false, false );
        }
        // main function will deal with cleaning up afterwards.
        return thread_pool_main( wait ? POOL_ID_WAIT : POOL_ID_NO_WAIT );
    }

    /**
     * Main function used by thread pool threads and also by the
     * serve_request function.
     */
    private boolean thread_pool_main( int pool_id )
    {
        // may retain st flag across loops.
        Thread currThread = Thread.currentThread();

        if ( pool_id < 0 )
        {
            synchronized ( m_sync_queue )
            {
                m_server_threads.add( currThread );
            }
        }
        boolean completedRequest = false;
        while ( true )
        {
            ServerRequest nextRequest = null;

            while ( true )
            {
                // thread pool shutdown 'function'
                if ( m_shutdown || ( completedRequest && pool_id < 0 ) )
                {
                    synchronized ( m_sync_queue )
                    {
                        // remove thread from pool
                        if ( pool_id >= 0 )
                        {
                            m_thread_pool_size--;
                            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
                            {
                                getLogger().debug( "(ServerManager) Thread pool shrunk to "
                                       + m_thread_pool_size );
                            }
                        }

                        // unmark this as the single thread thread.
                        if ( m_single_thread == currThread )
                        {
                            m_single_thread = null;
                        }
                        // notify shutdown that no more pool threads are active
                        m_server_threads.remove( currThread );
                    }
                    if ( m_shutdown && m_server_threads.isEmpty() )
                    {
                        m_sync_queue.notifyAllPoolThread();
                    }
                    return completedRequest;
                }

                completedRequest = false;

                // get the first available request (No DCL because of type boolean).
                boolean isRequestAvailable = ( !m_waiting_requests.isEmpty()
                        && ( m_single_thread == null || m_single_thread == currThread ) );
                if ( isRequestAvailable )
                {
                    synchronized ( m_sync_queue )
                    {
                        isRequestAvailable = ( !m_waiting_requests.isEmpty()
                              && ( m_single_thread == null || m_single_thread == currThread ) );
                        if ( isRequestAvailable )
                        {
                            if ( m_single_thread != currThread )
                            {
                                m_single_thread = currThread;
                            }
                            nextRequest = ( ServerRequest ) m_waiting_requests.removeFirst();
                            --m_holding_requests;
                        }
                    }
                }
                if ( !isRequestAvailable )
                {
                    // This is not DCL because no internal data of the thread instance is referenced
                    // the only thing that matters is whether the instance reference is null or not.
                    if ( m_single_thread == currThread )
                    {
                        // Unset the single_thread flag, as we're getting a request from internal.
                        synchronized ( m_sync_queue )
                        {
                            if ( m_single_thread == currThread )
                            {
                                m_single_thread = null;
                            }
                        }
                    }
                    // wait for a request to arrive
                    if ( m_requests.isEmpty() )
                    {
                        if ( pool_id != POOL_ID_NO_WAIT )
                        {
                            try
                            {
                                boolean timedOut = false;
                                long startedWait = System.currentTimeMillis();
                                m_sync_queue.wait( currThread );
                                timedOut = System.currentTimeMillis() - startedWait
                                      > DEFAULT_THREAD_POOL_WAIT_TIME - 1000;
                                // shutdown the current thread because of the timeout
                                if ( m_requests.isEmpty() && timedOut && m_thread_pool_size
                                      > m_min_thread_pool_size )
                                {
                                    synchronized ( m_sync_queue )
                                    {
                                        m_server_threads.remove( currThread );
                                        m_thread_pool_size--;
                                    }
                                    m_sync_queue.remove( currThread );
                                    return false;
                                }
                                continue;
                            }
                            catch ( InterruptedException ex )
                            {
                                Thread.currentThread().interrupt();
                                synchronized ( m_sync_queue )
                                {
                                    m_server_threads.remove( currThread );
                                    m_thread_pool_size--;
                                }
                                m_sync_queue.remove( currThread );
                                if ( m_shutdown && m_server_threads.isEmpty() )
                                {
                                    m_sync_queue.notifyAllPoolThread();
                                }
                                return false;
                            }
                        }
                    }
                    synchronized ( m_requests )
                    {
                        if ( !m_requests.isEmpty() )
                        {
                            nextRequest = ( ServerRequest ) m_requests.removeFirst();
                        }
                        else
                        {
                            continue;
                        }
                    }
                }
                break;
            }
            serve_request( nextRequest );
            completedRequest = true;
        }
    }

    /**
     * This function is responsible for all the queue management, it takes
     * an incoming request from the head of the queue and either dispatches
     * the request or puts it in the appropriate holding location.
     */
    protected void serve_request( ServerRequest request )
    {
        int state = request.begin_request();

        while ( true )
        {
            // find the adapter for the request.
            ObjectAdapter adapter;

            switch ( state )
            {

            case ServerRequest.STATE_FIND_ADAPTER:
                adapter = find_adapter( request.object_key(), request );

                if ( adapter == null
                      || ( state = request.adapter( adapter ) ) == ServerRequest.STATE_COMPLETE )
                {
                    return;
                }

                break;

            case ServerRequest.STATE_COMPLETE:
                return;

            default:
                adapter = request.adapter();
            }

            if ( state != ServerRequest.STATE_QUEUED )
            {
                throw Trace.signalIllegalCondition( getLogger(),
                        "state != ServerRequest.STATE_QUEUED" );
            }

            // deal with single threaded adapters.
            if ( adapter.single_threaded() )
            {
                synchronized ( m_sync_queue )
                {
                    if ( m_single_thread == null )
                    {
                        m_single_thread = Thread.currentThread();
                    }
                    else if ( m_single_thread != Thread.currentThread() )
                    {
                        // queue request in st queue
                        m_waiting_requests.addLast( request );
                        ++m_holding_requests;
                        return;
                    }
                }
            }

            // see if the manager rejects or queues the request.
            AdapterManagerImpl manager = ( AdapterManagerImpl ) adapter.getAdapterManager();

            if ( manager != null && !manager.begin_request( request ) )
            {
                return;
            }
            try
            {
                // this may throw an AdapterDestroyed exception
                request.dispatch();
            }
            catch ( AdapterDestroyedException ex )
            {
                // the adapter has been destroyed. If there is still an entry in
                // the adapter lookup table store the request with it for re-finding
                // when the target is eventualy destroyed,
                byte [] aid = ex.getAdapterID();

                if ( !( adapter == ex.getObjectAdapter() ) )
                {
                    org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                            "Adapters do not match." );
                }

                int [] aid_offsets = find_object_key_offsets( aid );

                AdapterValue ada_val = new AdapterValue( aid, aid_offsets[ 0 ], adapter );

                synchronized ( m_sync_state )
                {
                    ada_val = ( AdapterValue ) m_adapter_cache.get( ada_val );

                    if ( ada_val != null && ada_val.adapter() == adapter )
                    {
                        // hold request and wait for adapter to be detroyed.
                        if ( ada_val.getDestroyRequests() == null )
                        {
                            ada_val.setDestroyRequests( new MergeStack() );
                        }
                        ada_val.getDestroyRequests().addLast( request );

                        ++m_holding_requests;

                        return;
                    }
                }

                // the adapter has been destroyed try again. (the request may
                // result in a new adapter being created).
                continue;
            }
            finally
            {
                if ( manager != null )
                {
                    manager.complete_request( request );
                }
            }

            return;
        }
    }

    /**
     * Determine if this thread is a server thread.
     */
    private boolean is_server_thread()
    {
        synchronized ( m_sync_queue )
        {
            return m_server_threads.contains( Thread.currentThread() );
        }
    }

    /**
     * Extend the thread pool with one more thread. This will be called
     * from the enqueue request function when the thread pool must grow.
     * Lock on sync_state must be held.
     *
     * TODO: The thread pool is extended from time to time
     * but never reduced. All the once allocated resources will be
     * held indefinitely.
     */
    private void extend_thread_pool( int count )
    {
        if ( m_pool_threads == null || m_pool_threads.isDestroyed() )
        {
            if ( m_use_static_thread_group )
            {
                if ( s_static_pool_threads == null || s_static_pool_threads.isDestroyed() )
                {
                    s_static_pool_threads = new ThreadGroup( m_root_group, "Server Threads" );
                }
                m_pool_threads = s_static_pool_threads;
            }
            else
            {
                m_pool_threads = new ThreadGroup( m_root_group, "Server Threads" );
            }
        }

        m_thread_pool_size += count;

        for ( int i = 0; i < count; ++i )
        {
            m_next_tid++;
            PoolThread nextThread = new PoolThread( m_pool_threads, m_next_tid );
            m_server_threads.add( nextThread );
            nextThread.setDaemon( true );
            nextThread.start();
        }
        if ( getLogger().isDebugEnabled() && Trace.isHigh() )
        {
            getLogger().debug( "(ServerManager) Thread pool grew to " + m_thread_pool_size );
        }
    }

    /**
     */
    private boolean wait_for_startup()
    {
        if ( !m_running )
        {
            synchronized ( m_sync_state )
            {
                while ( !m_running )
                {
                    if ( m_shutdown )
                    {
                        return false;
                    }
                    try
                    {
                        m_sync_state.wait();
                    }
                    catch ( InterruptedException ex )
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Start the thread groups for server io operations and for channel
     * reaping. The thread group for channel reaping is only started
     * when a closing time for channel reaping greater 0 is specified.
     */
    public void startup( boolean block, boolean allowPool )
    {
        synchronized ( m_sync_state )
        {
            if ( m_shutdown )
            {
                return;
            }

            if ( !m_running )
            {
                m_running = true;

                m_allow_pool = allowPool;

                // start the channel reaper.
                if ( m_io_threads == null || m_io_threads.isDestroyed() )
                {
                    if ( m_use_static_thread_group )
                    {
                        if ( s_static_io_threads == null || s_static_io_threads.isDestroyed() )
                        {
                            s_static_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                        }
                        m_io_threads = s_static_io_threads;
                    }
                    else
                    {
                        m_io_threads = new ThreadGroup( m_root_group, "Server IO" );
                    }
                }

                if ( m_channel_closing_time > 0 )
                {
                    m_channel_reaper = new Thread( m_io_threads, new Runnable()
                                                 {
                                                     public void run()
                                                     {
                                                         channel_reaper();
                                                     }
                                                 }

                                                 , "Channel Reaper" );
                    m_channel_reaper.setDaemon( true );
                    m_channel_reaper.start();
                }

                if ( m_pool_thread_manager == null || ( !m_pool_thread_manager.isAlive()
                      || m_pool_thread_manager.isInterrupted() ) )
                {
                    if ( m_pool_thread_manager != null )
                    {
                        ( ( PoolThreadManager ) m_pool_thread_manager ).kill();
                    }

                    m_pool_thread_manager = new PoolThreadManager();
                    m_pool_thread_manager.setDaemon( true );
                    m_pool_thread_manager.start();
                }

                // start all the waiting threads.
                m_sync_state.notifyAll();
            }
            else if ( allowPool && !m_allow_pool )
            {
                synchronized ( m_sync_queue )
                {
                    // thread pool now allowed.
                    m_allow_pool = true;
                    extend_thread_pool( m_min_thread_pool_size );
                }
            }


            if ( block )
            {
                try
                {
                    while ( m_running )
                    {
                        m_sync_state.wait();
                    }
                }
                catch ( InterruptedException ex )
                {
                    // allow the server thread to be interrupted.
                }
            }
        }
    }

    /**
     * Spawns a thread to do the shutdown if wait_for_complete is false.
     */
    public void shutdown( boolean wait_for_complete )
    {
        if ( !wait_for_complete )
        {
            synchronized ( m_sync_state )
            {
                if ( m_shutdown )
                {
                    return;
                }

                Thread t = new Thread( m_root_group, new Runnable()
                  {
                      public void run()
                      {
                          shutdown( true );
                      }
                  }, "ORB shutdown thread" );
                t.setDaemon( true );
                t.start();

                while ( !m_io_complete )
                {
                    try
                    {
                        m_sync_state.wait();
                    }
                    catch ( InterruptedException ex )
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return;
        }

        // make sure we aren't calling this with a server thread.
        if ( is_server_thread() )
        {
            throw new BAD_INV_ORDER( OMGVMCID.value | 3, CompletionStatus.COMPLETED_MAYBE );
        }
        // stop listening for incoming connection requests + change to shutdown
        // state
        boolean interrupt = false;

        ServerProtocol [] protocols;

        synchronized ( m_sync_state )
        {
            synchronized ( m_sync_io )
            {
                if ( m_shutdown )
                {
                    return;
                }
                m_shutdown = true;

                protocols = new ServerProtocol[ m_protocol_to_worker.size() ];

                protocols = ( ServerProtocol [] )
                        m_protocol_to_worker.keySet().toArray( protocols );
            }

            // stop the channel reaper
            if ( m_channel_reaper != null )
            {
                m_channel_reaper.interrupt();
            }
        }

        // close all the protocols.
        for ( int i = 0; i < protocols.length; ++i )
        {
            if ( protocols[ i ] != null )
            {
                protocols[ i ].close();
            }
        }

        if ( m_pool_thread_manager != null )
        {
            ( ( PoolThreadManager ) m_pool_thread_manager ).kill();
        }

        synchronized ( m_sync_state )
        {
            // allow any threads blocked in shutdown(false) to return.
            m_io_complete = true;
            m_sync_state.notifyAll();
        }

        // notify server threads to wake up and notice death.
        m_sync_queue.notifyAllPoolThread();

        // wait for all server threads to complete.
        while ( !m_server_threads.isEmpty() )
        {
            try
            {
                Thread.sleep( 50 );
                // TODO: m_sync_queue.waitPoolThread();
            }
            catch ( InterruptedException ex )
            {
                interrupt = true;
            }
        }

        synchronized ( m_sync_state )
        {
            // cancel any queued or held requests.
            while ( !m_waiting_requests.isEmpty() )
            {
                ServerRequest request = ( ServerRequest ) m_waiting_requests.removeFirst();
                request.server_cancel( createShutdownException() );
                --m_holding_requests;
            }

            while ( !m_requests.isEmpty() )
            {
                ServerRequest request = ( ServerRequest ) m_requests.removeFirst();
                request.server_cancel( createShutdownException() );
            }

            // etherealize adapters and cancel adapter held requests
            Iterator itt = m_adapter_cache.values().iterator();

            while ( itt.hasNext() )
            {
                AdapterValue ada_val = ( AdapterValue ) itt.next();

                // discard any the held requests
                AdapterManagerImpl manager = ( AdapterManagerImpl )
                        ada_val.adapter().getAdapterManager();

                if ( manager != null && manager.m_state == State.HOLDING )
                {
                    if ( manager.m_hold_requests != null )
                    {
                        while ( !manager.m_hold_requests.isEmpty() )
                        {
                            ServerRequest request = ( ServerRequest )
                                    manager.m_hold_requests.removeFirst();
                            request.server_cancel( createShutdownException() );
                            --m_holding_requests;
                        }
                    }
                    manager.m_state = State.INACTIVE;
                }

                // etherealize the adapter.
                ada_val.adapter().etherealize( true );
            }

            if ( !( m_holding_requests == 0 ) )
            {
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Holding requests not empty." );
            }
        }

        // wait for channel reaper to complete.
        if ( m_channel_reaper != null )
        {
            try
            {
                m_channel_reaper.join();
                m_channel_reaper = null;
            }
            catch ( InterruptedException ex )
            {
                interrupt = true;
            }
        }

        // close the server channels.
        ServerChannel [] channels;

        synchronized ( m_sync_io )
        {
            channels = new ServerChannel[ m_channels.size() ];
            channels = ( ServerChannel[] ) m_channels.keySet().toArray( channels );
        }

        for ( int i = 0; i < channels.length; ++i )
        {
            channels[ i ].soft_close( true );
        }
        // allow anything blocked in startup to complete
        synchronized ( m_sync_state )
        {
            m_running = false;
            m_sync_state.notifyAll();
        }

        // destroy thread groups.
        if ( !m_use_static_thread_group )
        {
            if ( m_io_threads != null )
            {
                m_io_threads.setDaemon( true );

                try
                {
                    m_io_threads.destroy();
                }
                catch ( IllegalThreadStateException ex )
                {
                    // TODO: ???
                }
            }

            if ( m_pool_threads != null )
            {
                m_pool_threads.setDaemon( true );

                try
                {
                    m_pool_threads.destroy();
                }
                catch ( IllegalThreadStateException ex )
                {
                    // TODO: ???
                }
            }
        }

        org.openorb.orb.config.ORBLoader loader =
                ( ( org.openorb.orb.core.ORB ) m_orb ).getLoader();

        if ( loader instanceof Disposable )
        {
            ( ( Disposable ) loader ).dispose();
        }
        if ( interrupt )
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This class provides an implementation for the AdapterManager
     * interface.
     */
    public class AdapterManagerImpl
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableServer.POAManager, AdapterManager
    {
        /**
         * This class can only be instantiated by a server manager.
         */
        AdapterManagerImpl()
        {
        }

        private Set m_managed_adapters = new HashSet();

        private State m_state = State.HOLDING;

        private int m_managed_active_requests = 0;

        private MergeStack m_hold_requests;

        private boolean m_etherealize;

        private int m_max_manager_held_requests_override = m_max_mgr_held_requests;

        /**
         * Set the maximum number of requests a manager accepts.
         */
        public void setMaxManagerHeldRequests( int max )
        {
            if ( max < 0 )
            {
                throw new IllegalArgumentException();
            }
            m_max_manager_held_requests_override = max;
        }

        /**
         * Return the current state of the state machine.
         */
        public synchronized org.omg.PortableServer.POAManagerPackage.State get_state()
        {
            return m_state;
        }

        public void deactivate( boolean etherealize, boolean wait_for_completion )
            throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
        {
            // test to ensure this thread is not a server thread.
            if ( wait_for_completion && is_server_thread() )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 6,
                        CompletionStatus.COMPLETED_NO );
            }
            synchronized ( this )
            {
                if ( m_state == State.INACTIVE )
                {
                    if ( m_managed_active_requests == 0 )
                    {
                        throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
                    }
                    if ( wait_for_completion )
                    {
                        wait_for_complete();
                    }
                    return;
                }

                m_state = State.INACTIVE;

                if ( !wait_for_completion && etherealize && m_managed_active_requests > 0 )
                {
                    m_etherealize = true;
                    etherealize = false;
                }

                if ( wait_for_completion && m_managed_active_requests == 0 )
                {
                    wait_for_completion = false;
                }
            }

            if ( m_hold_requests != null )
            {
                while ( !m_hold_requests.isEmpty() )
                {
                    ServerRequest request = ( ServerRequest ) m_hold_requests.removeFirst();
                    request.server_cancel( createInactiveException() );
                }

                m_holding_requests -= m_hold_requests.size();
            }

            if ( wait_for_completion || etherealize )
            {
                synchronized ( this )
                {
                    if ( wait_for_completion )
                    {
                        wait_for_complete();
                    }
                    if ( etherealize )
                    {
                        complete_deactivate();
                    }
                }
            }
        }

        /**
         * Etherealize all object adapters that are managed by this manager.
         */
        private void complete_deactivate()
        {
            ObjectAdapter [] adapters = new ObjectAdapter[ m_managed_adapters.size() ];
            m_managed_adapters.toArray( adapters );

            for ( int i = 0; i < adapters.length; ++i )
            {
                adapters[ i ].etherealize( true );
            }
        }

        private void wait_for_complete()
        {
            try
            {
                while ( m_managed_active_requests > 0 && m_state != State.ACTIVE )
                {
                    wait();
                }
            }
            catch ( InterruptedException ex )
            {
                // we aren't in a server thread, just return.
                return;
            }
        }

        public void discard_requests( boolean wait_for_completion )
            throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
        {
            // test to ensure this thread is not a server thread.
            if ( wait_for_completion && is_server_thread() )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 6,
                        CompletionStatus.COMPLETED_NO );
            }
            MergeStack hold = null;

            synchronized ( this )
            {
                switch ( m_state.value() )
                {
                    case State._INACTIVE:
                        throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();

                    case State._HOLDING:
                        if ( m_hold_requests != null && !m_hold_requests.isEmpty() )
                        {
                            hold = m_hold_requests;
                            m_hold_requests = null;
                        }
                        break;

                    default:
                    // just continue
                    break;
                }

                m_state = State.DISCARDING;

                if ( hold == null || hold.isEmpty() )
                {
                    if ( wait_for_completion )
                    {
                        wait_for_complete();
                    }
                    return;
                }
            }

            // discard all the previously held requests
            while ( !hold.isEmpty() )
            {
                ServerRequest request = ( ServerRequest ) hold.removeFirst();
                request.server_cancel( createDiscardException() );
                --m_holding_requests;
            }

            if ( wait_for_completion )
            {
                synchronized ( this )
                {
                    wait_for_complete();
                }
            }
        }

        /**
         * Put the AdapterManager into the HOLDING state.
         * @param wait_for_completion If this flag is true the
         * operation will not return until the POAs associated with
         * this manager have completed any pending requests and
         * also entered the HOLDING state.
         */
        public void hold_requests( boolean wait_for_completion )
            throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
        {
            // test to ensure this thread is not a server thread.
            if ( wait_for_completion && is_server_thread() )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER(
                    org.omg.CORBA.OMGVMCID.value | 6,
                    CompletionStatus.COMPLETED_NO );
            }
            synchronized ( this )
            {
                if ( m_state == State.INACTIVE )
                {
                    throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
                }
                // change the state
                m_state = State.HOLDING;

                // wait for the POAs
                if ( wait_for_completion )
                {
                    wait_for_complete();
                }
            }
        }

        public void activate()
            throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
        {
            MergeStack hold = null;

            synchronized ( this )
            {
                switch ( m_state.value() )
                {
                    case State._INACTIVE:
                        throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();

                    case State._ACTIVE:
                        return;

                    case State._HOLDING:
                        hold = m_hold_requests;
                        m_hold_requests = null;
                        break;

                    default:
                        // just continue
                        break;
                }

                m_state = State.ACTIVE;
                notifyAll();
            }

            if ( hold != null && !hold.isEmpty() )
            {
                synchronized ( m_requests )
                {
                    m_holding_requests -= hold.size();
                    m_requests.append( hold );
                }
                m_sync_queue.notifyAllPoolThread();
            }
        }

        boolean begin_request( ServerRequest request )
        {
            org.omg.CORBA.SystemException respond_ex = null;

            synchronized ( this )
            {
                switch ( m_state.value() )
                {
                case State._ACTIVE:
                    ++( m_managed_active_requests );
                    return true;

                case State._HOLDING:
                    if ( m_hold_requests == null || m_hold_requests.size()
                          < ( ( m_max_manager_held_requests_override < m_max_mgr_held_requests )
                          ? m_max_manager_held_requests_override : m_max_mgr_held_requests ) )
                    {

                        if ( m_hold_requests == null )
                        {
                            m_hold_requests = new MergeStack();
                        }
                        m_hold_requests.addLast( request );

                        ++m_holding_requests;

                        return false;
                    }

                    // fallthrough. If max holding limit is reached treat like
                    // the discarding state.

                case State._DISCARDING:
                    respond_ex = createDiscardException();
                    break;

                case State._INACTIVE:
                    respond_ex = createInactiveException();
                    break;

                 default:
                     // just continue
                     break;
                }
            }

            request.server_cancel( respond_ex );
            return false;
        }

        void complete_request( ServerRequest request )
        {
            synchronized ( this )
            {
                if ( --m_managed_active_requests == 0 )
                {
                    if ( m_etherealize )
                    {
                        complete_deactivate();
                    }
                    notifyAll();
                }
            }
        }

        synchronized void addManagedAdapter( ObjectAdapter adapter )
        {
            m_managed_adapters.add( adapter );
        }

        synchronized void removeManagedAdapter( ObjectAdapter adapter )
        {
            m_managed_adapters.remove( adapter );
        }
    }

    // the two classes for doing the hash table lookup.
    // note: given progressive keys p and q, and adapter values k and m
    // then p == q & p == k & q == m does not imply k == m,
    // the prefixes must match for equating adapter values, whereas the prefixes
    // are ignored in all other comparisons.

    private static class ProgressiveAIDKey
    {
        public ProgressiveAIDKey( byte [] aid, int [] offsets )
        {
            m_aid = aid;
            m_offsets = offsets;
            m_hashes = new int[ m_offsets.length ];

            // generate the hashes.
            m_hash = 0;

            for ( int i = 0; i < m_hashes.length; ++i )
            {
                int s = m_offsets[ i ];
                int e = ( ( i + 1 ) < m_hashes.length ) ? m_offsets[ i + 1 ] : m_aid.length;

                for ( int j = s; j < e; ++j )
                {
                    m_hash = 31 * m_hash + ( m_aid[ j ] & 0xFF );
                }
                m_hashes[ i ] = m_hash;
            }

            m_hash_to = m_offsets.length - 1;
        }

        private byte [] m_aid;
        private int [] m_offsets;
        private int [] m_hashes;
        private int m_hash;
        private int m_hash_to;

        public boolean is_cacheable_object_key()
        {
            return m_offsets[ 0 ] != 0;
        }

        public int hashCode()
        {
            return m_hash;
        }

        public int hashTo()
        {
            return m_hash_to;
        }

        public int[] offsets()
        {
            return m_offsets;
        }

        public byte[] aid()
        {
            return m_aid;
        }

        public int useHash( int to )
        {
            m_hash_to = to;
            m_hash = ( to == -1 ) ? 0 : m_hashes[ to ];
            return m_hash;
        }

        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            if ( obj instanceof AdapterValue )
            {
                AdapterValue o2 = ( AdapterValue ) obj;

                // a whole key will not match a cacheable key.
                if ( ( o2.offset() == 0 ) != ( m_offsets[ 0 ] == 0 ) )
                {
                    return false;
                }
                // compare the hash codes
                if ( o2.hashCode() != m_hash )
                {
                    return false;
                }
                // compare the object keys
                int i = ( m_hash_to == m_offsets.length - 1 )
                      ? m_aid.length : m_offsets[ m_hash_to + 1 ];

                if ( i - m_offsets[ 0 ] != o2.aid().length - o2.offset() )
                {
                    return false;
                }
                int j = o2.aid().length;

                for ( --i, --j; j >= o2.offset(); --i, --j )
                {
                    if ( m_aid[ i ] != o2.aid()[ j ] )
                    {
                        return false;
                    }
                }
                return true;
            }

            // this is not a usual case
            if ( obj instanceof ProgressiveAIDKey )
            {
                ProgressiveAIDKey o2 = ( ProgressiveAIDKey ) obj;

                // a whole key will not match a cacheable key.
                if ( ( o2.m_offsets[ 0 ] == 0 ) != ( m_offsets[ 0 ] == 0 ) )
                {
                    return false;
                }
                // compare the hash codes
                if ( o2.m_hash_to != m_hash_to )
                {
                    return false;
                }
                for ( int i = m_hash_to; i >= 0; --i )
                {
                    if ( o2.m_hashes[ i ] != m_hashes[ i ]
                          || o2.m_offsets[ i ] - o2.m_offsets[ 0 ]
                          != m_offsets[ i ] - m_offsets[ 0 ] )
                    {
                        return false;
                    }
                }
                // compare the object keys
                int i = ( m_hash_to == m_offsets.length - 1 )
                      ? m_aid.length : m_offsets[ m_hash_to + 1 ];

                int j = ( o2.m_hash_to == o2.m_offsets.length - 1 ) ? o2.m_aid.length
                      : o2.m_offsets[ o2.m_hash_to + 1 ];

                for ( --i, --j; i >= m_offsets[ 0 ]; --i, --j )
                {
                    if ( m_aid[ i ] != o2.m_aid[ j ] )
                    {
                        return false;
                    }
                }

                return true;
            }
            return false;
        }
    }

    private static class AdapterValue
    {
        private byte [] m_aid;
        private int m_hash;
        private int m_offset;
        private ObjectAdapter m_adapter;

        private int m_priority;
        private int m_hits = 0;

        private MergeStack m_destroy_requests;

        private boolean m_waiting_for_destroy = false;

        public AdapterValue( byte [] aid, int offset, ObjectAdapter adapter )
        {
            m_aid = aid;
            m_offset = offset;

            m_hash = 0;

            for ( int i = offset; i < m_aid.length; ++i )
            {
                m_hash = 31 * m_hash + ( m_aid[ i ] & 0xFF );
            }
            m_adapter = adapter;

            if ( m_adapter != null )
            {
                m_priority = m_adapter.cache_priority();
            }
            else
            {
                m_priority = -1;
            }
        }

        public boolean is_cacheable_object_key()
        {
            return m_offset != 0;
        }

        public int hashCode()
        {
            return m_hash;
        }

        public int offset()
        {
            return m_offset;
        }

        public byte[] aid()
        {
            return m_aid;
        }

        public int hits()
        {
            return m_hits;
        }

        public void incHits()
        {
            m_hits++;
        }

        public ObjectAdapter adapter()
        {
            return m_adapter;
        }

        public MergeStack getDestroyRequests()
        {
            return m_destroy_requests;
        }

        public void setDestroyRequests( MergeStack destroy_requests )
        {
            m_destroy_requests = destroy_requests;
        }

        public boolean getWaitingForDestroy()
        {
            return m_waiting_for_destroy;
        }

        public void setWaitingForDestroy( boolean waiting_for_destroy )
        {
            m_waiting_for_destroy = waiting_for_destroy;
        }

        public boolean equals( Object obj )
        {
            if ( obj instanceof AdapterValue )
            {
                AdapterValue o2 = ( AdapterValue ) obj;
                if ( o2.m_hash != m_hash || o2.m_offset != m_offset
                      || o2.m_aid.length != m_aid.length )
                {
                    return false;
                }
                for ( int i = m_aid.length - 1; i >= 0; --i )
                {
                    if ( m_aid[ i ] != o2.m_aid[ i ] )
                    {
                        return false;
                    }
                }
                return true;
            }

            if ( obj instanceof ProgressiveAIDKey )
            {
                // compare the hash codes
                ProgressiveAIDKey o2 = ( ProgressiveAIDKey ) obj;

                if ( o2.hashCode() != m_hash )
                {
                    return false;
                }
                // a whole key will not match a progressive key.
                if ( ( m_offset == 0 ) != ( o2.offsets()[ 0 ] == 0 ) )
                {
                    return false;
                }
                // compare the object keys
                int i = ( o2.hashTo() == o2.offsets().length - 1 ) ? o2.aid().length
                      : o2.offsets()[ o2.hashTo() + 1 ];

                if ( i - o2.offsets()[ 0 ] != m_aid.length - m_offset )
                {
                    return false;
                }
                int j = m_aid.length;

                for ( --i, --j; j >= m_offset; --i, --j )
                {
                    if ( o2.aid()[ i ] != m_aid[ j ] )
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Returns an array of strings describing the active server channels.
     *
     * @return An array of strings.
     */
    protected String [] getChannelsInfo()
    {
        final java.util.Vector vec = new java.util.Vector();
        synchronized ( m_sync_io )
        {
            // thread-synchronize while accessing channels map
            final Iterator iterObj = m_channels.keySet().iterator();
            Object obj;
            while ( iterObj.hasNext() )
            {
                // for each channel entry
                if ( ( obj = iterObj.next() ) instanceof ServerChannel )
                {
                    //channel object fetched OK; add description to list
                    vec.add( ( ( ServerChannel ) obj ).toString() );
                }
            }
        }
        // convert vector of strings to an array and return
        return ( String [] ) ( vec.toArray( new String[ vec.size() ] ) );
    }

    /**
     * This is a special sync queue used for thread pools.
     * A LIFO strategie is used to give older thread's a chance to die.
     * It significantly reduces the number of context switches for
     * the pool threads.
     */
    private class SyncQueue
    {
        private final LinkedList m_waiting_threads = new LinkedList();
        private int m_wait_interval_size = 0;
        SyncQueue()
        {
        }

        final void wait( Object nextPoolThread )
            throws InterruptedException
        {
            synchronized ( nextPoolThread )
            {
                synchronized ( m_waiting_threads )
                {
                    if ( !m_requests.isEmpty() )
                    {
                        return;
                    }
                    if ( !m_waiting_threads.contains( nextPoolThread ) )
                    {
                        m_waiting_threads.addFirst( nextPoolThread );
                        m_wait_interval_size++;
                    }
                }
                nextPoolThread.wait( DEFAULT_THREAD_POOL_WAIT_TIME );
            }
        }

        final void notifyPoolThread()
        {
            Object nextPoolThread = null;
            synchronized ( m_waiting_threads )
            {
                if ( !m_waiting_threads.isEmpty() )
                {
                    nextPoolThread = m_waiting_threads.removeFirst();
                }
            }
            if ( nextPoolThread != null )
            {
                synchronized ( nextPoolThread )
                {
                    nextPoolThread.notify();
                }
            }
        }

        final void notifyAllPoolThread()
        {
            LinkedList clone = null;
            synchronized ( m_waiting_threads )
            {
                // clone and clear the list
                clone = ( LinkedList ) m_waiting_threads.clone();
                m_waiting_threads.clear();
            }

            // send notify for all waiting pool threads
            Object nextPoolThread = null;
            Iterator it = clone.iterator();
            while ( it.hasNext() )
            {
                nextPoolThread = it.next();
                if ( nextPoolThread != null )
                {
                    synchronized ( nextPoolThread )
                    {
                        nextPoolThread.notify();
                    }
                }
            }
        }

        final void remove( Object poolThread )
        {
            synchronized ( m_waiting_threads )
            {
                m_waiting_threads.remove( poolThread );
            }
        }

        final int size()
        {
            return m_waiting_threads.size();
        }

        final int waitIntervalSize()
        {
            synchronized ( m_waiting_threads )
            {
                int result = m_wait_interval_size;
                // reset the wait interval
                m_wait_interval_size = size();
                return result;
            }
        }
    }

    /**
     * Implementation of a pool thread.
     */
    private final class PoolThread
        extends Thread
    {
        private PoolThread( ThreadGroup group, int id )
        {
            super( group, "Pool thread #" + id );
            m_id = id;
        }

        private int m_id;

        public void run()
        {
            thread_pool_main( m_id );
        }
    }

    /**
     * This class is reponsible for starting additional threads.
     */
    private class PoolThreadManager
        extends Thread
    {
        private boolean m_killed = false;

        /**
         * Constructor.
         */
        public PoolThreadManager()
        {
           super( "PoolThreadManager" );
        }

        public void run()
        {
            boolean loop = true;
            while ( loop )
            {
                try
                {
                    // check if a new thread will be needed .
                    Thread.sleep( 1000 );
                    // check min. max, size of the thread pool. if all active threads were
                    // running during this interval then we need additional thread's
                    if ( m_thread_pool_size < m_min_thread_pool_size )
                    {
                        synchronized ( m_sync_queue )
                        {
                            extend_thread_pool( m_min_thread_pool_size - m_thread_pool_size );
                        }
                    }
                    else if ( m_thread_pool_size < m_max_thread_pool_size
                          && m_sync_queue.waitIntervalSize() == 0 )
                    {
                        synchronized ( m_sync_queue )
                        {
                            extend_thread_pool( 1 );
                        }
                    }
                }
                catch ( Exception ex )
                {
                    if ( m_killed )
                    {
                        loop = false;
                    }
                }
            }
        }

        void kill()
        {
            m_killed = true;
            interrupt();
        }
    }
}

