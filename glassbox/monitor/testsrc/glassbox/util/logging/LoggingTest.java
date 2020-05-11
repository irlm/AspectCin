/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.logging;

import glassbox.util.logging.api.LogManagement;
import glassbox.util.logging.api.LogOwner;
import glassbox.util.org.sl4j.ILoggerFactory;
import glassbox.util.org.sl4j.Logger;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LoggingTest extends MockObjectTestCase implements LogOwner {
    public void tearDown() {
        LogManagement.setLoggerFactory(null);
    }
    
	public void testGetLogger() {
        Mock mockFactory = mock(ILoggerFactory.class);
        Mock mockLogger = mock(Logger.class);
        mockFactory.stubs().method("getLogger").will(returnValue(mockLogger.proxy()));
        mockFactory.expects(once()).method("getLogger").with(eq(LoggingTest.class.getName())).will(returnValue(mockLogger.proxy()));
        LogManagement.setLoggerFactory((ILoggerFactory)mockFactory.proxy());		
		assertEquals(mockLogger.proxy(), getLogger());
        assertEquals(mockLogger.proxy(), LogManagement.getLogger(LoggingTest.class));
	}
}
