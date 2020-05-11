/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.ui;

import glassbox.monitor.MonitorResponseTestCase;
import glassbox.response.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.*;
import org.apache.struts.actions.DispatchAction;

public class StrutsRequestUnitTest extends MonitorResponseTestCase {
    
    public void testNormalAction() {
        new DummyStrutsAction().execute(null, null, null, null);
        assertEquals(2, listener.responses.size());
        Response end = getResponse(1);
        assertEquals(time[0], end.getStart());
        assertEquals(time[1], end.getEnd());
        assertProperties(operationFactory.makeOperation(Action.class, DummyStrutsAction.class.getName()), Response.UI_CONTROLLER, end);
    }
    
    public void testDispatchAction() {
        new DummyDispatchAction().execute(null, null, null, null);
        assertEquals(4, listener.responses.size());
        
        Response nested = getResponse(2);
        assertEquals(time[1], nested.getStart());
        assertEquals(time[2], nested.getEnd());
        assertProperties(operationFactory.makeOperation(Action.class, DummyDispatchAction.class.getName()+".helper"), Response.UI_CONTROLLER, nested);
        
        Response end = getResponse(3);
        assertEquals(time[0], end.getStart());
        assertEquals(time[3], end.getEnd());
        assertProperties(operationFactory.makeOperation(Action.class, DummyDispatchAction.class.getName()+".execute"), Response.UI_CONTROLLER, end);
    }

    static class DummyStrutsAction extends Action {
        public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
            return null;
        }
    }

    static class DummyDispatchAction extends DispatchAction {
        public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
            return helper(mapping, form, request, response);
        }
        public ActionForward helper(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
            return null;
        }
    }

}
