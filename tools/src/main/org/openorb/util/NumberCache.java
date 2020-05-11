/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

/**
 * A utility class for obtaining <code>Number</code> instances.
 * The following system properties are used to configure the cache:
 * <table>
 *  <tr>
 *   <th>Key</th>
 *   <th>Value</th>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.disable</code></td>
 *   <td>
 *     Set this to <code>false</code> to disable all caching.
 *     Note that this also disables <code>Character</code> caching.
 *   </td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.short.min</code></td>
 *   <td>The min (inclusive) <code>Short</code> value to cache (default -128).</td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.short.max</code></td>
 *   <td>The max (inclusive) <code>Short</code> value to cache (default 127).</td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.integer.min</code></td>
 *   <td>The min (inclusive) <code>Integer</code> value to cache (default -128).</td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.integer.max</code></td>
 *   <td>The max (inclusive) <code>Integer</code> value to cache (default 127).</td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.long.min</code></td>
 *   <td>The min (inclusive) <code>Long</code> value to cache (default -128).</td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.long.max</code></td>
 *   <td>The max (inclusive) <code>Long</code> value to cache (default 127).</td>
 *  </tr>
 * </table>
 *
 * <p>
 * Note that all <code>Byte</code> values are cached unless caching is disabled.
 *
 * @author Richard G Clark
 * @version $Revision: 1.5 $ $Date: 2004/06/23 07:13:19 $
 * @see CharacterCache
 */
public final class NumberCache
{
    private static final String DISABLE_CACHE_KEY = "openorb.cache.disable";
    private static final boolean DISABLE_CACHE
            = "true".equals( System.getProperty( DISABLE_CACHE_KEY ) );

    private static final String VERBOSE_KEY = "openorb.cache.verbose";
    private static final boolean VERBOSE
            = "true".equals( System.getProperty( VERBOSE_KEY ) );

    private NumberCache()
    {
    }

    /**
     * Gets the <code>Byte</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Byte</code> instance
     */
    public static Byte getByte( final byte value )
    {
        if ( DISABLE_CACHE )
        {
            return new Byte( value );
        }

        return ByteCache.getByte( value );
    }

    /**
     * Gets the <code>Short</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Short</code> instance
     */
    public static Short getShort( final short value )
    {
        if ( DISABLE_CACHE )
        {
            return new Short( value );
        }

        return ShortCache.getShort( value );
    }

    /**
     * Gets the <code>Integer</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Integer</code> instance
     */
    public static Integer getInteger( final int value )
    {
        if ( DISABLE_CACHE )
        {
            return new Integer( value );
        }

        return IntegerCache.getInteger( value );
    }

    /**
     * Gets the <code>Long</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Long</code> instance
     */
    public static Long getLong( final long value )
    {
        if ( DISABLE_CACHE )
        {
            return new Long( value );
        }

        return LongCache.getLong( value );
    }

    /**
     * Gets the <code>Float</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Float</code> instance
     */
    public static Float getFloat( final float value )
    {
        return new Float( value );
    }

    /**
     * Gets the <code>Double</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Double</code> instance
     */
    public static Double getDouble( final double value )
    {
        return new Double( value );
    }

    private static final class ByteCache
    {
        private static final Byte[] CACHE;

        static
        {
            CACHE = new Byte[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
            for ( int i = 0; i < CACHE.length; i++ )
            {
                CACHE[i] = new Byte( ( byte ) ( i + Byte.MIN_VALUE ) );
            }
        }

        private ByteCache()
        {
        }

        static Byte getByte( final byte value )
        {
            return CACHE[value - Byte.MIN_VALUE];
        }
    }

    private static final class ShortCache
    {
        private static final String CACHE_MIN_KEY = "openorb.cache.short.min";
        private static final String CACHE_MAX_KEY = "openorb.cache.short.max";
        private static final short CACHE_MIN_DEFAULT = -128;
        private static final short CACHE_MAX_DEFAULT = 127;
        private static final Short[] CACHE;
        private static final short CACHE_MIN;
        private static final short CACHE_MAX;

        static
        {
            CACHE_MIN = parseValue( CACHE_MIN_KEY, CACHE_MIN_DEFAULT );
            CACHE_MAX = parseValue( CACHE_MAX_KEY, CACHE_MAX_DEFAULT );

            CACHE = new Short[CACHE_MAX - CACHE_MIN + 1];
            for ( int i = 0; i < CACHE.length; i++ )
            {
                CACHE[i] = new Short( ( short ) ( i + CACHE_MIN ) );
            }
        }

        private ShortCache()
        {
        }

        private static short parseValue( final String key, final short defaultValue )
        {
            final String value = System.getProperty( key );

            if ( null == value )
            {
                return defaultValue;
            }

            try
            {
                return Short.parseShort( value );
            }
            catch ( final NumberFormatException e )
            {
                return defaultValue;
            }
        }

        static Short getShort( final short value )
        {
            if ( ( CACHE_MIN <= value ) && ( value <= CACHE_MIN ) )
            {
                return CACHE[value - CACHE_MIN];
            }
            return new Short( value );
        }
    }

    private static final class IntegerCache
    {

        private static final String CACHE_MIN_KEY = "openorb.cache.integer.min";
        private static final String CACHE_MAX_KEY = "openorb.cache.integer.max";
        private static final int CACHE_MIN_DEFAULT = -128;
        private static final int CACHE_MAX_DEFAULT = 127;
        private static final Integer[] CACHE;
        private static final int CACHE_MIN;
        private static final int CACHE_MAX;

        static
        {
            CACHE_MIN = parseValue( CACHE_MIN_KEY, CACHE_MIN_DEFAULT );
            CACHE_MAX = parseValue( CACHE_MAX_KEY, CACHE_MAX_DEFAULT );

            CACHE = new Integer[CACHE_MAX - CACHE_MIN + 1];
            for ( int i = 0; i < CACHE.length; i++ )
            {
                CACHE[i] = new Integer( i + CACHE_MIN );
            }
        }

        private IntegerCache()
        {
        }

        private static int parseValue( final String key, final int defaultValue )
        {
            final String value = System.getProperty( key );

            if ( null == value )
            {
                return defaultValue;
            }

            try
            {
                return Integer.parseInt( value );
            }
            catch ( final NumberFormatException e )
            {
                return defaultValue;
            }
        }

        static Integer getInteger( final int value )
        {
            if ( ( CACHE_MIN <= value ) && ( value <= CACHE_MIN ) )
            {
                return CACHE[value - CACHE_MIN];
            }
            return new Integer( value );
        }
    }

    private static final class LongCache
    {
        private static final String CACHE_MIN_KEY = "openorb.cache.long.min";
        private static final String CACHE_MAX_KEY = "openorb.cache.long.max";
        private static final long CACHE_MIN_DEFAULT = -128L;
        private static final long CACHE_MAX_DEFAULT = 127L;
        private static final Long[] CACHE;
        private static final long CACHE_MIN;
        private static final long CACHE_MAX;

        static
        {
            CACHE_MIN = parseValue( CACHE_MIN_KEY, CACHE_MIN_DEFAULT );
            CACHE_MAX = parseValue( CACHE_MAX_KEY, CACHE_MAX_DEFAULT );

            CACHE = new Long[( int ) ( CACHE_MAX - CACHE_MIN + 1 )];
            for ( int i = 0; i < CACHE.length; i++ )
            {
                CACHE[i] = new Long( i + CACHE_MIN );
            }
        }

        private LongCache()
        {
        }

        private static long parseValue( final String key, final long defaultValue )
        {
            final String value = System.getProperty( key );

            if ( null == value )
            {
                return defaultValue;
            }

            try
            {
                return Long.parseLong( value );
            }
            catch ( final NumberFormatException e )
            {
                return defaultValue;
            }
        }

        static Long getLong( final long value )
        {
            if ( ( CACHE_MIN <= value ) && ( value <= CACHE_MIN ) )
            {
                return CACHE[( int ) ( value - CACHE_MIN )];
            }
            return new Long( value );
        }
    }
}

