/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.client.util.StringUtil;
import glassbox.common.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ConnectionHelperTest extends BaseTestCase {

    public ConnectionHelperTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ConnectionHelperTest.class);
        return suite;
    }

    public void testConnectionHelper() {
    	ConnectionHelper helper = (ConnectionHelper)getContext().getBean("connectionHelper");
    	assertNotNull(helper);
    	assertTrue(helper.getAllConnections().size() > 0);
    }
    
       
    public void testReplaceAll() {
        assertEquals("123", StringUtil.replace("1xyz2xyz3", "xyz", ""));
        assertEquals("", StringUtil.replace("ccc", "c", ""));
        assertEquals("BOOM", StringUtil.replace("BcM", "c", "OO"));
        assertEquals("BOOM", StringUtil.replace("BOOM", "BOOMY", "x"));
        assertEquals("xM", StringUtil.replace("BOOM", "BOO", "x"));
        assertEquals("1Y", StringUtil.replace("BOOMY", "BOOMY", "1Y"));
    }
}
