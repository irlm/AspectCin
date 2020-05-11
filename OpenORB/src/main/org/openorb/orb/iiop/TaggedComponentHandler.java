/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

/**
 * This is the base interface for TaggedComponent handlers.
 * It can be used to dynamically parse TaggedComponents.
 *
 * @author Michael Rumpf
 */
public interface TaggedComponentHandler
{
    /**
     * Return the tag numbers this handler is repsonsible for.
     *
     * @return Array of tag numbers.
     */
    int[] getTags();

    /**
     * Dump the component.
     *
     * @param component The component to handle.
     * @param codec The codec for decode the data.
     */
    AbstractTagData handle( TaggedComponent component, Codec codec );
}

