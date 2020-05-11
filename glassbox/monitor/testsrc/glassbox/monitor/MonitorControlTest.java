/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor;

import junit.framework.TestCase;
import glassbox.monitor.AbstractMonitorControl.RuntimeControl;

public class MonitorControlTest extends TestCase {
    static int count;
    public void setUp() {
        count = 0;
        AbstractMonitor.setAllDisabled(false);
        RuntimeControl.aspectOf(DummyMonitor.aspectOf()).setEnabled(true);
        RuntimeControl.aspectOf(DummySubMonitor.aspectOf()).setEnabled(true);
    }
    public void tearDown() {
        RuntimeControl.exit();
        AbstractMonitor.setAllDisabled(false);
    }
    public void testEnabled() {
        RuntimeControl.enter();
        hook();
        assertEquals(1, count);
    }
    public void testGlobalDisabled() {
        AbstractMonitor.setAllDisabled(true);
        RuntimeControl.enter();
        hook();
        assertEquals(0, count);
    }
    public void testLocalDisabled() {
        RuntimeControl.aspectOf(DummyMonitor.aspectOf()).setEnabled(false);
        RuntimeControl.enter();
        hook();
        assertEquals(0, count);
    }
    public void testSubDisabled() {
        RuntimeControl.aspectOf(DummySubMonitor.aspectOf()).setEnabled(false);
        RuntimeControl.enter();
        rook();
        assertEquals(0, count);
    }
    public void testSiblingDisabled() {
        // disabling a sibling is the closest we can come - we can't get an instance of the abstract super aspect
        RuntimeControl.aspectOf(DummyMonitor.aspectOf()).setEnabled(false);
        RuntimeControl.enter();
        rook();
        assertEquals(1, count);
    }
    private void hook() {}
    private void rook() {}
    private static abstract aspect DummySuperMonitor extends AbstractMonitor {
        pointcut hooked();
        before() : within(MonitorControlTest) && hooked() {
            count++;
        }
        public String getLayer() {
            return "";
        }            
    }
    
    private static aspect DummyMonitor extends DummySuperMonitor {
        before() : within(MonitorControlTest) && execution(* hook()) {
            count++;
        }
    }
    private static aspect DummySubMonitor extends DummySuperMonitor {
        pointcut hooked() : execution(* rook(..));
    }
}
