/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.DispatchFailureProblem;
import glassbox.analysis.api.*;
import glassbox.client.helper.problems.*;
import glassbox.client.pojo.OperationAnalysisData;
import glassbox.client.pojo.OperationData;

import java.io.Serializable;
import java.util.*;

public class ProblemDisplayHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private OperationData operationData = null;
    private OperationAnalysisData analysisData = null;
    
    protected static String[] problemKeys = new String[] {
        "fail.database.connection",
        "fail.database.statement",
        "single.threaded.queue",
        "fail.remote.call",
        "excess.cpu",
        "excess.work", 
        "fail.processing",
        "slow.database",
        "slow.database.overall",           
        "slow.remote.call",
        "slow.remote.overall",
        "slow.dispatch",
        "fail.dispatch"
    };    
    
    public ProblemDisplayHelper(OperationData operationData, OperationAnalysisData analysisData) {      
        this.operationData = operationData;
        this.analysisData = analysisData;
    }
    
    public List getProblems(int slowCount) {        
        ArrayList problems = new ArrayList();
        
        Iterator problemsIt = analysisData.getProblems().iterator();
        while(problemsIt.hasNext()) {
            ProblemAnalysis analysis = (ProblemAnalysis)problemsIt.next();
            
            List problemsOfType = analysisData.getOperationAnalysis().getProblemsOfType(analysis.getClass());
            problems.add(getProblemHelper(analysis, problemsOfType, slowCount));
        }        
        return problems;
    }
    
    protected ProblemHelper getProblemHelper(ProblemAnalysis problem, List problemsOfType, int slowCount) {
        ProblemHelper ph = getProblemHelper(problem);
        //XXX refactor
        if (ph instanceof BaseProblemHelper) {
            BaseProblemHelper bph = (BaseProblemHelper)ph;
            bph.setOperationData(operationData);
            bph.setAnalysisData(analysisData);
            bph.setProblem(problem);
            bph.setProblemsOfType(problemsOfType);
            bph.setSlowProblemCount(slowCount);
        } else if (ph instanceof DynamicProblemHelper) {
            DynamicProblemHelper dph = (DynamicProblemHelper)ph;
            dph.setOperation(analysisData.getSummary().getOperation());
            dph.setAnalysisData(analysisData);
            dph.setProblem(problem);
            dph.setProblemsOfType(problemsOfType);
            dph.setSlowProblemCount(slowCount);
            dph.setMessageHelper(
                new DynamicProblemHelper.MessageHelper() {
                    public String getString(String key) {
                        return MessageHelper.getString(key);
                    }
                    public Enumeration getKeys() {
                        return MessageHelper.getKeys();
                    }
                });
        } else {
            System.err.println("no dice "+ph.getClass().getClassLoader()+" vs "+DynamicProblemHelper.class.getClassLoader());
        }
        return ph;
    }
    
    public ProblemHelper getProblemHelper(ProblemAnalysis problem) {
        if(problem instanceof SlowDatabaseCallProblem) {
            return new SlowDatabaseCallHelper();
        } else if(problem instanceof SlowDatabaseOverallProblem) {
            return new SlowDatabaseCallOverallHelper();
        } else if(problem instanceof SlowRemoteCallProblem) {
            return new SlowRemoteCallHelper();
        } else if(problem instanceof SlowRemoteOverallProblem) {
            return new SlowRemoteCallOverallHelper();
        } else if(problem instanceof RemoteCallFailureProblem) {
            return new RemoteCallFailureHelper();
        } else if(problem instanceof DbStatementFailureProblem) {
            return new DatabaseStatementFailureHelper();
        } else if(problem instanceof DbConnectionFailureProblem) {
            return new DatabaseConnectionFailureHelper();
        } else if(problem instanceof ContentionProblem) {
            return new ContentionProblemHelper();
        } else if(problem instanceof ExcessCpuProblem) {
            return new ExcessCpuProblemHelper();
        } else if(problem instanceof DispatchFailureProblem) {
            return new FailedDispatchHelper();
        } else if(problem instanceof SlowDispatchProblem) {
            return new SlowDispatchProblemHelper();
        } else if(problem instanceof SlowMethodProblem) {             
            return new SlowMethodProblemHelper();
        } else if(problem instanceof AbstractSlowProcessingProblem) {
            return new ExcessWorkProblemHelper();
        } else if(problem instanceof DefaultFailureProblem) {
            return new FailedProcessingHelper();
        } else {
            return null;
        }       
    }        
    
   public static String[] getRuledOutProblems() {
        return getAllProblemKeys();
   }
   
   public static String[] getAllProblemKeys() {
       return problemKeys;
   }
        
   public static String getRuledOutProblem(String key) {
        return MessageHelper.getString("ruled.out.problem." + key);
   }
         
   public static String getRuledOutProblemSummary(String key) {
        return MessageHelper.getString("summary.extra." + key);
   } 
}
