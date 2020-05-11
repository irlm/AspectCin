/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.TreeTimeStats;

public aspect LogTimeSamples {
    //exposes AspectJ bug... just inlined for now
//    private int TreeTimeStats.logCount = 0;
//    private int TreeTimeStats.nextLogCount = 1;
//    
//    after(TreeTimeStats stats) : execution(* recordSample(..)) && this(stats) {
//        if (++stats.logCount >= stats.nextLogCount) {
//            logInfo(stats.toString());
//            stats.nextLogCount *= 10;
//        }
//    }
    
    private static final long serialVersionUID = 1L;
}
