/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import glassbox.analysis.api.*;
import glassbox.track.api.*;

import java.io.Serializable;
import java.util.*;

public class ProblemFactory {
    public DbFailureProblem makeFailingDbCall(PerfStats stats) {
        if(stats.getWorstFailures() == null || stats.getWorstFailures().size()==0) {
            return null;
        }
        
        List worstFailures = stats.getWorstFailures();
        List analyses = new ArrayList(worstFailures.size());
        for (Iterator it=worstFailures.iterator(); it.hasNext();) {
            FailureStats failureStats = (FailureStats)it.next();
            analyses.add(analyzeDatabaseFailure(failureStats));            
        }
            
        CallDescription aCall = stats.getCallDescription();
        if (aCall.callType() == CallDescription.DATABASE_CONNECTION) {
            return new DbConnectionFailureProblem(aCall, stats.getFailureCount(), analyses);
        } else {
            return new DbStatementFailureProblem(aCall, stats.getFailureCount(), analyses);
        }
    }

    public ProblemAnalysis makeFailureProblem(PerfStats stats) {
        if(stats.getWorstFailures() == null || stats.getWorstFailures().size()==0) {
            return null;
        }
        
        CallDescription aCall = stats.getCallDescription();
        switch (aCall.callType()) {
            case CallDescription.DATABASE_STATEMENT:
            case CallDescription.DATABASE_CONNECTION:
                return makeFailingDbCall(stats);
            case CallDescription.REMOTE_CALL:
                return new RemoteCallFailureProblem(aCall, stats.getFailureCount(), analyzeDefaultFailures(stats.getWorstFailures()));
            case CallDescription.OPERATION_PROCESSING:
                return new OperationProcessingFailureProblem(aCall, stats.getFailureCount(), analyzeDefaultFailures(stats.getWorstFailures()));
            case CallDescription.DISPATCH:
                return new DispatchFailureProblem(aCall, stats.getFailureCount(), analyzeDefaultFailures(stats.getWorstFailures()));
            default:
                return new DefaultFailureProblem(aCall, stats.getFailureCount(), analyzeDefaultFailures(stats.getWorstFailures()));
        }
    }
    
    protected FailureAnalysis analyzeDatabaseFailure(FailureStats failureStats) {
        DefaultFailureAnalysis analysis = new DefaultFailureAnalysis(failureStats);
        List relatedUrls = new LinkedList();
        
        if (failureStats instanceof SQLFailureDescription) {
            SQLFailureDescription sqlFailure = (SQLFailureDescription) failureStats.getFailure();
            int errorCode = sqlFailure.getSQLErrorCode();
            String sqlState = sqlFailure.getSQLState();
            if (errorCode != 0) {
                relatedUrls.add(new String[] {"http://www.google.com/search?q=sql+error+code+"+errorCode, "Search SQL Error Code " + errorCode });
            }
            
            if (sqlState != null && sqlState.length()>0) {
                relatedUrls.add(new String[] {"http://www.google.com/search?q=sqlstate+"+sqlState+"+error", "Search SQL State " + sqlState });
            }
        }
    
        analysis.setRelatedURLs(relatedUrls);
        return analysis;
    }
    
    protected List analyzeDefaultFailures(List worstFailures) {
        List analyses = new ArrayList(worstFailures.size());
        for (Iterator it=worstFailures.iterator(); it.hasNext();) {
            FailureStats failureStats = (FailureStats)it.next();
            analyses.add(analyzeDefaultFailure(failureStats));            
        }
        return analyses;
    }        
    
    protected FailureAnalysis analyzeDefaultFailure(FailureStats failureStats) {
        return new DefaultFailureAnalysis(failureStats);
    }

    public ProblemAnalysis makeContentionProblem(PerfStats contentionStats, int totalCount) {
        SlowRequestDescriptor slowRequestDescriptor = new SlowRequestDescriptor(contentionStats.getAccumulatedTime(),
                (ThreadState)contentionStats.getKey(), totalCount, contentionStats.getSlowSingleOperationCount());
        return new ContentionProblem(slowRequestDescriptor);
    }

    // TODO: use this and not have these useless subtypes as a marker...
    public SingleCallProblem makeSlowCall(PerfStats stats) {
        int opCount = getTopLevelStats(stats).getCount();
        return new SlowCallProblem(stats.getCallDescription(), stats.getSlowestSingleEvents(), stats.getAccumulatedTime(), stats.getCount(), opCount);
    }
    
    private PerfStats getTopLevelStats(PerfStats stats) {
        while (stats.getParent() != null) {
            stats = stats.getParent();
        }
        return stats;
    }

    public SlowRemoteCallProblem makeSlowRemoteCall(PerfStats stats, int opCount) {
        return new SlowRemoteCallProblem(stats.getCallDescription(), stats.getSlowestSingleEvents(), stats.getAccumulatedTime(), stats.getCount(), opCount);
    }

    public SlowDatabaseCallProblem makeSlowDatabaseCallProblem(PerfStats stats, int opCount) {
        return new SlowDatabaseCallProblem(stats.getCallDescription(), stats.getSlowestSingleEvents(), stats.getAccumulatedTime(), stats.getCount(), opCount);
    }

    public SlowDatabaseOverallProblem makeSlowSingleDatabaseProblem(ArrayList dbCalls, PerfStats dbStats, int opCount) {
        List keys = new LinkedList();
        keys.add(dbStats.getKey());
        
        return new SlowDatabaseOverallProblem(dbCalls, keys, dbStats.getAccumulatedTime(), dbStats.getCount(), opCount);
    }

    public SlowDatabaseOverallProblem makeSlowDatabaseOverallProblem(List resourceCalls, List resourceKeys, PerfStats resourceStats, int opCount) {
        return new SlowDatabaseOverallProblem(resourceCalls, resourceKeys, resourceStats.getAccumulatedTime(), resourceStats.getCount(), opCount);
    }

    public SlowRemoteOverallProblem makeSlowRemoteOverallProblem(List resourceCalls, List resourceKeys, PerfStats resourceStats) {
        int opCount = getTopLevelStats(resourceStats).getCount();
        
        return new SlowRemoteOverallProblem(resourceCalls, resourceKeys, resourceStats.getAccumulatedTime(), resourceStats.getCount(), opCount);
    }

//    private ProblemAnalysis makeProblem(Class problemClass, PerfStats stats) {
//        Class[] parameters = { CallDescription.class, Integer.TYPE, List.class };  
//        Constructor ctor = problemClass.getConstructor(parameters);
//        
//        return (ProblemAnalysis)ctor.newInstance(new Object[] { stats.getCallDescription(), new Integer(stats.getFailureCount()), stats.getWorstFailures() });
//    }
}
