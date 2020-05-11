/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import java.util.List;

import junit.framework.TestCase;

import glassbox.track.api.StatisticsTypeImpl;

public class StatisticsTypeTest extends TestCase {
    public void testOrdered() {
        List allStats = StatisticsTypeImpl.getAllTypes();
        int len = allStats.size();
        assertTrue(len>0);
        StatisticsTypeImpl type = (StatisticsTypeImpl)allStats.get(0);
        int prev = type.getIndex();
        for (int j=1; j<len; j++) {
            type = (StatisticsTypeImpl)allStats.get(j);
            int next = type.getIndex();
            assertTrue(next>prev);
            prev = next;
        }
    }
}
