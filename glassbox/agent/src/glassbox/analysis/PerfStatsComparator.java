/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import glassbox.track.api.PerfStats;

import java.util.Comparator;

public class PerfStatsComparator implements Comparator {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg1, Object arg2) {
        PerfStats p1 = (PerfStats)arg1;
        PerfStats p2 = (PerfStats)arg2;
        long delta = p1.getAccumulatedTime() - p2.getAccumulatedTime();
        // sort descending
        return delta>0. ? -1 : (delta<0. ? 1 : 0);
    }

}
