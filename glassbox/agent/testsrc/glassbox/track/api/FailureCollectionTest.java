/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.util.Iterator;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class FailureCollectionTest extends MockObjectTestCase {
    private FailureCollection collection;
    private FailureDescription failure;
    private Request request;
    private Mock mockFailure;
    private Mock mockRequest;
    private int LIMIT = 3;

    public void setUp() {
        collection = new FailureCollection(LIMIT);
        mockFailure = makeFailure();
        failure = (FailureDescription) mockFailure.proxy();
        mockRequest = mock(Request.class);
        request = (Request) mockRequest.proxy();
    }

    public void testEmpty() {
        assertEquals(0, collection.getList().size());
    }

    public void testAddOne() {
        FailureStats stats = collection.add(failure, request);
        assertEquals(1, stats.getCount());
        assertEquals(1, collection.getList().size());
    }

    public void testAddAgain() {
        FailureStats stats1 = collection.add(failure, request);
        FailureStats stats2 = collection.add(failure, request);
        assertSame(stats1, stats2);
        assertEquals(2, stats1.getCount());
        assertEquals(1, collection.getList().size());
    }

    public void testAddDifferent() {
        Mock mockFailureB = makeFailure();
        FailureDescription failureB = (FailureDescription) mockFailureB.proxy();

        FailureStats stats1 = collection.add(failure, request);
        FailureStats stats2 = collection.add(failureB, request);
        assertNotSame(stats1, stats2);
        assertEquals(1, stats1.getCount());
        assertEquals(2, collection.getList().size());
    }

    public void testAtLimit() {
        Mock mockFailureB = makeFailure();
        FailureDescription failureB = (FailureDescription) mockFailureB.proxy();
        Mock mockFailureC = makeFailure();
        FailureDescription failureC = (FailureDescription) mockFailureC.proxy();

        FailureStats stats1 = collection.add(failure, request);
        FailureStats stats2 = collection.add(failureB, request);
        FailureStats stats3 = collection.add(failureC, request);
        List list = collection.getList();
        assertEquals(LIMIT, list.size());
    }
    
    public void testPastLimit() {
        for (int i=0; i<LIMIT+1; i++) {
            if (i>0) waitTick();
            mockFailure = makeFailure();
            failure = (FailureDescription) mockFailure.proxy();
            collection.add(failure, request);
        }
        
        List list = collection.getList();
        assertEquals(LIMIT, list.size());
    }

    public void testPreferMore() {
        FailureStats stats1 = collection.add(failure, request);
        collection.add(failure, request);
        Mock mockFailureB = makeFailure();
        FailureDescription failureB = (FailureDescription) mockFailureB.proxy();
        FailureStats stats2 = collection.add(failureB, request);

        for (int i = 0; i < 3; i++) {
            waitTick();
            Mock mockFailureC = makeFailure();
            FailureDescription failureC = (FailureDescription) mockFailureC.proxy();
            collection.add(failureC, request);
        }

        List list = collection.getList();
        assertEquals(LIMIT, list.size());
        assertTrue(list.contains(stats1));
        assertFalse(list.contains(stats2));
    }

    public void testPreferSevere() {
        FailureStats stats1 = collection.add(failure, request);
        collection.add(failure, request);
        Mock mockFailureB = makeFailure(FailureDetectionStrategy.WARNING);
        FailureDescription failureB = (FailureDescription) mockFailureB.proxy();
        FailureStats stats2 = collection.add(failureB, request);
        for (int i = 0; i < 3; i++) {
            waitTick();
            Mock mockFailureC = makeFailure(FailureDetectionStrategy.WARNING);
            FailureDescription failureC = (FailureDescription) mockFailureC.proxy();
            collection.add(failureC, request);
            collection.add(failureC, request);
        }

        assertTrue(collection.getList().contains(stats1));
        List list = collection.getList();
        assertEquals(LIMIT, list.size());
        assertFalse(list.contains(stats2));
    }

    private Mock makeFailure(int severity) {
        Mock mock = mock(FailureDescription.class, "severity "+severity);
        mock.stubs().method("getSeverity").withNoArguments().will(returnValue(severity));
        return mock;
    }

    private Mock makeFailure() {
        return makeFailure(FailureDetectionStrategy.FAILURE);
    }

    private void waitTick() {
        long tm = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(1);
            } catch (Throwable t) {;}
        } while (System.currentTimeMillis() == tm);
    }
}
