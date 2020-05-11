package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id: MessagePushOperation.java,v 1.5 2005/04/17 17:10:46 alphonse.bendt Exp $
 */
public abstract class MessagePushOperation implements PushOperation {
    
    protected final Message message_;
    
    public MessagePushOperation(Message message) {
        message_ = (Message)message.clone();
    }

    public void dispose() {
        message_.dispose();
    }
}
