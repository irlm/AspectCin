/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INV_OBJREF;
import org.omg.IIOP.Version;
import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IIOP.ListenPoint;
import org.omg.IIOP.ListenPointHelper;

import org.omg.IOP.IOR;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentSeqHelper;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_ORB_TYPE;
import org.omg.IOP.TAG_POLICIES;

import org.openorb.orb.net.AbstractAddress;
import org.openorb.orb.net.Address;

import org.openorb.orb.util.Trace;
import org.openorb.util.ExceptionTool;

/**
 * IIOP request target.
 *
 * @author Chris Wood
 * @version $Revision: 1.12 $ $Date: 2004/02/19 07:21:31 $
 */
public final class IIOPAddress
    extends AbstractAddress
    implements Cloneable
{
    private InetAddress m_host;
    private String m_host_name;
    private int m_port;
    private Version m_version;
    private int m_orb_type = 0;
    private int m_hash = -1;
    private String m_protocol = "iiop";

    private CodeSetComponentInfo m_codeset_component_info = null;

    // This member is not set anywhere !!!
    private IIOPAddress [] m_alternates = null;

    /**
     * Mask of bits set for the version priority.
     */
    public static final short MASK_PRI_VERSION = 0x000F;

    /**
     * Version priorities
     */
    public static final short [] PRI_VERSION = { 0x000F, 0x000E, 0x000D };

    /**
     * Flag set if address is an alternate address.
     */
    public static final short PRI_FLAG_ALTERNATE = 0x0800;

    /**
     * Flag set if address is from profile not containing primary profile
     * component.
     */
    public static final short PRI_FLAG_NONPRIMARY = 0x0400;

    /**
     * Bits available to be altered for differing security levels.
     */
    public static final short MASK_PRI_SECURE = 0x00F0;

    /**
     * Standard unsecured address.
     */
    public static final short PRI_UNSECURE = 0x00F0;

    /**
     * Default constructor.
     */
    private IIOPAddress()
    {
    }

    /**
     * Set the host name of this IIOP address.
     */
    public void setHost( InetAddress host )
    {
        if ( host != null )
        {
            m_host = host;
            m_host_name = host.getHostName();
            m_hash = -1;
        }
    }

    /**
     * Set the port number of this IIOP address.
     */
    public void setPort( int port )
    {
        if ( port != 0 )
        {
            m_port = port;
            m_hash = -1;
        }
    }

    /**
     * Set the protocol version of this IIOP address.
     */
    public void setProtocol( String protocol )
    {
        m_protocol = protocol;
        m_hash = -1;
    }

    /**
     * Clone an IIOP address object.
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch ( CloneNotSupportedException ex )
        {
            Trace.signalIllegalCondition( null, "Clone operation not supported (" + ex + ")." );
        }
        // never reached
        return null;
    }

    /**
     * The protocol string as would appear in the corbaloc address.
     */
    public String getProtocol()
    {
        return m_protocol;
    }

    /**
     * Return the hostname of the target.
     */
    public String get_hostname()
    {
        return m_host_name;
    }

    /**
     * Return the host as an InetAddress
     */
    public InetAddress get_host()
    {
        return m_host;
    }

    /**
     * Return the port number of the target.
     */
    public int get_port()
    {
        return m_port;
    }

    /**
     * Get the IIOP version of the target.
     */
    public Version get_version()
    {
        return m_version;
    }

    /**
     * Return the four char orb type.
     */
    public int get_orb_type()
    {
        return m_orb_type;
    }

    /**
     * Return the codeset data.
     */
    public CodeSetComponentInfo getCodesetComponentInfo()
    {
        return m_codeset_component_info;
    }

    /**
     * Return all the alternative addresses created from this profile.
     */
    public Address [] get_alternates()
    {
        return m_alternates;
    }

    /**
     * A string which could be used to contact the endpoint in a corbaloc
     * style address.
     */
    public String getEndpointString()
    {
        return m_protocol + ":" + m_version.major + "." + m_version.minor
              + "@" + m_host_name + ":" + m_port;
    }

    /**
     * Human readable string describing the endpoint.
     */
    public String getEndpointDescription()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( getEndpointString() );

        if ( m_host == null )
        {
            sb.append( " host unknown" );
        }
        else
        {
            boolean comma = false;

            if ( getPriority( MASK_PRI_SECURE ) != PRI_UNSECURE )
            {
                sb.append( " flags: secure(" );
                sb.append( getPriority( MASK_PRI_SECURE ) >> 8 );
                sb.append( ")" );
                comma = true;
            }

            if ( getPriority( PRI_FLAG_NONPRIMARY ) == 0 )
            {
                if ( comma )
                {
                    sb.append( ", " );
                }
                else
                {
                    sb.append( " flags: " );
                }
                sb.append( "primary" );

                comma = true;
            }

            if ( getPriority( PRI_FLAG_ALTERNATE ) != 0 )
            {
                if ( comma )
                {
                    sb.append( ", " );
                }
                else
                {
                    sb.append( " flags: " );
                }
                sb.append( "alternate" );
            }
        }

        return sb.toString();
    }

    /**
     * Compute a hash code for this class.
     * @return The has code for this class (static).
     */
    public int hashCode()
    {
        if ( m_hash == -1 )
        {
            m_hash = super.hashCode() ^ m_host.hashCode() ^ m_port ^ m_protocol.hashCode();
        }
        return m_hash;
    }

    /**
     * Checks whether two instances are equal.
     * The comparison is based on ports, hostnames, and protocol version.
     * @param obj The object to compare with.
     * @return True if the addresses are equal, false otherwise.
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof IIOPAddress ) )
        {
            return false;
        }
        IIOPAddress ia2 = ( IIOPAddress ) obj;

        if ( m_port != ia2.m_port
              || !m_host_name.equals( ia2.m_host_name )
              || !m_protocol.equals( ia2.m_protocol )
              || m_version.major != ia2.m_version.major
              || m_version.minor != ia2.m_version.minor )
        {
            return false;
        }
        return super.equals( obj );
    }

    /**
     * Create a set of IIOP addresses from a profile in an IOR.
     */
    static IIOPAddress [] get_addresses( org.omg.IOP.Codec codec,
            IOR ior, int selected_profile, Logger logger )
    {
        if ( ior.profiles[ selected_profile ].tag != TAG_INTERNET_IOP.value )
        {
            throw new INV_OBJREF( "Invalid tag for object reference",
                    IIOPMinorCodes.INV_OBJREF_BAD_TAG, CompletionStatus.COMPLETED_NO );
        }
        IIOPAddress addr = new IIOPAddress();

        addr.set_ior( ior, selected_profile );

        byte [] pd = ior.profiles[ selected_profile ].profile_data;

        if ( pd[ 1 ] != 1 )
        {
            return new IIOPAddress[ 0 ];
        }
        TaggedComponent [] procomp = null;

        try
        {
            switch ( pd[ 2 ] )
            {

            case 0:
                {
                    // IIOP 1.0
                    ProfileBody_1_0 body = ProfileBody_1_0Helper.extract(
                            codec.decode_value( pd, ProfileBody_1_0Helper.type() ) );
                    addr.m_version = body.iiop_version;
                    addr.m_host_name = body.host;
                    addr.m_port = ( body.port & 0xFFFF );
                    addr.set_oid( body.object_key );
                    procomp = new TaggedComponent[ 0 ];
                }

                break;

            case 1:

            case 2:

            default:
                {
                    ProfileBody_1_1 body = ProfileBody_1_1Helper.extract(
                            codec.decode_value( pd, ProfileBody_1_1Helper.type() ) );
                    addr.m_version = body.iiop_version;
                    addr.m_host_name = body.host;
                    addr.m_port = ( body.port & 0xFFFF );
                    addr.set_oid( body.object_key );
                    procomp = body.components;

                    if ( addr.m_version.minor > 2 )
                    {
                        addr.m_version.minor = 2;
                    }
                }

                break;
            }

            try
            {
                addr.m_host = InetAddress.getByName( addr.m_host_name );
            }
            catch ( UnknownHostException ex )
            {
                addr.m_host = null;
                // set hash since trying to set it will fail.
                addr.m_hash = addr.m_host_name.hashCode() ^ addr.m_port;
            }

            // scan for multiple components
            int mcc = 0;

            for ( int i = 0; i < ior.profiles.length; ++i )
            {
                if ( ior.profiles[ i ].tag == TAG_MULTIPLE_COMPONENTS.value )
                {
                    ++mcc;
                }
            }
            TaggedComponent [] cpts;

            if ( mcc == 0 )
            {
                cpts = procomp;
            }
            else
            {
                TaggedComponent [][] mcs;

                if ( procomp != null )
                {
                    mcs = new TaggedComponent[ mcc + 1 ][];
                    mcs[ mcc ] = procomp;
                }
                else
                {
                    mcs = new TaggedComponent[ mcc ][];
                }

                for ( int i = 0; i < ior.profiles.length && mcc > 0; ++i )
                {
                    if ( ior.profiles[ i ].tag == TAG_MULTIPLE_COMPONENTS.value )
                    {
                        mcs[ --mcc ] = TaggedComponentSeqHelper.extract(
                                codec.decode_value( ior.profiles[ i ].profile_data,
                                TaggedComponentSeqHelper.type() ) );
                    }
                }
                mcc = 0;

                for ( int i = 0; i < mcs.length; ++i )
                {
                    mcc += mcs[ i ].length;
                }
                cpts = new TaggedComponent[ mcc ];

                mcc = 0;

                for ( int i = 0; i < mcs.length; ++i )
                {
                    System.arraycopy( mcs[ i ], 0, cpts, mcc, mcs[ i ].length );
                    mcc += mcs[ i ].length;
                }
            }

            addr.set_components( cpts, procomp.length );
            short priority = ( short )
                    ( PRI_FLAG_NONPRIMARY | PRI_UNSECURE | PRI_VERSION[ pd[ 2 ] ] );

            ArrayList alts = new ArrayList();

            for ( int i = 0; i < cpts.length; ++i )
            {
                switch ( cpts[ i ].tag )
                {

                case TAG_ALTERNATE_IIOP_ADDRESS.value:
                    {
                        ListenPoint lp = ListenPointHelper.extract(
                                codec.decode_value( cpts[ i ].component_data,
                                ListenPointHelper.type() ) );

                        alts.add( lp );
                        addr.set_component_data( i,
                                new TagAlternateIIOPAddressData( cpts[ i ].tag,
                                lp.host, lp.port ) );
                    }

                    break;

                case TAG_ORB_TYPE.value:
                    addr.m_orb_type = codec.decode_value(
                            cpts[ i ].component_data, org.omg.CORBA.ORB.init().get_primitive_tc(
                            org.omg.CORBA.TCKind.tk_long ) ).extract_long();

                    addr.set_component_data( i, new TagOrbTypeData(
                            cpts[ i ].tag, addr.m_orb_type ) );
                    break;

                case org.omg.IOP.TAG_CODE_SETS.value:
                    {
                        addr.m_codeset_component_info =
                                CodeSetComponentInfoHelper.extract(
                                codec.decode_value( cpts[ i ].component_data,
                                CodeSetComponentInfoHelper.type() ) );

                        addr.set_component_data( i, new TagCodeSetsData(
                                cpts[i].tag, addr.m_codeset_component_info ) );
                    }
                    break;

                case 28:   /* org.omg.FT.TAG_FT_PRIMARY */
                    // this just contains a boolean.
                    {
                        final boolean primaryProfile =
                                cpts[ i ].component_data[ 1 ] != 0;

                        if ( primaryProfile )
                        {
                            priority = ( short ) ( priority & ~PRI_FLAG_NONPRIMARY );
                        }
                        addr.set_component_data( i,
                                new TagFTPrimaryData( cpts[ i ].tag, primaryProfile ) );
                    }
                    break;

                case 29:   /* org.omg.FT.TAG_FT_HEARTBEAT_ENABLED */
                    // this just contains a boolean.
                    {
                        final boolean heartbeatEnabled =
                                cpts[ i ].component_data[ 1 ] != 0;

                        addr.set_component_data( i,
                                new TagFTHeartbeatEnabledData( cpts[ i ].tag,
                                heartbeatEnabled ) );
                    }

                    break;

                case TAG_POLICIES.value:
                    // TODO: Implement this !!!!
                    throw new org.omg.CORBA.NO_IMPLEMENT();

                default:
                    // try to find a handler at the TaggedComponent handler registry
                    AbstractTagData data = TaggedComponentHandlerRegistry.handleComponent(
                          cpts[ i ], codec );
                    if ( data != null )
                    {
                        addr.set_component_data( i, data );
                    }
                }
            }

            addr.setPriority( priority );

            // if anything else is done do it before doing this
            IIOPAddress [] addrs = new IIOPAddress[ 1 + alts.size() ];
            addrs[ 0 ] = addr;

            for ( int i = 0; i < alts.size(); ++i )
            {
                ListenPoint lp = ( ListenPoint ) alts.get( i );
                addrs[ i + 1 ] = ( IIOPAddress ) addr.clone();
                addrs[ i + 1 ].m_host_name = lp.host;

                try
                {
                    addrs[ i + 1 ].m_host = InetAddress.getByName( lp.host );
                }
                catch ( UnknownHostException ex )
                {
                    addrs[ i + 1 ].m_host = null;
                    // set hash since trying to set it will fail.
                    addrs[ i + 1 ].m_hash = addrs[ i + 1 ].m_host_name.hashCode()
                          ^ addrs[ i + 1 ].m_port;
                }

                addrs[ i + 1 ].m_port = lp.port & 0xFFFF;
                addrs[ i + 1 ].setPriority( PRI_FLAG_ALTERNATE, PRI_FLAG_ALTERNATE );
            }

            return addrs;
        }
        catch ( final org.omg.IOP.CodecPackage.FormatMismatch ex )
        {
            logger.error( "Profile data cannot be parsed.", ex );

            throw ExceptionTool.initCause( new INV_OBJREF(
                    "Profile data cannot be parsed (" + ex + ")",
                    IIOPMinorCodes.INV_OBJREF_BAD_PROFILE,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        catch ( final org.omg.IOP.CodecPackage.TypeMismatch ex )
        {
            logger.error( "Profile data cannot be parsed.", ex );

            throw ExceptionTool.initCause( new INV_OBJREF(
                    "Profile data cannot be parsed (" + ex + ")",
                    IIOPMinorCodes.INV_OBJREF_BAD_PROFILE,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
    }

    /**
     * The data class for org.omg.FT.TAG_FT_HEARTBEAT_ENABLED
     */
    private static final class TagFTHeartbeatEnabledData
        extends AbstractTagData
    {
        private final boolean m_heartbeatEnabled;

        private TagFTHeartbeatEnabledData( final int componentId,
                final boolean heartbeatEnabled )
        {
            super( componentId );
            m_heartbeatEnabled = heartbeatEnabled;
        }

        protected StringBuffer createMessage()
        {
            final StringBuffer buf = super.createMessage();
            buf.append( " (TAG_FT_HEARTBEAT_ENABLED)\n        Heartbeat " );
            buf.append( m_heartbeatEnabled ? "enabled" : "disabled" );
            return buf;
        }

    }

    /**
     * The data class for org.omg.FT.TAG_FT_PRIMARY
     */
    private static final class TagFTPrimaryData
        extends AbstractTagData
    {
        private final boolean m_primaryProfile;

        private TagFTPrimaryData( final int componentId,
                final boolean primaryProfile )
        {
            super( componentId );
            m_primaryProfile = primaryProfile;
        }

        protected StringBuffer createMessage()
        {
            final StringBuffer buf = super.createMessage();
            buf.append( " (TAG_FT_PRIMARY)\n"
                  + ( m_primaryProfile ? "        Primary " : "        NonPrimary " ) );
            buf.append( "profile" );
            return buf;
        }
    }

    /**
     * The data class for org.omg.IOP.TAG_CODE_SETS
     */
    private static final class TagCodeSetsData
        extends AbstractTagData
    {
        private final CodeSetComponentInfo m_info;

        private TagCodeSetsData( final int componentId,
                final CodeSetComponentInfo info )
        {
            super( componentId );
            m_info = info;
        }

        protected StringBuffer createMessage()
        {
            final StringBuffer buf = super.createMessage();
            buf.append( " (TAG_CODE_SETS)\n        For char data: " );

            int cs = m_info.ForCharData.native_code_set;

            for ( int j = 0; j <= m_info.ForCharData.conversion_code_sets.length; ++j )
            {
                buf.append( "\n            " );
                buf.append( ( cs == m_info.ForCharData.native_code_set ) ? "SNCS" : "SCCS" );
                buf.append( ":" );
                appendIntAsHex( buf, cs );
                buf.append( " \'" );
                buf.append( CodeSetDatabase.getNameFromId( cs ) );
                buf.append( "\' \'" );
                buf.append( CodeSetDatabase.getDescriptionFromId( cs ) );
                buf.append( "\'" );

                if ( j < m_info.ForCharData.conversion_code_sets.length )
                {
                    cs = m_info.ForCharData.conversion_code_sets[ j ];
                }
            }

            buf.append( "\n        For wchar data: " );
            cs = m_info.ForWcharData.native_code_set;

            for ( int j = 0; j <= m_info.ForWcharData.conversion_code_sets.length; ++j )
            {
                buf.append( "\n            " );
                buf.append( ( cs == m_info.ForWcharData.native_code_set ) ? "SNCS" : "SCCS" );
                buf.append( ":" );
                appendIntAsHex( buf, cs );
                buf.append( " \'" );
                buf.append( CodeSetDatabase.getNameFromId( cs ) );
                buf.append( "\' \'" );
                buf.append( CodeSetDatabase.getDescriptionFromId( cs ) );
                buf.append( "\'" );

                if ( j < m_info.ForWcharData.conversion_code_sets.length )
                {
                    cs = m_info.ForWcharData.conversion_code_sets[ j ];
                }
            }

            return buf;
        }
    }

    /**
     * The data class for TAG_ORB_TYPE
     */
    private static final class TagOrbTypeData
        extends AbstractTagData
    {
        private final int m_orbType;

        private TagOrbTypeData( final int componentId, final int orbType )
        {
            super( componentId );
            m_orbType = orbType;
        }

        protected StringBuffer createMessage()
        {
            final StringBuffer buf = super.createMessage();
            buf.append( " (TAG_ORB_TYPE)\n        Type:  " );
            appendIntAsHex( buf, m_orbType );
            return buf;
        }
    }

    /**
     * The data class for TAG_ALTERNATE_IIOP_ADDRESS
     */
    private static final class TagAlternateIIOPAddressData
        extends AbstractTagData
    {
        private final String m_host;
        private final short m_port;

        private TagAlternateIIOPAddressData( final int componentId,
                final String host, final short port )
        {
            super( componentId );
            m_host = host;
            m_port = port;
        }

        protected StringBuffer createMessage()
        {
            final StringBuffer buf = super.createMessage();
            buf.append( " (TAG_ALTERNATE_IIOP_ADDRESS)\n" );
            buf.append( "        Host:  " + m_host + "\n" );
            buf.append( "        Port:  " + ( m_port & 0xFFFF ) );
            return buf;
        }
    }
}

