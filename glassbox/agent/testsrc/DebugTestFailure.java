/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
import glassbox.acceptance.ContentionTest;
import glassbox.monitor.thread.ThreadMonitorIntegrationTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DebugTestFailure {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for default package");
        //$JUnit-BEGIN$
        suite.addTestSuite(ThreadMonitorIntegrationTest.class);
        suite.addTestSuite(ContentionTest.class);
        //$JUnit-END$
        return suite;
    }

}
