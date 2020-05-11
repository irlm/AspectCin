/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.util.HashMap;

import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.openorb.util.NumberCache;

/**
 * This is the registry for TaggedComponent handlers.
 * New handlers can be registered by using the registerHandler()
 * method.
 *
 * @author Michael Rumpf
 */
public final class TaggedComponentHandlerRegistry
{
    private static final HashMap COMPONENT_HANDLER = new HashMap();

    private TaggedComponentHandlerRegistry()
    {
    }

    /**
     * Register a new handler.
     *
     * @param tch A TaggedComponent handler.
     */
    public static void registerHandler( TaggedComponentHandler tch )
    {
        int[] tags = tch.getTags();
        for ( int i = 0; i < tags.length; i++ )
        {
            COMPONENT_HANDLER.put( NumberCache.getInteger( tags[ i ] ), tch );
        }
    }

    /**
     * Look for a handlers for the specified component and
     * invoke the handle() method on the handler.
     *
     * @param component The TaggedComponent to search a handler for.
     * @param codec The Codec used for decoding the TaggedComponent.
     * @return An instance of AbstractTagData when a handler was found,
     * or null otherwise.
     */
    public static AbstractTagData handleComponent( TaggedComponent component, Codec codec )
    {
        AbstractTagData result = null;
        TaggedComponentHandler handler = ( TaggedComponentHandler )
              COMPONENT_HANDLER.get( NumberCache.getInteger( component.tag ) );
        if ( handler != null )
        {
            result = handler.handle( component, codec );
        }
        return result;
    }
}

