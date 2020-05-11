/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import java.util.*;

import glassbox.track.api.*;
import glassbox.util.timing.api.TimeConversion;


public class OperationAnalysisImpl implements OperationAnalysis {
    private OperationSummary summary;
    private List/*<ProblemAnalysis>*/ problems = new ArrayList/*<ProblemAnalysis>*/();
    //private List/*<List>*/ problemTypes = new ArrayList/*<Class>*/();
    private ScenarioSummary scenarioSummary;
    private TimeDecomposition componentDecomposition; 
    private TimeDecomposition resourceDecomposition; 
    private long monitorStartTime;
    private long slowThresholdMillis;
    private List/*<Request>*/ problemRequests;
    private double minimumSlowFrac;
    private double meanCpuTime;
    
    public OperationAnalysisImpl(OperationSummary summary, ScenarioSummary scenarioSummary, TimeDecomposition componentDecomposition, TimeDecomposition resourceDecomposition, List problemRequests, long monstart, double meanCpuTime) {
        this.summary = summary;
        this.scenarioSummary = scenarioSummary;
        this.monitorStartTime = monstart;
        
        this.problemRequests = problemRequests;
        this.componentDecomposition = componentDecomposition;
        this.resourceDecomposition = resourceDecomposition;
        this.meanCpuTime = meanCpuTime;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.OperationAnalysis#getComponentDecomposition()
     */
    public TimeDecomposition getComponentDecomposition() {
        return componentDecomposition;
    }
    
    public TimeDecomposition getResourceDecomposition() {
        return resourceDecomposition;
    }
    
    /* Returning the percent of the time spent in a certain part of the component or resouce compared to the overall operation */
    public int getComponentTimePercent(int compID) { 
        try {
            if(scenarioSummary.getAccumulatedTime() <= 0 || componentDecomposition.getPart(compID).getAccumulatedTime() <= 0){
                return 0;
            }   
            return Math.round(100 * componentDecomposition.getPart(compID).getAccumulatedTime() / scenarioSummary.getAccumulatedTime());
        } catch (RuntimeException e) {
            return 0;
        }

    }
    
    public int getResourceTimePercent(int compID) {   
        try {
            if(scenarioSummary.getAccumulatedTime() <= 0 || resourceDecomposition.getPart(compID).getAccumulatedTime() <= 0){
                return 0;
            }   
            return (int)Math.round(100. * ((double)resourceDecomposition.getPart(compID).getAccumulatedTime()) / 
                    (double)scenarioSummary.getAccumulatedTime());
        } catch (RuntimeException e) {
            return 0;
        }

    }
    
    public int getCPUTimePercent() { 

        long accum = scenarioSummary.getAccumulatedTime();
        if (accum <= 0) {
            return 0;
        }

        double percent = getMeanCpuTime()*(double)scenarioSummary.getCount()/((double)accum);
        return (int)Math.round(percent*100.);

    }
    
    public String getFormatCPUTime(){
        return TimeConversion.formatTime((long)getMeanCpuTime());
    }
    
    
    public String getComponentTimeMS(int compID){
        
        try {
            
            return TimeConversion.formatTime(componentDecomposition.getPart(compID).getAccumulatedTime() / componentDecomposition.getPart(compID).getCount());
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            return "0.0 s";
        }  
                
    }
    
    public String getResourceTimeMS(int compID){
        
        try {

            return TimeConversion.formatTime(resourceDecomposition.getPart(compID).getAccumulatedTime() / resourceDecomposition.getPart(compID).getCount());
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            return "0.0 s";
        }  
                
    }

    public OperationSummary summary() {
        return summary;
    }
    
    public ScenarioSummary getScenarioSummary() {
        return scenarioSummary;
    }
    
    public List/*<ProblemAnalysis>*/ problems() {
        return problems;
    }
    
    public List/*<ProblemAnalysis>*/ getProblemsOfType(Class/*<? extends ProblemAnalysis>*/ problemSubclass) {
        ArrayList/*<ProblemAnalysis>*/ tproblems = new ArrayList/*<ProblemAnalysis>*/();
        for (Iterator iter = problems.iterator(); iter.hasNext();) {
            ProblemAnalysis problem = (ProblemAnalysis) iter.next();
            if (problemSubclass.isInstance(problem)) {
                tproblems.add(problem);
            }
        }
        return tproblems;
    }

    public long getMonitoringStartTime() {
    	return monitorStartTime;
    }
    
    public double getMinimumSlowFrac() {
        return minimumSlowFrac;
    }

    public void setMinimumSlowFrac(double minimumSlowFrac) {
        this.minimumSlowFrac = minimumSlowFrac;
    }

    public long getSlowThresholdMillis() {
        return slowThresholdMillis;
    }

    public void setSlowThresholdMillis(long slowThresholdMillis) {
        this.slowThresholdMillis = slowThresholdMillis;
    }

    public List/*<Request>*/ getProblemRequests() {
        return problemRequests;
    }
    
    public boolean isFailing() {
        return summary.isFailing();
    }

    public boolean isSlow() {
        return summary.isSlow();
    }
    
    private static final long serialVersionUID = 2;

    public double getMeanCpuTime() {
        return meanCpuTime;
    }
}
