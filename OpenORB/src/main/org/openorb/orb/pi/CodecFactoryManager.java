/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.IOP.CodecFactory;
import org.omg.IOP.Encoding;

/**
 * Interface for codec factory registration.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface CodecFactoryManager
{
    /**
     * Registration point for codec factories.
     *
     * @param enc The encoding to register a factory for.
     * @param factory The factory to register for the specified encoding.
     */
    void register_codec_factory( Encoding enc, CodecFactory factory );
}

