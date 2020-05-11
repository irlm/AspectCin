/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

public class DefaultTimeDecomposition implements TimeDecomposition {

    private FrequencySummaryStats parts[];
    
    public DefaultTimeDecomposition() {
        parts = new FrequencySummaryStats[MAX_PARTS];
        for (int i=0; i<MAX_PARTS; i++) {
            parts[i] = new FrequencySummaryStats(0, 0, 0L);
        }
    }
    
    public FrequencySummaryStats getPart(int componentId) {
        return parts[componentId];
    }
    
    public void setPart(int componentId, FrequencySummaryStats frequencyStats) {
        parts[componentId] = frequencyStats;
    }

}
