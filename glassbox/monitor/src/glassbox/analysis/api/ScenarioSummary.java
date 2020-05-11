/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

public class ScenarioSummary extends SummaryStats {

    public static final int NUMBER_OF_SCENARIOS = 3;
    
    private SummaryStats[] scenarios;
    
    public ScenarioSummary(int count, long accumulatedTime, SummaryStats scenarios[]) {
        super(count, accumulatedTime);
        this.scenarios = scenarios;
    }

    public SummaryStats getScenario(int scenario) {
        return scenarios[scenario];
    }
    
}
