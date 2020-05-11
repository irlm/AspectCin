/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import glassbox.analysis.api.SlowProblem;

import java.util.Comparator;

public class SlowProblemComparator implements Comparator {

    public int compare(Object arg1, Object arg2) {
        SlowProblem p1 = (SlowProblem)arg1;
        SlowProblem p2 = (SlowProblem)arg2;
        double delta = p1.getMeanTime() - p2.getMeanTime();
                
        // sort descending
        return delta>0. ? -1 : (delta<0. ? 0 : -1); 
    }

}
