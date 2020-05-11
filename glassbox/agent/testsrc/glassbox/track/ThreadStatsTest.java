/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.StatisticsRegistry;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ThreadStatsTest extends MockObjectTestCase {

    public void testPeek() {
        Mock mockRegistry = mock(StatisticsRegistry.class);
        
        ThreadStats threadStats = new ThreadStats();
        threadStats.setRegistry((StatisticsRegistry)mockRegistry.proxy());
        
        assertEquals(mockRegistry.proxy(), threadStats.peek());
    }
}
