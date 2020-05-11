/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.omg.CORBA.INTERNAL;

import org.omg.IOP.TaggedProfile;

import org.openorb.orb.net.ServerProtocol;

import org.openorb.util.ExceptionTool;

/**
 * The methods of this class comes from the ServerManager class in the
 * net package.
 * The comment in front of those methods was: "These methods should be moved
 * somewhere else because they are independent of the server side".
 * Because the construct_ior is used from the adapters only the IORUtil class
 * is located in the adapter package.
 *
 * @author Michael Rumpf
 */
public final class IORUtil
{
    /**
     * Utility class, do not instantiate.
     */
    private IORUtil()
    {
    }

    /**
     * Used in creating multicomponent profiles.
     */
    private static org.omg.IOP.Codec s_component_codec;

    /**
     * Construct an IOR.
     */
    public static org.omg.IOP.IOR construct_ior( String type_id,
                                    byte[] object_key,
                                    org.openorb.orb.pi.ComponentSet component_set,
                                    Object[] protProfIDs,
                                    org.omg.CORBA.ORB orb )
    {
        org.omg.IOP.IOR ret = new org.omg.IOP.IOR();

        ret.type_id = type_id;

        int u = 0;

        org.omg.IOP.TaggedComponent [] multi = component_set.getComponents(
                org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value );

        if ( multi != null && multi.length > 0 )
        {
            ret.profiles = new org.omg.IOP.TaggedProfile[ protProfIDs.length / 2 + 1 ];
        }
        else
        {
            multi = null;
            ret.profiles = new org.omg.IOP.TaggedProfile[ protProfIDs.length / 2 ];
        }

        // add ordinary profiles.
        for ( int i = 0; i < protProfIDs.length; i += 2 )
        {
            ServerProtocol protocol = ( ServerProtocol ) protProfIDs[ i ];
            int profile_tag = ( ( Integer ) protProfIDs[ i + 1 ] ).intValue();

            org.omg.IOP.TaggedProfile profile =
                protocol.create_profile( profile_tag, component_set, object_key );

            if ( profile != null )
            {
                ret.profiles[ u++ ] = profile;
            }
        }

        // if no active profiles we don't have a valid reference
        if ( u == 0 )
        {
            return null;
        }
        // add multicomponent profile.
        if ( multi != null )
        {
            org.omg.CORBA.Any any = orb.create_any();
            org.omg.IOP.TaggedComponentSeqHelper.insert( any, multi );

            org.omg.IOP.Codec cmptCodec = getComponentCodec( orb );
            byte [] buf;

            try
            {
                buf = cmptCodec.encode_value( any );
            }
            catch ( final org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex )
            {
                throw ExceptionTool.initCause( new INTERNAL(
                        "Invalid encoding type" ), ex );
            }

            if ( buf != null )
            {
                ret.profiles[ u++ ] = new org.omg.IOP.TaggedProfile(
                        org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value, buf );
            }
        }

        if ( u < ret.profiles.length )
        {
            org.omg.IOP.TaggedProfile [] tmp = new org.omg.IOP.TaggedProfile[ u ];
            System.arraycopy( ret.profiles, 0, tmp, 0, u );
            ret.profiles = tmp;
        }

        return ret;
    }

    private static org.omg.IOP.Codec getComponentCodec( org.omg.CORBA.ORB orb )
    {
        if ( s_component_codec == null )
        {
            try
            {
                org.omg.IOP.CodecFactory factory = ( org.omg.IOP.CodecFactory )
                    orb.resolve_initial_references( "CodecFactory" );

                s_component_codec = factory.create_codec(
                    new org.omg.IOP.Encoding(
                        org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1,
                        ( byte ) 0 ) );
            }
            catch ( final org.omg.CORBA.UserException ex )
            {
                throw ExceptionTool.initCause( new INTERNAL(
                      "Either resolve of CodecFactory failed or create_codec "
                      + "returned with an exception (" + ex + ")" ), ex );
            }
        }
        return s_component_codec;
    }

    /**
     * Constructs a new IOR which is a merge of all the profiles in each of
     * the targets.
     */
    public static org.omg.IOP.IOR merge_iors( String type_id, org.omg.IOP.IOR [] iors )
    {
        Set pms = new HashSet();
        for ( int i = 0; i < iors.length; ++i )
        {
            for ( int j = 0; j < iors[ i ].profiles.length; ++j )
            {
                pms.add( new ProfileMember( iors[ i ].profiles[ j ] ) );
            }
        }
        org.omg.IOP.IOR ret = new org.omg.IOP.IOR( type_id,
                new org.omg.IOP.TaggedProfile[ pms.size() ] );
        int i = 0;
        Iterator itt = pms.iterator();
        while ( itt.hasNext() )
        {
            ret.profiles[ i++ ] = ( ( ProfileMember ) itt.next() ).profile();
        }
        return ret;
    }

    /**
     * Single use class, just hashes profiles.
     */
    private static class ProfileMember
    {
        private int m_hash;
        private TaggedProfile m_profile;

        public ProfileMember( org.omg.IOP.TaggedProfile profile )
        {
            m_profile = profile;
            m_hash = m_profile.tag;

            for ( int i = 0; i < m_profile.profile_data.length; ++i )
            {
                m_hash = 31 * m_hash + m_profile.profile_data[ i ];
            }
        }

        public int hashCode()
        {
            return m_hash;
        }

        public TaggedProfile profile()
        {
            return m_profile;
        }

        public boolean equals( Object obj )
        {
            if ( !( obj instanceof ProfileMember ) )
            {
                return false;
            }
            ProfileMember pm2 = ( ProfileMember ) obj;

            if ( m_hash != pm2.hashCode() || m_profile.tag != pm2.profile().tag
                    || m_profile.profile_data.length != pm2.m_profile.profile_data.length )
            {
                return false;
            }
            for ( int i = 0; i < m_profile.profile_data.length; ++i )
            {
                if ( m_profile.profile_data[ i ] != pm2.m_profile.profile_data[ i ] )
                {
                    return false;
                }
            }
            return true;
        }
    }
}

