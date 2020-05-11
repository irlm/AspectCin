/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.util.LinkedList;

/**
 * Implementation for current objects. Each push call should allways
 * have a corresponding pop.
 *
 * @author Chris Wood
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public class CurrentStack
{
    private ThreadLocal m_local = new StackThreadLocal();

    /**
     * Constructor.
     */
    private static class StackThreadLocal
        extends ThreadLocal
    {
        protected Object initialValue()
        {
            return new LinkedList();
        }
    }

    /**
     * Get current data.
     *
     * @return The object on the top of the current stack.
     */
    public synchronized Object peek()
    {
        LinkedList ts = ( LinkedList ) m_local.get();
        return ts.getLast();
    }

    /**
     * Setup current data. Every push call should have a corresponding
     * pop call.
     *
     * @param obj object to put on the current stack.
     */
    public synchronized void push( Object obj )
    {
        LinkedList ts = ( LinkedList ) m_local.get();
        ts.addLast( obj );
    }

    /**
     * Remove data from current. Associated with a push call.
     *
     * @return The old current object.
     */
    public synchronized Object pop()
    {
        LinkedList ts = ( LinkedList ) m_local.get();
        return ts.removeLast();
    }

    /**
     * Replace current data.
     *
     * @param obj object to replace the current object.
     * @return The old current object.
     */
    public synchronized Object set( Object obj )
    {
        LinkedList ts = ( LinkedList ) m_local.get();
        Object ret = ts.removeLast();
        ts.addLast( obj );
        return ret;
    }
}

