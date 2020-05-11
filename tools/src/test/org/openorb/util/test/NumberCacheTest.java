/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.test;

import junit.framework.TestCase;
import org.openorb.util.NumberCache;

/**
 * Tests for the <code>NumberCache</code>.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/06/23 07:13:19 $
 */
public class NumberCacheTest extends TestCase
{
    public NumberCacheTest( final String name )
    {
        super( name );
    }

    public void testByte()
    {
        for ( int i = 0x00, base = 0x00; i <= 0xFF; i++ )
        {
            final byte n = ( byte ) ( base + i );
            assertEquals( n, NumberCache.getByte( n ).byteValue() );
        }
    }

    public void testShort()
    {
        testShortSequence( 0x0000, 0xFF );
        testShortSequence( 0x7F00, 0xFF );
        testShortSequence( 0x8000, 0xFF );
        testShortSequence( 0xFF00, 0xFF );
    }

    public void testInteger()
    {
        testIntegerSequence( 0x00000000, 0xFF );
        testIntegerSequence( 0x7FFFFF00, 0xFF );
        testIntegerSequence( 0x80000000, 0xFF );
        testIntegerSequence( 0xFFFFFF00, 0xFF );
    }


    public void testLong()
    {
        testLongSequence( 0x0000000000000000L, 0xFF );
        testLongSequence( 0x7FFFFF0000000000L, 0xFF );
        testLongSequence( 0x8000000000000000L, 0xFF );
        testLongSequence( 0xFFFFFF0000000000L, 0xFF );
    }

    public void testFloat()
    {
        testFloatSequence( 0x00000000, 0xFF );
        testFloatSequence( 0x80000000, 0xFF );
        testFloatSequence( 0x7FF00000, 0xFF );
        testFloatSequence( 0xFFF00000, 0xFF );
    }

    public void testDouble()
    {
        testDoubleSequence( 0x0000000000000000L, 0xFF );
        testDoubleSequence( 0x8000000000000000L, 0xFF );
        testDoubleSequence( 0x7FF0000000000000L, 0xFF );
        testDoubleSequence( 0xFFF0000000000000L, 0xFF );
    }

    private void testShortSequence( final int base, final int maxOffset )
    {
        for ( int i = 0x00; i <= maxOffset; i++ )
        {
            final short n = ( short ) ( base + i );
            assertEquals( n, NumberCache.getShort( n ).shortValue() );
        }
    }

    private void testIntegerSequence( final int base, final int maxOffset )
    {
        for ( int i = 0x00; i <= maxOffset; i++ )
        {
            final int n = base + i;
            assertEquals( n, NumberCache.getInteger( n ).intValue() );
        }
    }

    private void testLongSequence( final long base, final long maxOffset )
    {
        for ( long i = 0x00; i <= maxOffset; i++ )
        {
            final long n = base + i;
            assertEquals( n, NumberCache.getLong( n ).longValue() );
        }
    }

    private void testFloatSequence( final int base, final int maxOffset )
    {
        for ( int i = 0x00; i <= maxOffset; i++ )
        {
            final float n = Float.intBitsToFloat( base + i );
            final float cached = NumberCache.getFloat( n ).floatValue();
            assertEquals( Float.floatToRawIntBits( n ),
                    Float.floatToRawIntBits( cached ) );
        }
    }

    private void testDoubleSequence( final long base, final long maxOffset )
    {
        for ( long i = 0x00; i <= maxOffset; i++ )
        {
            final double n = Double.longBitsToDouble( base + i );
            final double cached = NumberCache.getDouble( n ).doubleValue();
            assertEquals( Double.doubleToRawLongBits( n ),
                    Double.doubleToRawLongBits( cached ) );
        }
    }
}