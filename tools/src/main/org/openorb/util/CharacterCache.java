/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

/**
 * A utility class for obtaining <code>Character</code> instances.
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
 *     Note that this also disables <code>Number</code> caching.
 *   </td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.character.min</code></td>
 *   <td>
 *     The min (inclusive) <code>Character</code> value to cache
 *     (default \u0000).
 *   </td>
 *  </tr>
 *  <tr>
 *   <td><code>openorb.cache.character.max</code></td>
 *   <td>
 *     The max (inclusive) <code>Character</code> value to cache
 *     (default \u00FF).
 *   </td>
 *  </tr>
 * </table>
 *
 * <p>
 * The min and max values can be specified as either a literal or a unicode escape.
 *
 * @author Richard G Clark
 * @version $Revision: 1.4 $ $Date: 2004/06/23 07:13:18 $
 * @see NumberCache
 */
public final class CharacterCache
{
    private static final String DISABLE_CACHE_KEY = "openorb.cache.disable";
    private static final boolean DISABLE_CACHE
            = "true".equals( System.getProperty( DISABLE_CACHE_KEY ) );

    private CharacterCache()
    {
    }

    /**
     * Gets the <code>Character</code> instance for the specifed value.
     *
     * @param value the value for the object
     *
     * @return the <code>Character</code> instance
     */
    public static Character getCharacter( final char value )
    {
        if ( DISABLE_CACHE )
        {
            return new Character( value );
        }

        return Cache.getCharacter( value );
    }

    private static final class Cache
    {
        private static final String CACHE_MIN_KEY = "openorb.cache.character.min";
        private static final String CACHE_MAX_KEY = "openorb.cache.character.max";
        private static final char CACHE_MIN_DEFAULT = '\u0000';
        private static final char CACHE_MAX_DEFAULT = '\u00FF';
        private static final Character[] CACHE;
        private static final char CACHE_MIN;
        private static final char CACHE_MAX;

        static
        {
            CACHE_MIN = parseValue( CACHE_MIN_KEY, CACHE_MIN_DEFAULT );
            CACHE_MAX = parseValue( CACHE_MAX_KEY, CACHE_MAX_DEFAULT );

            CACHE = new Character[CACHE_MAX - CACHE_MIN + 1];
            for ( int i = 0; i < CACHE.length; i++ )
            {
                CACHE[i] = new Character( ( char ) ( i + CACHE_MIN ) );
            }
        }

        private Cache()
        {
        }

        private static char parseValue( final String key, final char defaultValue )
        {
            final String value = System.getProperty( key );

            if ( null == value )
            {
                return defaultValue;
            }

            if ( 1 == value.length() )
            {
                return value.charAt( 0 );
            }

            if ( !value.startsWith( "\\u" ) )
            {
                return defaultValue;
            }

            try
            {
                return ( char ) Integer.parseInt( value.substring( 2 ), 16 );
            }
            catch ( final NumberFormatException e )
            {
                return defaultValue;
            }
        }

        static Character getCharacter( final char value )
        {
            if ( ( CACHE_MIN <= value ) && ( value <= CACHE_MIN ) )
            {
                return CACHE[value - CACHE_MIN];
            }
            return new Character( value );
        }
    }

}

