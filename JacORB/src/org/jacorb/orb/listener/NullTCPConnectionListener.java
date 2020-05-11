/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.orb.listener;

/**
 * empty implementation of the TCPConnectionListener interface
 *
 * @author Alphonse Bendt
 * @version $Id: NullTCPConnectionListener.java,v 1.2 2006/06/29 15:42:48 alphonse.bendt Exp $
 */
public class NullTCPConnectionListener implements TCPConnectionListener
{
    public void connectionOpened(TCPConnectionEvent e)
    {
        // empty implementation
    }

    public void connectionClosed(TCPConnectionEvent e)
    {
        // empty implementation
    }

    public boolean isListenerEnabled()
    {
        return false;
    }
}
