/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

/**
 * Identity hash table key. Use to wrap objects to insert into hash maps
 * where the entry should be hashed/looked up on the actual object, rather
 * than any equals / hashCode operations it defines.
 *
 * @author Chris Wood
 */
public class IdentityKey
{
    private Object m_target;

    /**
     * Constructor.
     *
     * @param target The target object to store in this instance.
     */
    public IdentityKey( Object target )
    {
        m_target = target;
    }

    /**
     * Return the hash code of the internal object.
     *
     * @return A hash code for the internal object.
     */
    public int hashCode()
    {
        return System.identityHashCode( m_target );
    }

    /**
     * Compare two instances of this class.
     *
     * @param obj Another IdentityKey instance.
     * @return True if both are IdentityKey instances and the internal
     * object is the same instance, false otherwise.
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof IdentityKey ) )
        {
            return false;
        }
        return m_target == ( ( IdentityKey ) obj ).m_target;
    }
}

