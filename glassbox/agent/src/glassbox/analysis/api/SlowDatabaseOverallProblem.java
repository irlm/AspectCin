/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import java.util.*;

public class SlowDatabaseOverallProblem extends SlowAggregateResourceProblem {

    public SlowDatabaseOverallProblem(List distinctCalls, List resourceKeys, long accumulatedTime, int aggCount, int opCount) {
        super(distinctCalls, resourceKeys, accumulatedTime, aggCount, opCount);
    }

    private static final long serialVersionUID = 1; 
}
