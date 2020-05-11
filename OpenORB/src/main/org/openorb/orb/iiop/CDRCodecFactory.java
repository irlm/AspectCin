/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:48 $
 */
public class CDRCodecFactory
    extends org.omg.CORBA.LocalObject
    implements org.omg.IOP.CodecFactory
{
    private org.omg.CORBA.ORB m_orb;
    private CDRCodec m_codec_0;
    private CDRCodec m_codec_1;
    private CDRCodec m_codec_2;

    public CDRCodecFactory( org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
    }

    public org.omg.IOP.Codec create_codec( org.omg.IOP.Encoding enc )
        throws org.omg.IOP.CodecFactoryPackage.UnknownEncoding
    {
        if ( enc.format != org.omg.IOP.ENCODING_CDR_ENCAPS.value
              || enc.major_version != 1 )
        {
            throw new org.omg.IOP.CodecFactoryPackage.UnknownEncoding();
        }
        switch ( enc.minor_version )
        {

        case 0:

            if ( m_codec_0 == null )
            {
                m_codec_0 = new CDRCodec( m_orb, enc );
            }
            return m_codec_0;

        case 1:
            if ( m_codec_1 == null )
            {
                m_codec_1 = new CDRCodec( m_orb, enc );
            }
            return m_codec_1;

        case 2:
            if ( m_codec_2 == null )
            {
                m_codec_2 = new CDRCodec( m_orb, enc );
            }
            return m_codec_2;

        default:
            throw new org.omg.IOP.CodecFactoryPackage.UnknownEncoding();
        }
    }
}

