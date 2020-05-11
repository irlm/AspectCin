/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.openorb.orb.policy.ProfilePriorityPolicy;
import org.openorb.orb.policy.ProfilePriorityPolicyHelper;
import org.openorb.orb.policy.DISABLE_PROFILE;
import org.openorb.orb.policy.PROFILE_PRIORITY_POLICY_ID;

import org.openorb.util.NumberCache;

/**
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/05/13 04:09:25 $
 */
public class ClientManagerImpl
    implements ClientManager
{
    // m_channels unused two minutes are paused
    private static final int DEFAULT_PAUSE_TIME = 2 * 60 * 1000;

    private static ThreadGroup s_static_work_threads;

    private org.omg.CORBA.ORB m_orb;

    private Map m_protocols = new HashMap();
    private Map m_channels = new HashMap();

    private ServerManager m_server_manager;

    private Object m_sync_state = new Object();

    private boolean m_shutdown = false;

    private int m_pause;

    private ThreadGroup m_root_group;
    private boolean m_use_static_thread_group = false;
    private ThreadGroup m_work_threads;
    private Thread m_channel_reaper;


    public ClientManagerImpl( org.omg.CORBA.ORB orb )
    {
        this( orb, null );
    }

    public ClientManagerImpl( org.omg.CORBA.ORB orb, ServerManager serverManager )
    {
        m_orb = orb;
        m_server_manager = serverManager;
        m_root_group = Thread.currentThread().getThreadGroup();

        m_pause = ( ( org.openorb.orb.core.ORB ) orb ).getLoader().getIntProperty(
                "openorb.client.reapPauseDelay", DEFAULT_PAUSE_TIME );
        m_use_static_thread_group =
                ( ( org.openorb.orb.core.ORB ) orb ).getLoader().getBooleanProperty(
                "openorb.useStaticThreadGroup", false );
    }

    /**
     * Return an orb reference.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Get a reference to the server manager. This is used to find the adapter
     * for local requests.
     */
    public ServerManager getServerManager()
    {
        return m_server_manager;
    }

    /**
     * Create bindings for a given IOR.
     */
    public ClientBinding [] create_bindings( org.omg.CORBA.Object obj, org.omg.IOP.IOR ior )
    {
        int profiles = ior.profiles.length;

        org.omg.GIOP.IORAddressingInfo info = new org.omg.GIOP.IORAddressingInfo( 0, ior );

        // lookup the profile priority policy
        org.openorb.orb.core.Delegate delegate =
                ( org.openorb.orb.core.Delegate )
                ( ( org.omg.CORBA.portable.ObjectImpl ) obj )._get_delegate();
        ProfilePriorityPolicy policy =
                ProfilePriorityPolicyHelper.narrow( delegate.get_client_policy( obj,
                PROFILE_PRIORITY_POLICY_ID.value ) );

        ArrayList allbindings = new ArrayList( 8 );

        for ( int i = 0; i < profiles; ++i )
        {
            int profile_tag = ior.profiles[ i ].tag;
            byte priority = ( policy == null ) ? 8 : policy.find_priority( profile_tag );

            ClientProtocol protocol;
            // lookup the protocol
            if ( priority != DISABLE_PROFILE.value
                  && ( protocol = ( ClientProtocol )
                  m_protocols.get( NumberCache.getInteger( profile_tag ) ) ) != null )
            {
                info.selected_profile_index = i;

                // create the addresses
                Address [] addresses = protocol.createAddresses( info );

                // create the bindings
                for ( int j = 0; j < addresses.length; ++j )
                {
                    ClientBinding binding = protocol.createBinding( addresses[ j ] );

                    binding.setPriority( ( binding.getPriority()
                         & ~ClientBinding.MASK_PROFILE_PRIORITY )
                         | ( ( ( int ) priority ) << 12
                         & ClientBinding.MASK_PROFILE_PRIORITY ) );
                    allbindings.add( binding );
                }
            }
        }

        if ( allbindings.isEmpty() )
        {
            throw new org.omg.CORBA.INV_OBJREF();
        }
        ClientBinding [] ret = new ClientBinding[ allbindings.size() ];

        ret = ( ClientBinding [] ) allbindings.toArray( ret );

        return ret;
    }

    /**
     * Register a client protocol.
     */
    public void register_protocol( int profile_tag,
                                   ClientProtocol protocol )
    {
        m_protocols.put( NumberCache.getInteger( profile_tag ), protocol );
    }

    /**
     * Register a channel to join the work queue. This is called when the channel
     * enters the open state.
     */
    public boolean register_channel( ClientChannel channel )
    {
        synchronized ( m_sync_state )
        {
            if ( m_shutdown )
            {
                return false;
            }
            synchronized ( m_channels )
            {
                if ( m_channels.containsKey( channel ) )
                {
                    return true;
                }
                if ( m_work_threads == null || m_work_threads.isDestroyed() )
                {
                    if ( m_use_static_thread_group )
                    {
                        if ( s_static_work_threads == null || s_static_work_threads.isDestroyed() )
                        {
                            s_static_work_threads = new ThreadGroup( m_root_group, "Client IO" );
                        }
                        m_work_threads = s_static_work_threads;
                    }
                    else
                    {
                        m_work_threads = new ThreadGroup( m_root_group, "Client IO" );
                    }
                }

                if ( m_channel_reaper == null && m_pause > 0 )
                {
                    // create the channel reaper.
                    m_channel_reaper = new Thread( m_work_threads, new Runnable()
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

                Thread thread = new Thread( m_work_threads,
                        new ChannelRecvRunnable( channel ),
                        "Receive Worker for " + channel.toString() );
                thread.setDaemon( true );
                thread.start();

                m_channels.put( channel, thread );

                return true;
            }
        }
    }

    /**
     * Called when a channel enters the closed or paused state. Returns once
     * all channel threads have completed their work cycles.
     */
    public void unregister_channel( ClientChannel channel )
    {
        Thread thread;

        synchronized ( m_channels )
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

    private class ChannelRecvRunnable implements Runnable
    {
        private ClientChannel m_chan;

        public ChannelRecvRunnable( ClientChannel chan )
        {
            m_chan = chan;
        }

        public void run()
        {
            // this returns when the thread is interrupted or the channel is closed.
            m_chan.run_recv();
        }
    }

    public void shutdown( boolean wait_for_complete, boolean kill_requests )
    {
        boolean shut = false;
        Thread chanReap = null;

        synchronized ( m_sync_state )
        {
            if ( !m_shutdown )
            {
                m_shutdown = true;
                shut = true;

                if ( m_channel_reaper != null )
                {
                    m_channel_reaper.interrupt();
                    chanReap = m_channel_reaper;
                }
            }
        }

        if ( chanReap != null )
        {
            try
            {
                chanReap.join();
            }
            catch ( InterruptedException ex )
            {
                // TODO: ???
            }
        }

        if ( shut )
        {
            ClientChannel [] cchans;

            synchronized ( m_channels )
            {
                cchans = new ClientChannel[ m_channels.size() ];
                m_channels.keySet().toArray( cchans );
            }

            for ( int i = 0; i < cchans.length; ++i )
            {
                cchans[ i ].close( kill_requests,
                        new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 4,
                        org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE ) );
            }
        }

        if ( !m_use_static_thread_group )
        {
            if ( m_work_threads != null )
            {
                m_work_threads.setDaemon( true );

                try
                {
                    m_work_threads.destroy();
                }
                catch ( IllegalThreadStateException ex )
                {
                    // TODO: ???
                }
            }
        }
    }

    private void channel_reaper()
    {
        int lasttime = RequestIDAllocator.get_request_id();

        ClientChannel [] chans = null;

        while ( !m_shutdown )
        {
            try
            {
                Thread.sleep( m_pause );
            }
            catch ( InterruptedException ex )
            {
                break;
            }

            synchronized ( m_channels )
            {
                if ( m_channels.isEmpty() )
                {
                    m_channel_reaper = null;
                    break;
                }

                if ( chans == null || chans.length < m_channels.size() )
                {
                    chans = new ClientChannel[ m_channels.size() ];
                }
                chans = ( ClientChannel[] ) m_channels.keySet().toArray( chans );
            }

            for ( int i = 0; i < chans.length && chans[ i ] != null; ++i )
            {
                if ( chans[ i ].state() == ClientChannel.STATE_CONNECTED
                      && chans[ i ].channel_age() < lasttime )
                {
                    chans[ i ].pause();
                }
                chans[ i ] = null;
            }

            lasttime = RequestIDAllocator.get_request_id();
        }
    }
}

