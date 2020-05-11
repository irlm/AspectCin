/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor;

import java.io.*;

import glassbox.config.extension.api.OperationPlugin;
import glassbox.track.api.OperationDescription;
import glassbox.track.api.OperationDescriptionImpl;

import javax.servlet.http.HttpServlet;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class OperationDescriptionTest extends MockObjectTestCase {

    private OperationDescription descriptionA;
    private OperationDescription descriptionAprime;
    private OperationDescription descriptionB;
    private OperationDescription descriptionX;

    public void setUp() {
        descriptionA = makeOperation(HttpServlet.class.getName(), "servletA");
        descriptionAprime = makeOperation(HttpServlet.class, "servletA");
        descriptionB = makeOperation(HttpServlet.class, "servletB");
        descriptionX = makeOperation(Object.class, "servletA");
    }
    
    public void testEquals() {
        assertEquals("servletA", descriptionA.getOperationName());
        assertEquals(HttpServlet.class.getName(), descriptionAprime.getOperationType());
        assertTrue(descriptionA.equals(descriptionA));
        assertTrue(descriptionA.equals(descriptionAprime));
        assertEquals(descriptionA.hashCode(), descriptionAprime.hashCode());
        assertFalse(descriptionA.equals(null));
        assertFalse(descriptionA.equals(descriptionB));
        assertFalse(descriptionA.equals(descriptionX));
    }
    
    public void testSimpleCopy() {
        OperationDescription od = new OperationDescriptionImpl((OperationDescriptionImpl)descriptionA);
        assertEquals(od, descriptionA);
        assertNull(od.getPlugin());
    }
    
    public void testPluginCopy() {            
        Mock plugin=mock(OperationPlugin.class);
        plugin.stubs().method("getKey").will(returnValue("sample.plugin"));
        descriptionB.setPlugin((OperationPlugin)plugin.proxy());
        assertNotNull(descriptionB.getPluginKey());
        OperationDescription od = new OperationDescriptionImpl((OperationDescriptionImpl)descriptionB);
        assertEquals(od, descriptionB);       
        assertEquals(od.getPluginKey(), descriptionB.getPluginKey());
    }
    
    public void testPluginSerialization() {            
        Mock plugin=mock(OperationPlugin.class);
        plugin.stubs().method("getKey").will(returnValue("sample.plugin"));        
        descriptionB.setPlugin((OperationPlugin)plugin.proxy());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(descriptionB);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        OperationDescription od = (OperationDescription)ois.readObject();
        assertEquals(od, descriptionB);       
        assertEquals(descriptionB.getPluginKey(), od.getPluginKey());
        assertNotNull(descriptionB.getPluginKey());
    }
    
    public void testBadCtorType() {
        try {
            makeOperation((String)null, "");
            fail("bad ctor call");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testToString() {
        String a = descriptionA.toString();
        String b = descriptionAprime.toString();
        assertTrue(!a.equals(""));
        assertTrue(a.equals(b));
    }
    
    public void testBadCtorKey() {
        try {
            makeOperation("", null);
            fail("bad ctor call");
        } catch (IllegalArgumentException e) {
            // success
        }
    }
    
    private OperationDescription makeOperation(Class type, String operation) {
        return makeOperation(type.getName(), operation);
    }
    
    private OperationDescription makeOperation(String type, String operation) {
        return new OperationDescriptionImpl(type, operation, operation, null, false);
    }   
}
