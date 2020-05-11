/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import java.util.Map;
import java.util.HashMap;

import org.omg.CORBA.LocalObject;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;

import org.openorb.util.NumberCache;

/**
 * This class implements the Codec Factory. It is used to return a
 * Codec according to a encoding format.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/05/13 04:09:27 $
 */
public class CodecFactoryManagerImpl
    extends LocalObject
    implements CodecFactory, CodecFactoryManager
{
    private Map m_all_codecs = new HashMap();

    /**
     * Operation create_codec creates a codec from an encoding.
     *
     * @param enc The encoding to create a codec from.
     * @return The codec created from the specified encoding.
     * @throws UnknownEncoding When the factory is null.
     */
    public Codec create_codec( Encoding enc )
        throws UnknownEncoding
    {
        Integer key = NumberCache.getInteger( ( int ) enc.format << 16
              | ( int ) enc.major_version << 8
              | ( int ) enc.minor_version );
        CodecFactory factory = ( CodecFactory ) m_all_codecs.get( key );

        if ( factory == null )
        {
            throw new UnknownEncoding( "Factory for '" + key + "' could not be found!" );
        }
        return factory.create_codec( enc );
    }

    /**
     * Registration point for codec factories.
     *
     * @param enc The encopding to register a factory for.
     * @param factory The factory for the specified encoding.
     */
    public void register_codec_factory( Encoding enc,
                                        CodecFactory factory )
    {
        Integer key = NumberCache.getInteger( ( int ) enc.format << 16
              | ( int ) enc.major_version << 8
              | ( int ) enc.minor_version );

        if ( factory == null )
        {
            m_all_codecs.remove( key );
        }
        else
        {
            m_all_codecs.put( key, factory );
        }
    }
}

