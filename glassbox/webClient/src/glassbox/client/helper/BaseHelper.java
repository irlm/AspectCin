/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.client.web.session.SessionData;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

public abstract class BaseHelper implements Serializable {

    protected SessionData getSessionData(HttpSession httpSession) {
        SessionData session = (SessionData) httpSession.getAttribute(SessionData.CLIENT_SESSION_KEY);
        if (session == null) {
            session = new SessionData();            
            httpSession.setAttribute(SessionData.CLIENT_SESSION_KEY, session);
        }
        return session;
    }
}
