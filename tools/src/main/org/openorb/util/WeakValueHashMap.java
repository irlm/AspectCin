/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * ???
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/07 13:26:15 $
 */
public class WeakValueHashMap
    extends AbstractMap
    implements Map
{
    private final Map m_hash;

    private final ReferenceQueue m_queue = new ReferenceQueue();

    private Set m_entrySet;

    /**
     * Constructs a new, empty <code>WeakValueHashMap</code> with the given
     * initial capacity and the given load factor.
     *
     * @param  initialCapacity  The initial capacity of the
     *                          <code>WeakValueHashMap</code>
     * @param  loadFactor       The load factor of the <code>WeakValueHashMap</code>
     */
    public WeakValueHashMap( int initialCapacity, float loadFactor )
    {
        m_hash = new HashMap( initialCapacity, loadFactor );
    }

    /**
     * Constructs a new, empty <code>WeakValueHashMap</code> with the given
     * initial capacity and the default load factor, which is
     * <code>0.75</code>.
     *
     * @param  initialCapacity  The initial capacity of the
     *                          <code>WeakValueHashMap</code>
     */
    public WeakValueHashMap( int initialCapacity )
    {
        m_hash = new HashMap( initialCapacity );
    }

    /**
     * Constructs a new, empty <code>WeakValueHashMap</code> with the default
     * initial capacity and the default load factor, which is
     * <code>0.75</code>.
     */
    public WeakValueHashMap()
    {
        m_hash = new HashMap();
    }

    /**
     * Constructs a new <code>WeakValueHashMap</code> with the same mappings as the
     * specified <tt>Map</tt>.  The <code>WeakValueHashMap</code> is created with an
     * initial capacity of twice the number of mappings in the specified map
     * or 11 (whichever is greater), and a default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param   t the map whose mappings are to be placed in this map.
     * @since 1.3
     */
    public WeakValueHashMap( Map t )
    {
        this( Math.max( 2 * t.size(), 11 ), 0.75f );
        putAll( t );
    }

    /**
     * Remove all invalidated entries from the map, that is, remove all entries
     * whose keys have been discarded.  This method should be invoked once by
     * each public mutator in this class.  We don't invoke this method in
     * public accessors because that can lead to surprising
     * ConcurrentModificationExceptions.
     */
    private void processQueue()
    {
        WeakValue wv;
        while ( ( wv = ( WeakValue ) m_queue.poll() ) != null )
        {
            m_hash.remove( wv.getKey() );
        }
    }

    /**
     * Returns a <code>Set</code> view of the mappings in this map.
     *
     * @return The set of entries in the map. This method creates an empty set if
     * the member is null.
     */
    public Set entrySet()
    {
        if ( m_entrySet == null )
        {
            m_entrySet = new EntrySet();
        }
        return m_entrySet;
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested.
     * @return True if the internal map contains the key, false otherwise.
     */
    public boolean containsKey( Object key )
    {
        return m_hash.containsKey( key );
    }


    /**
     * Returns the value to which this map maps the specified <code>key</code>.
     * If this map does not contain a value for this key, then return
     * <code>null</code>.
     *
     * @param  key  The key whose associated value, if any, is to be returned
     * @return The value for the spacified key.
     */
    public Object get( Object key )
    {
        WeakValue val = ( WeakValue ) m_hash.get( key );
        if ( val == null )
        {
            return null;
        }
        return val.get();
    }

    /**
     * Updates this map so that the given <code>key</code> maps to the given
     * <code>value</code>.  If the map previously contained a mapping for
     * <code>key</code> then that mapping is replaced and the previous value is
     * returned.
     *
     * @param  key    The key that is to be mapped to the given
     *                <code>value</code>
     * @param  value  The value to which the given <code>key</code> is to be
     *                mapped
     *
     * @return  The previous value to which this key was mapped, or
     *          <code>null</code> if if there was no mapping for the key
     */
    public Object put( Object key, Object value )
    {
        processQueue();
        return m_hash.put( key, new WeakValue( key, value, m_queue ) );
    }

    /**
     * Removes the mapping for the given <code>key</code> from this map, if
     * present.
     *
     * @param  key  The key whose mapping is to be removed
     *
     * @return  The value to which this key was mapped, or <code>null</code> if
     *          there was no mapping for the key
     */
    public Object remove( Object key )
    {
        processQueue();
        return m_hash.remove( key );
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear()
    {
        processQueue();
        m_hash.clear();
    }

    /* weak value. Entered into map. */
    private static class WeakValue
        extends WeakReference
    {
        private Object m_key;

        public WeakValue( Object k, Object val, ReferenceQueue q )
        {
            super( val, q );
            m_key = k;
        }

        public Object getKey()
        {
            return m_key;
        }
    }

    private static boolean valEquals( Object o1, Object o2 )
    {
        return ( o1 == null ) ? ( o2 == null ) : o1.equals( o2 );
    }

    /* Internal class for entries */
    private class Entry
        implements Map.Entry
    {
        private Map.Entry m_ent;
        /* Strong reference to value, so that the GC
          will leave it alone as long as this Entry
          exists */
        private Object m_value;

        Entry( Map.Entry ent, Object value )
        {
            m_ent = ent;
            m_value = value;
        }

        public Object getKey()
        {
            return m_ent.getKey();
        }

        public Object getValue()
        {
            return m_value;
        }

        public Object setValue( Object value )
        {
            m_value = value;
            return m_ent.setValue( new WeakValue( m_ent.getKey(), value, m_queue ) );
        }

        public boolean equals( Object o )
        {
            if ( !( o instanceof Map.Entry ) )
            {
                return false;
            }
            Map.Entry e = ( Map.Entry ) o;
            return ( valEquals( getKey(), e.getKey() )
                     && valEquals( m_value, e.getValue() ) );
        }

        public int hashCode()
        {
            Object k;
            return ( ( ( ( k = getKey() ) == null ) ? 0 : k.hashCode() )
                     ^ ( ( m_value == null ) ? 0 : m_value.hashCode() ) );
        }

    }


    /* Internal class for entry sets */
    private class EntrySet
        extends AbstractSet
    {
        private Set m_hashEntrySet = m_hash.entrySet();

        public Set getEntrySet()
        {
            return m_hashEntrySet;
        }

        public Iterator iterator()
        {
            return new Iterator()
                   {
                       private Iterator m_hashIterator = getEntrySet().iterator();
                       private Entry m_next = null;
                       public boolean hasNext()
                       {
                           while ( m_hashIterator.hasNext() )
                           {
                               Map.Entry ent = ( Map.Entry ) m_hashIterator.next();
                               WeakValue wv = ( WeakValue ) ent.getValue();
                               Object v = null;
                               if ( ( wv != null ) && ( ( v = wv.get() ) == null ) )
                               {
                                   // Weak value has been cleared by GC
                                   continue;
                               }
                               m_next = new Entry( ent, v );
                               return true;
                           }
                           return false;
                       }

                       public Object next()
                       {
                           if ( ( m_next == null ) && !hasNext() )
                           {
                               throw new NoSuchElementException();
                           }
                           Entry e = m_next;
                           m_next = null;
                           return e;
                       }

                       public void remove()
                       {
                           m_hashIterator.remove();
                       }
                   };
        }

        public boolean isEmpty()
        {
            return !( iterator().hasNext() );
        }

        public int size()
        {
            int j = 0;
            for ( Iterator i = iterator(); i.hasNext(); i.next() )
            {
                j++;
            }
            return j;
        }

        public boolean remove( Object o )
        {
            processQueue();
            if ( !( o instanceof Map.Entry ) )
            {
                return false;
            }
            Map.Entry e = ( Map.Entry ) o;
            Object ek = e.getKey();
            WeakValue wv = ( WeakValue ) m_hash.get( ek );
            if ( ( wv == null ) ? m_hash.containsKey( ek ) : ( wv.get() != null ) )
            {
                m_hash.remove( ek );
                return true;
            }
            return false;
        }

        public int hashCode()
        {
            int h = 0;
            for ( Iterator i = getEntrySet().iterator(); i.hasNext(); )
            {
                Map.Entry ent = ( Map.Entry ) i.next();
                Object k = ent.getKey();
                if ( k == null )
                {
                    continue;
                }
                WeakValue wv = ( WeakValue ) ent.getValue();
                Object v;
                h += ( k.hashCode()
                       ^ ( ( wv == null || ( v = wv.get() ) == null ) ? 0 : v.hashCode() ) );
            }
            return h;
        }
    }
}

