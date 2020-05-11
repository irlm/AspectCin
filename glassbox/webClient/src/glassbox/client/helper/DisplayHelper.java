/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.OperationAnalysis;
import glassbox.analysis.api.SlowProblem;
import glassbox.client.helper.problems.ProblemHelper;
import glassbox.client.helper.problems.SlowMethodProblemHelper;
import glassbox.client.pojo.OperationAnalysisData;
import glassbox.client.pojo.OperationData;
import glassbox.config.extension.web.api.DefaultPanelKeyFactory;
import glassbox.config.extension.web.api.PanelKeyFactory;
import glassbox.track.api.OperationDescription;
import glassbox.util.timing.api.TimeConversion;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DisplayHelper extends BaseHelper {

    protected OperationData operationData = null;
    protected OperationAnalysisData analysisData = null;
    protected PanelKeyFactory panelKeyFactory;
    protected Object operationFormatter;
    private static final PanelKeyFactory DEFAULT_PANEL_KEY_FACTORY = new DefaultPanelKeyFactory();
    private static final Log log = LogFactory.getLog(DisplayHelper.class);    
    
    public DisplayHelper(OperationData data, OperationAnalysisData analysis) {
        try {
            this.operationData = data;
            this.analysisData = analysis;
            this.panelKeyFactory = getPanelKeyFactory(data.getOperationKey());
            this.operationFormatter = getOperationFormatter(analysis==null ? null : analysis.getOperationAnalysis());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public PanelKeyFactory getPanelKeyFactory(OperationDescription operation) {
        return DEFAULT_PANEL_KEY_FACTORY;
    }
    
    public Object getOperationFormatter(OperationAnalysis analysis) {
        return null;
    }
    
    public Object getOperationFormatter() {
        return operationFormatter;
    }
    
    public OperationAnalysisData getAnalysisData() {
        return analysisData;
    }

    public void setAnalysisData(OperationAnalysisData analysisData) {
        this.analysisData = analysisData;
    }

    public OperationData getOperationData() {
        return operationData;
    }

    public void setOperationData(OperationData operationData) {
        this.operationData = operationData;
    }
    
    /**
     * Filters out auxiliary information: slow java method when other things are slow...
     * the analysis framework should change so that only real problems appear in the problems list
     * and indications/symptoms are tracked separately!
     * Also filters out duplicates of the same type.
     * @return a list with one problem helper per type of top-level core problem
     */
    public List getCoreProblems() {
        List problems = getProblems();
        if (problems == null) {
            log.warn("Unexpected null problems", new Exception());
            return new ArrayList();
        }
        if (problems.size() < 2) {
            return problems;
        }
        
        int slowCount = getSlowProblemCount();
            
        List coreProblems = new ArrayList();
        Class last = null;
        for (Iterator iter = problems.iterator(); iter.hasNext();) {
            ProblemHelper helper = (ProblemHelper) iter.next();
            if (helper == null) {
                log.warn("Unexpected null problem: "+problems, new Exception());
                continue;
            }
            if (helper.getProblem() == null) {
                log.warn("Unexpected null problem: "+helper, new Exception());
                continue;
            }
            // two different problem types might just share the same helper, e.g., the dynamic problem helper!
            if (!((slowCount>1 && helper instanceof SlowMethodProblemHelper) || helper.getProblem().getClass() == last)) {
                last = helper.getProblem().getClass();
                coreProblems.add(helper);
            }
        }
        return coreProblems;
     }
     
    public int getSlowProblemCount() {
        int slowCount = 0;
        for (Iterator iter = analysisData.getProblems().iterator(); iter.hasNext();) {
            Object problem = iter.next();
            if (problem instanceof SlowProblem) {
                slowCount++;
            }
        }
        return slowCount;
    }
    
    public List getProblems() {        
        return new ProblemDisplayHelper(operationData, analysisData).getProblems(getSlowProblemCount());
    }
    
    public String getExecutiveSummaryPanel() {
        return MessageHelper.getString(panelKeyFactory.getExecutiveSummaryPanel());
    }
    
    public String getTechnologySummaryPanel() {
        return MessageHelper.getString(panelKeyFactory.getTechnologySummaryPanel());
    }
    
    public String getTechnologyDetailsPanel() {
        if(analysisData.isFailing()) {
            return MessageHelper.getString(panelKeyFactory.getFailingTechnologyDetailsPanel());
        } else if(analysisData.isSlow()) {
            return MessageHelper.getString(panelKeyFactory.getSlaViolationTechnologyDetailsPanel());
        }
        return MessageHelper.getString(panelKeyFactory.getNormalTechnologyDetailsPanel());
    }
 
    public String getCommonSolutionsPanel() {
        if(analysisData.isNormal()) return MessageHelper.getString("empty.panel");
        return MessageHelper.getString(panelKeyFactory.getCommonSolutionsPanel());
    }
    
    public String getRuledOutPanel() {
        return MessageHelper.getString(panelKeyFactory.getRuledOutPanel());
    }
    
    public String getInputURLPanel() {
        return MessageHelper.getString(panelKeyFactory.getInputURLPanel());
    }
    
    public String getTimeSpentPanel() {
        return MessageHelper.getString(panelKeyFactory.getTimeSpentPanel());
    }
    
    public static String[] getRuledOutProblems() {
        return ProblemDisplayHelper.getRuledOutProblems() ;
   }
        
   public static String getRuledOutProblem(String key) {
        return ProblemDisplayHelper.getRuledOutProblem(key);        
   }
         
   public static String getRuledOutProblemSummary(String key) {
       return ProblemDisplayHelper.getRuledOutProblemSummary(key);       
   } 
   
   public String formatDate(long nanos) {
       return TimeConversion.formatDate(nanos);
   }
   
   public String formatTime(long nanos) {
       return TimeConversion.formatTime(nanos);
   }
   
   public String formatTimeInSeconds(double seconds) {
       return TimeConversion.formatTimeInSeconds(seconds);
   }
   
   public String formatStackTraceLine(int stackTraceLine) {
       if (stackTraceLine <= 0) {
           return "";
       }
       return Integer.toString(stackTraceLine);
   }
}
