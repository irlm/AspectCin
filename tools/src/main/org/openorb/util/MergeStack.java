/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.util.ListIterator;
import java.util.AbstractSequentialList;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

/**
 * A weak set. This does not prevent it's members being reclaimed by the
 * garbage collector.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2005/02/24 07:43:33 $
 */
public class MergeStack
    extends AbstractSequentialList
{
    private static final int DEFAULT_ALLOC_INC = 10;

    private static final Object LIST_EMPTY_OBJ = new Object();

    private int m_allocInc;

    private int m_size = 0;

    private PartialList m_head = null;

    private PartialList m_tail = null;

    private int m_modCount = 0;

    /**
     * Constructs a new, empty list.
     */
    public MergeStack()
    {
        this( DEFAULT_ALLOC_INC );
    }

    /**
     * Constructs a new, empty list with the given allocation increment.
     *
     * @param allocInc The increment value for new allocations.
     */
    public MergeStack( int allocInc )
    {
        m_allocInc = allocInc;
        m_head = new PartialList( m_allocInc );
        m_tail = m_head;
    }

    /**
     * Removes all of the elements from this collection. The collection will be
     * empty after this call returns.
     */
    public void clear()
    {
        m_head = new PartialList( m_allocInc );
        m_tail = m_head;
        m_size = 0;
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * @param index ???
     * @return A list iterator over the elements in this list (in proper
     * sequence).
     */
    public ListIterator listIterator( int index )
    {
        if ( index > m_size || index < 0 )
        {
            throw new IndexOutOfBoundsException();
        }
        return new MSListIterator( index );
    }

    /**
     * Returns the number of elements in this collection.  If the collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this collection.
     */
    public int size()
    {
        return m_size;
    }

    /**
     * Appends the specified element to the end of this stack (optional
     * operation). <p>
     *
     * @param obj The element to be appended to this stack.
     * @return <tt>true</tt> (as per the general contract of
     * <tt>Collection.add</tt>).
     */
    public boolean add( Object obj )
    {
        addLast( obj );
        return true;
    }

    /**
     * Get the first element in the list.
     *
     * @return the first element from the list
     * @throws NoSuchElementException if the list is empty.
     */
    public Object getFirst()
        throws NoSuchElementException
    {
        if ( m_size == 0 )
        {
            throw new NoSuchElementException();
        }
        Object ret = m_head.getFirst();
        if ( ret == LIST_EMPTY_OBJ )
        {
            throw new Error( "Illegal condition detected in MergerStack("
                  + System.identityHashCode( this )
                  + ").getFirst() : [ret == LIST_EMPTY_OBJ]" );
        }
        return ret;
    }

    /**
     * Add a new element to the front of the stack.
     *
     * @param obj The object to add to the stack.
     */
    public void addFirst( Object obj )
    {
        if ( !m_head.addFirst( obj ) )
        {
            PartialList n = m_head.setPrev( new PartialList( m_allocInc ) );
            n.setNext( m_head );
            m_head = n;
            m_head.addFirst( obj );
        }
        ++m_size;
        ++m_modCount;
    }

    /**
     * Remove and return the first element in the list.
     *
     * @return The first element from the list.
     * @throws NoSuchElementException if the list is empty.
     */
    public Object removeFirst()
        throws NoSuchElementException
    {
        if ( m_size == 0 )
        {
            throw new NoSuchElementException();
        }
        Object ret;
        while ( ( ret = m_head.removeFirst() ) == LIST_EMPTY_OBJ )
        {
            m_head = m_head.getNext();
            m_head.setPrev( null );
        }
        --m_size;
        ++m_modCount;
        return ret;
    }

    /**
     * Get the first element in the list.
     *
     * @return The first element from the list.
     * @throws NoSuchElementException if the list is empty.
     */
    public Object getLast()
        throws NoSuchElementException
    {
        if ( m_size == 0 )
        {
            throw new NoSuchElementException();
        }
        Object ret = m_tail.getLast();
        if ( ret == LIST_EMPTY_OBJ )
        {
            throw new Error( "Illegal condition detected in MergerStack("
                  + System.identityHashCode( this )
                  + ").getLast() : [ret == LIST_EMPTY_OBJ]" );
        }
        return ret;
    }

    /**
     * Add a new element to the end of the stack.
     *
     * @param obj The new element to add to the stack.
     */
    public void addLast( Object obj )
    {
        if ( !m_tail.addLast( obj ) )
        {
            PartialList n = m_tail.setNext( new PartialList( m_allocInc ) );
            n.setPrev( m_tail );
            m_tail = n;
            m_tail.addLast( obj );
        }
        ++m_size;
        ++m_modCount;
    }

    /**
     * Remove and return the last element in the list.
     *
     * @return the last element from the list
     * @throws NoSuchElementException if the list is empty.
     */
    public Object removeLast()
        throws NoSuchElementException
    {
        if ( m_size == 0 )
        {
            throw new NoSuchElementException();
        }
        Object ret;
        while ( ( ret = m_tail.removeLast() ) == LIST_EMPTY_OBJ )
        {
            m_tail = m_tail.getPrev();
            m_tail.setNext( null );
        }
        --m_size;
        ++m_modCount;
        return ret;
    }

    /**
     * Append another MergeStack onto this one. The other merge stack will
     * end up empty.
     *
     * @param next Another MergeStack instance to append.
     */
    public void append( MergeStack next )
    {
        if ( next.m_size == 0 )
        {
            return;
        }
        m_size += next.m_size;
        ++m_modCount;
        m_tail.append( next.m_head );
        if ( m_tail.getNext() != null )
        {
            m_tail = next.m_tail;
        }
        next.clear();
    }

    private static final class PartialList
    {
        private final Object [] m_objects;
        private final int m_capacity;

        private int m_start = 0;
        private int m_end = 0;

        private PartialList m_prev = null;
        private PartialList m_next = null;

        PartialList( int capacity )
        {
            m_capacity = capacity;
            m_objects = new Object[ m_capacity ];
        }

        public PartialList getPrev()
        {
            return m_prev;
        }

        public PartialList setPrev( PartialList prev )
        {
            m_prev = prev;
            return m_prev;
        }

        public PartialList getNext()
        {
            return m_next;
        }

        public PartialList setNext( PartialList next )
        {
            m_next = next;
            return m_next;
        }

        boolean full()
        {
            return m_end == m_start + m_capacity;
        }

        boolean empty()
        {
            return m_end == m_start;
        }

        int size()
        {
            return m_end - m_start;
        }

        Object getLast()
        {
            if ( empty() )
            {
                return LIST_EMPTY_OBJ;
            }
            return m_objects[ ( m_end - 1 ) % m_capacity ];
        }

        boolean addLast( Object obj )
        {
            if ( full() )
            {
                return false;
            }
            m_objects[ m_end % m_capacity ] = obj;
            ++m_end;
            return true;
        }

        Object removeLast()
        {
            if ( empty() )
            {
                return LIST_EMPTY_OBJ;
            }
            int p = --m_end % m_capacity;
            Object ret = m_objects[ p ];
            m_objects[ p ] = null;
            return ret;
        }

        Object getFirst()
        {
            if ( empty() )
            {
                return LIST_EMPTY_OBJ;
            }
            return m_objects[ m_start ];
        }

        boolean addFirst( Object obj )
        {
            if ( full() )
            {
                return false;
            }
            if ( --m_start < 0 )
            {
                m_start = m_capacity - 1;
                m_end += m_capacity;
            }
            m_objects[ m_start ] = obj;
            return true;
        }

        Object removeFirst()
        {
            if ( empty() )
            {
                return LIST_EMPTY_OBJ;
            }
            Object ret = m_objects[ m_start ];
            m_objects[ m_start ] = null;
            if ( ++m_start == m_capacity )
            {
                m_start = 0;
                m_end -= m_capacity;
            }
            return ret;
        }

        Object get( int pos )
        {
            if ( pos >= size() )
            {
                throw new IndexOutOfBoundsException();
            }
            return m_objects[ ( m_start + pos ) % m_capacity ];
        }

        Object set( int pos, Object repl )
        {
            if ( pos >= size() )
            {
                throw new IndexOutOfBoundsException();
            }
            Object ret = m_objects[ ( m_start + pos ) % m_capacity ];
            m_objects[ ( m_start + pos ) % m_capacity ] = repl;
            return ret;
        }

        void append( PartialList next )
        {
            int nsize = next.size();
            // merge the two together if there's lots of capacity
            if ( m_capacity - size() >= nsize )
            {
                for ( int i = 0; i < nsize; ++i )
                {
                    m_objects[ ( m_end + i ) % m_capacity ] =
                            next.m_objects[ ( next.m_start + i ) % next.m_capacity ];
                }
                m_end += nsize;
                m_next = next.m_next;
            }
            else
            {
                m_next = next;
            }

            if ( m_next != null )
            {
                m_next.m_prev = this;
            }
        }
    }

    private class MSListIterator
        implements ListIterator
    {
        private int m_mod;

        private int m_index;

        private PartialList m_curr;
        private int m_currIdx;

        private int m_repl = 1;

        MSListIterator( int index )
        {
            m_mod = m_modCount;
            m_index = index;
            if ( index < m_size / 2 )
            {
                m_curr = m_head;
                m_currIdx = 0;
                while ( index > m_curr.size() )
                {
                    index -= m_curr.size();
                    m_curr = m_curr.getNext();
                }
                m_currIdx = index;
            }
            else
            {
                m_curr = m_tail;
                index = m_size - index;
                while ( index > m_curr.size() )
                {
                    index -= m_curr.size();
                    m_curr = m_curr.getPrev();
                }
                m_currIdx = m_curr.size() - index;
            }
        }

        /**
         * Returns the index of the element that would be returned by a subsequent
         * call to <tt>previous</tt>. (Returns -1 if the list iterator is at the
         * beginning of the list.)
         *
         * @return the index of the element that would be returned by a subsequent
         *         call to <tt>previous</tt>, or -1 if list iterator is at
         *         beginning of list.
         */
        public int previousIndex()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            return m_index - 1;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            return m_index < m_size;
        }

        /**
         * Replaces the last element returned by <tt>next</tt> or
         * <tt>previous</tt> with the specified element (optional operation).
         * This call can be made only if neither <tt>ListIterator.remove</tt> nor
         * <tt>ListIterator.add</tt> have been called after the last call to
         * <tt>next</tt> or <tt>previous</tt>.
         *
         * @param o The element with which to replace the last element returned by
         *         <tt>next</tt> or <tt>previous</tt>.
         * @exception IllegalStateException if neither <tt>next</tt> nor
         *            <tt>previous</tt> have been called, or <tt>remove</tt> or
         *     <tt>add</tt> have been called after the last call to
         *     <tt>next</tt> or <tt>previous</tt>.
         */
        public void set( Object o )
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            if ( m_repl > 0 )
            {
                throw new IllegalStateException();
            }
            m_curr.set( m_currIdx + m_repl, o );
        }

        /**
         * Returns the next element in the interation.
         *
         * @return The next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         */
        public Object next()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            if ( m_index == m_size )
            {
                throw new NoSuchElementException();
            }
            Object ret = m_curr.get( m_currIdx );
            if ( ++m_currIdx >= m_curr.size() )
            {
                m_curr = m_curr.getNext();
                m_currIdx = 0;
            }
            m_index++;
            m_repl = 0;
            return ret;
        }

        /**
         * Returns the index of the element that would be returned by a subsequent
         * call to <tt>next</tt>. (Returns list size if the list iterator is at the
         * end of the list.)
         *
         * @return The index of the element that would be returned by a subsequent
         *         call to <tt>next</tt>, or list size if list iterator is at end
         *         of list.
         */
        public int nextIndex()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            return m_index;
        }

        /**
         *
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @exception UnsupportedOperationException if the <tt>remove</tt>
         *     operation is not supported by this Iterator.
         * @exception IllegalStateException if the <tt>next</tt> method has not
         *     yet been called, or the <tt>remove</tt> method has already
         *     been called after the last call to the <tt>next</tt>
         *     method.
         */
        public void remove()
        {
            if ( m_index == 0 )
            {
                removeFirst();
                m_curr = m_head;
                m_mod = m_modCount;
            }
            else if ( m_index == m_size )
            {
                removeLast();
                m_curr = m_tail;
                m_mod = m_modCount;
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * Returns <tt>true</tt> if this list iterator has more elements when
         * traversing the list in the reverse direction.  (In other words, returns
         * <tt>true</tt> if <tt>previous</tt> would return an element rather than
         * throwing an exception.)
         *
         * @return <tt>true</tt> if the list iterator has more elements when
         *         traversing the list in the reverse direction.
         */
        public boolean hasPrevious()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            return m_index > 0;
        }

        /**
         * Inserts the specified element into the list (optional operation).  The
         * element is inserted immediately before the next element that would be
         * returned by <tt>next</tt>, if any, and after the next element that
         * would be returned by <tt>previous</tt>, if any.  (If the list contains
         * no elements, the new element becomes the sole element on the list.)
         * The new element is inserted before the implicit cursor: a subsequent
         * call to <tt>next</tt> would be unaffected, and a subsequent call to
         * <tt>previous</tt> would return the new element.  (This call increases
         * by one the value that would be returned by a call to <tt>nextIndex</tt>
         * or <tt>previousIndex</tt>.)
         *
         * @param o The element to insert.
         * @exception UnsupportedOperationException if the <tt>add</tt> method is
         *     not supported by this list iterator.
         *
         * @exception ClassCastException if the class of the specified element
         *     prevents it from being added to this Set.
         *
         * @exception IllegalArgumentException if some aspect of this element
         *           prevents it from being added to this Collection.
         */
        public void add( Object o )
        {
            if ( m_index == 0 )
            {
                addFirst( o );
                m_curr = m_head;
                m_mod = m_modCount;
            }
            else if ( m_index == m_size )
            {
                addLast( o );
                m_curr = m_tail;
                m_mod = m_modCount;
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * Returns the previous element in the list.  This method may be called
         * repeatedly to iterate through the list backwards, or intermixed with
         * calls to <tt>next</tt> to go back and forth.  (Note that alternating
         * calls to <tt>next</tt> and <tt>previous</tt> will return the same
         * element repeatedly.)
         *
         * @return the previous element in the list.
         * @exception NoSuchElementException if the iteration has no previous
         *           element.
         */
        public Object previous()
        {
            if ( m_mod != m_modCount )
            {
                throw new ConcurrentModificationException();
            }
            if ( m_index == 0 )
            {
                throw new NoSuchElementException();
            }
            if ( --m_currIdx < 0 )
            {
                m_curr = m_curr.getPrev();
                m_currIdx = m_curr.size() - 1;
            }
            --m_index;
            m_repl = -1;
            return m_curr.get( m_currIdx );
        }
    }

    private static void testList( MergeStack s, int c )
    {
        ListIterator itt = s.listIterator( 0 );
        for ( int i = 0; i < c; ++i )
        {
            if ( itt.nextIndex() != i )
            {
                throw new Error( "itt.nextIndex() != i" );
            }
            if ( ( ( Integer ) itt.next() ).intValue() != i )
            {
                throw new Error( "((Integer)itt.next()).intValue() != i" );
            }
        }
        if ( itt.hasNext() )
        {
            throw new Error( "itt.hasNext()" );
        }
        for ( int i = 0; i < c; ++i )
        {
            if ( ( ( Integer ) s.removeFirst() ).intValue() != i )
            {
                throw new Error( "((Integer)s.removeFirst()).intValue() != i" );
            }
        }
        if ( !s.isEmpty() )
        {
            throw new Error( "!s.isEmpty()" );
        }
    }
}

