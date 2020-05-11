/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.analysis.api;

import java.util.Comparator;

public class SlowCallComparator implements Comparator {

    public int compare(Object /* <SlowCallProblem> */ obj1, Object /* <SlowCallProblem> */ obj2) {
        SlowCallProblem call1 = (SlowCallProblem) obj1;
        SlowCallProblem call2 = (SlowCallProblem) obj2;
        
        if (call1.getAccumulatedTime() == call2.getAccumulatedTime()) 
            return 0; 
        
        if (call1.getAccumulatedTime() > call2.getAccumulatedTime()) 
            return -1;
        
        return 1;
    }
    
}