/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.util.timing.api.TimeConversion;

import java.util.List;

public class AbstractSlowProcessingProblem extends AbstractProblemAnalysis {

    protected long totalTime;
    protected int count;

    public AbstractSlowProcessingProblem(long totalTime, int count) {
        this.totalTime = totalTime;
        this.count = count;
    }

    public double getMeanTime() {
        return TimeConversion.meanNanosInSeconds(totalTime, count);
    }

    public List getEvents() {
        return null;
    }

}