/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.pojo;

import glassbox.analysis.api.*;
import glassbox.client.helper.DisplayHelper;
import glassbox.client.helper.MessageHelper;
import glassbox.track.api.OperationPerfStats;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class OperationAnalysisData implements Serializable {

    protected OperationAnalysis operationAnalysis = null;    
    
    public OperationAnalysisData(OperationAnalysis operationAnalysis) {
        this.operationAnalysis = operationAnalysis;
    }

    public OperationAnalysis getOperationAnalysis() {
        return operationAnalysis;
    }

    public void setOperationAnalysis(OperationAnalysis operationAnalysis) {
        this.operationAnalysis = operationAnalysis;
    }
    
    public OperationSummary getSummary() {
        return operationAnalysis.summary();
    }
    
    public List getProblems() {
       return operationAnalysis.problems();        
    }
    
    public ScenarioSummary getScenarioSummary() {
        return operationAnalysis.getScenarioSummary();
    }

    public long getMonitoringStartTime() {
        return operationAnalysis.getMonitoringStartTime();
    }
    
    public String getMonitoringStartTimeAsDate() {
        
        Date then = new Date(operationAnalysis.getMonitoringStartTime());
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(then);
    }
       
    public String getSlowThresholdMillis() {
        return TimeConversion.formatTime(operationAnalysis.getSlowThresholdMillis()*1000);
    }
    
    public String getSlowThresholdSecondsLongFormat() {
        return TimeConversion.formatMillis(operationAnalysis.getSlowThresholdMillis());
    }
    
    public String getSlowThresholdSeconds() {
        return TimeConversion.formatMillis(operationAnalysis.getSlowThresholdMillis());
    }
    
    public double getMinimumSlowFrac() {
        return operationAnalysis.getMinimumSlowFrac();
    }
    
    public List getProblemRequests() {
        return operationAnalysis.getProblemRequests();
    }

    public boolean isFailing() {
        return operationAnalysis.isFailing();
    }
    
   public boolean isSlow() {
       return operationAnalysis.isSlow();
   }
   
   public boolean isNormal() {
       if(isSlow() || isFailing())
           return false;
       return true;
   }
   
   public int getFailingOperations() {
       return getScenarioSummary().getScenario(OperationPerfStats.FAILURE_SCENARIO).getCount();
   }
   
   public String getFailingOperationsAverageTime() {
       return TimeConversion.formatMeanNanos(getScenarioSummary().getScenario(
               OperationPerfStats.FAILURE_SCENARIO).getAccumulatedTime(), getFailingOperations());
   }
   
   public long getFailingOperationsPercentage() {
       if(getTotalOperations() == 0) return 0;
       return (100 *getFailingOperations())/getTotalOperations();
   }
   
   public int getSlowOperations() {
       return getScenarioSummary().getScenario(OperationPerfStats.SLOW_SCENARIO).getCount();
   }

   public String getSlowOperationsAverageTime() {
       return TimeConversion.formatMeanNanos(getScenarioSummary().getScenario(
               OperationPerfStats.SLOW_SCENARIO).getAccumulatedTime(), getSlowOperations());
   }
   
   /** @return time in nanos */ 
   public long getSlowOperationsAverageTimeNanos() {
       return (getScenarioSummary().getScenario(OperationPerfStats.SLOW_SCENARIO). getAccumulatedTime()/getSlowOperations());
   }
   
   public long getSlowOperationsPercentage() {       
       if(getTotalOperations() == 0) return 0;       
       return (100 * getSlowOperations())/getTotalOperations();        
   }
   
   public int getTotalOperations() {
       return getSlowOperations() + getNormalOperations() + getFailingOperations();
   }
   
   public int getNormalOperations() {
       return getScenarioSummary().getScenario(OperationPerfStats.NORMAL_SCENARIO).getCount();
   }
  
   public String getNormalOperationsAverageTime() {
       return TimeConversion.formatMeanNanos(getScenarioSummary().getScenario(
               OperationPerfStats.NORMAL_SCENARIO).getAccumulatedTime(), getNormalOperations());
   }
   
   public String getOperationStatus() {
       if(isFailing()) {
           return MessageFormat.format(MessageHelper.getString("operation.status.fail"), 
                   new Object[]{String.valueOf(getFailingOperations()), String.valueOf(getFailingOperationsPercentage())});    
       } else if(isSlow()) {           
           return MessageFormat.format(MessageHelper.getString("operation.status.slow"), 
                   new Object[]{String.valueOf(getSlowOperations()), String.valueOf(getSlowOperationsPercentage())});
       } else {
           return MessageFormat.format(MessageHelper.getString("operation.status.ok"), 
                   new Object[]{new Double(operationAnalysis.getSlowThresholdMillis()*.001)});
       }
   }

   public DisplayHelper getDisplayHelper(OperationData data) {
       return new DisplayHelper(data, this);
   }       
}