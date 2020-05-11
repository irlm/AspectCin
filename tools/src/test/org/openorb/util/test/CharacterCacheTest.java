/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.test;

import junit.framework.TestCase;
import org.openorb.util.CharacterCache;

/**
 * Tests for the <code>CharacterCache</code>.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/06/23 07:15:21 $
 */
public class CharacterCacheTest extends TestCase
{
    public CharacterCacheTest( final String name )
    {
        super( name );
    }

    public void test0x0000to0x00FF()
    {
        for ( char c = 0; c <= 0xFF; c++ )
        {
            assertEquals( c, CharacterCache.getCharacter( c ).charValue() );
        }

    }
}