/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A weak set. This does not prevent it's members being reclaimed by the
 * garbage collector.
 *
 * @author Chris Wood
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public class WeakHashSet
    extends AbstractSet
    implements Set
{
    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * default capacity and load factor, which is <tt>0.75</tt>.
     */
    public WeakHashSet()
    {
        m_map = new WeakHashMap();
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection.  The capacity of the backing <tt>WeakHashMap</tt> instance is
     * twice the size of the specified collection or eleven (whichever is
     * greater), and the default load factor (which is <tt>0.75</tt>) is used.
     *
     * @param c the collection whose elements are to be placed into this set.
     */
    public WeakHashSet( Collection c )
    {
        m_map = new WeakHashMap( Math.max( 2 * c.size(), 11 ) );
        addAll( c );
    }

    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the hash map.
     * @param      loadFactor        the load factor of the hash map.
     */
    public WeakHashSet( int initialCapacity, float loadFactor )
    {
        m_map = new WeakHashMap( initialCapacity, loadFactor );
    }

    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * the specified initial capacity and default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param      initialCapacity   the initial capacity of the hash table.
     */
    public WeakHashSet( int initialCapacity )
    {
        m_map = new WeakHashMap( initialCapacity );
    }

    /**
     * The backing map.
     */
    private WeakHashMap m_map;

    /**
     * Dummy map entry.
     */
    private static final Object INMAP = new Object();

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     *
     */
    public Iterator iterator()
    {
        return m_map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set. Unlike most set
     * implementations this operation takes linear time in the size of the set.
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size()
    {
        return m_map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements. Unlike most set
     * implementations this operation takes linear time in the size of the set.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty()
    {
        return m_map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains( Object o )
    {
        return m_map.containsKey( o );
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    public boolean add( Object o )
    {
        return m_map.put( o, INMAP ) == null;
    }

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     */
    public boolean remove( Object o )
    {
        return m_map.remove( o ) == INMAP;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear()
    {
        m_map.clear();
    }
}
