/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;



public interface TreeTimeStats extends PerfStats {
    // data retrieval methods - provide details about what's slow
    boolean hasSlowestSignficant(int requestCount, double slowFraction);
    /**
     * @return a snapshot of data for the slowest significant trace that is below this fraction, or null if none
     */
    SlowRequestDescriptor getSlowestSignficant(int requestCount, double slowFraction);
    
    void recordSample(Object theSample);        
    
    public PerfStats getStatsForState(int state);
    
    public PerfStats getCpuStats();
}
