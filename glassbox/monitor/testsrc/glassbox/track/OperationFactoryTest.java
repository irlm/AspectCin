/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.monitor.OperationFactory;
import glassbox.response.DefaultResponseFactory;
import glassbox.track.api.OperationDescription;

import javax.servlet.http.HttpServlet;
import javax.servlet.jsp.HttpJspPage;

import junit.framework.TestCase;

public class OperationFactoryTest extends TestCase {

    private OperationDescription descriptionA;
    private OperationDescription pkgServletA;
    private OperationDescription subJsp;
    private OperationDescription rootJsp;

    // XXX refactor some of these tests to be tests of the operation factory
    public void setUp() {
        OperationFactory factory = new OperationFactory();
        factory.setResponseFactory(new DefaultResponseFactory());
        descriptionA = factory.makeOperation(HttpServlet.class.getName(), "servletA");
        pkgServletA = factory.makeOperation(HttpServlet.class.getName(), "pkg.servletA");
        subJsp = factory.makeOperation(HttpJspPage.class.getName(), "/secure/bob.jsp");
        rootJsp = factory.makeOperation(HttpJspPage.class.getName(), "/fred.jsp");
    }
    
    public void testShortName() {
        assertEquals("servletA", descriptionA.getShortName());
        assertEquals("servletA", pkgServletA.getShortName());
        assertEquals("bob.jsp", subJsp.getShortName());
        assertEquals("fred.jsp", rootJsp.getShortName());
    }
    
    public void testFullName() {
        assertEquals("servletA", descriptionA.getShortName());
        assertEquals("pkg.servletA", pkgServletA.getOperationName());
        assertEquals("/secure/bob.jsp", subJsp.getOperationName());
        assertEquals("/fred.jsp", rootJsp.getOperationName());
    }
    
}
