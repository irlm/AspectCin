/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import glassbox.agent.api.NotSerializable;
import glassbox.analysis.api.*;
import glassbox.track.api.*;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class OperationAnalyzer {

    public static final long DEFAULT_LOW_INITIAL_MEMORY = 256 * 1024 * 1024;

    private static final long MINIMUM_NANOS_FOR_DETAILS = TimeConversion.NANOSECONDS_PER_MILLISECOND / 10L; // 0.1 ms

    private double minimumSlowFrac = 0.05;
    private double excessCpuFrac = 0.5;

    private long slowThresholdNanos = 1000 * TimeConversion.NANOSECONDS_PER_MILLISECOND;

    private ProblemFactory problemFactory;
    
    // define this internally so we don't depend on Java 5
    private static interface Iterable {
        public Iterator/* <PerfStats> */ iterator();
    }

    private static final long serialVersionUID = 2L;

    public void setMinimumSlowFrac(double value) {
        minimumSlowFrac = value;
    }

    public void setSlowThresholdMillis(long value) {
        setSlowThresholdNanos(value * TimeConversion.NANOSECONDS_PER_MILLISECOND);
    }

    public void setExcessCpuFrac(double value) {
        excessCpuFrac = value;
    }

    public double getMinimumSlowFrac() {
        return minimumSlowFrac;
    }

    public double getExcessCpuFrac() {
        return excessCpuFrac;
    }

    public long getSlowThresholdMillis() {
        return TimeConversion.convertNanosToMillis(slowThresholdNanos);
    }

    /** threshold for slow, in nanoseconds */
    public void setSlowThresholdNanos(long slowThresholdNanos) {
        this.slowThresholdNanos = slowThresholdNanos;
    }

    public long getSlowThresholdNanos() {
        return slowThresholdNanos;
    }

    public boolean isSlowSingleRequest(PerfStats stats, int totalCount) {
        return isSlow(stats.getSlowSingleOperationCount(), totalCount);
    }

    public boolean isSlowScenarioStats(OperationPerfStats stats) {
        return isSlow(stats.getScenarioStats(OperationPerfStats.SLOW_SCENARIO).getCount(), stats.getOperationCount());
    }

    public boolean isSlow(PerfStats stats, int totalCount) {
        return isSlow(stats.getSlowCount(), totalCount);
    }

    public boolean isSlow(int slowCount, int totalCount) {
        return slowCount > 0 && slowCount >= (totalCount * getMinimumSlowFrac());
    }

    static public class DiagnosisImportanceComparator implements Comparator/* <String> */ {

        final String[] order = { OperationSummary.FAIL_PROCESSING, OperationSummary.FAIL_DATABASE_CONNECTION,
                OperationSummary.FAIL_DATABASE_STATEMENT, OperationSummary.FAIL_REMOTE_CALL,
                OperationSummary.SLOW_DATABASE, OperationSummary.SLOW_REMOTE_CALL,
                OperationSummary.SLOW_REMOTE_OVERALL, OperationSummary.SINGLE_THREAD_QUEUE,
                OperationSummary.SLOW_METHOD, OperationSummary.EXCESS_CPU, OperationSummary.EXCESS_WORK,
                OperationSummary.OK, };

        final Map/* <String, Integer> */ orderMap = new HashMap/* <String, Integer> */(order.length * 4 + 1);

        public DiagnosisImportanceComparator() {
            for (int i = 0; i < order.length; i++) {
                orderMap.put(order[i], new Integer(i));
            }
        }

        public int compare(Object o1, Object o2) {
            return compare((String) o1, (String) o2);
        }
        public int compare(String o1, String o2) {
            return rank(o1) - rank(o2);
        }

        int rank(String diag) {
            Integer rank = (Integer) orderMap.get(diag);
            return (rank == null ? order.length : rank.intValue());
        }

    };

    static public DiagnosisImportanceComparator diagnosisImportanceComparator = new DiagnosisImportanceComparator();

    public OperationSummary summarize(OperationDescription operation, OperationPerfStats stats) {
        List/* <String> */ diagnoses = new ArrayList/* <String> */();
        boolean hasFailed = false;
        boolean wasSlow = isSlowScenarioStats(stats);

        hasFailed |= summarizeDatabase(stats, diagnoses);
        hasFailed |= summarizeRemoteCalls(stats, diagnoses);
        hasFailed |= summarizeContention(stats, diagnoses);
        summarizeCpuUsage(stats, diagnoses);
        hasFailed |= summarizeDispatch(stats, diagnoses);
        
        if (!hasFailed && (stats.getOperationStats().getFailingOperationCount() > 0)) {
            hasFailed = true;
            diagnoses.add(0, OperationSummary.FAIL_PROCESSING);
        }

        int statusCode = OperationSummary.StatusOK;
        if (hasFailed) {
            statusCode = OperationSummary.StatusFailing;
            // don't report slow problems if only slow when failing!
            if (!wasSlow) {
                for (Iterator iter = diagnoses.iterator(); iter.hasNext();) {
                    String problemCode = (String) iter.next();
                    if (!OperationSummaryImpl.Util.isFailureProblem(problemCode)) {
                        iter.remove();
                    }
                }
            }
        } else {
            if (wasSlow) {
                statusCode = OperationSummary.StatusSlow;
            } else if (diagnoses.size() > 0) {
                // this condition can happen when >1 operation roll up into a common containing operation:
                // we might detect contention in the parent when it is only contention for a sibling
                // this should be fixed by recording summarized stats at each level
                if (!isSlowScenarioStats(stats)) {
                    logDebug("Pre-existing diagnoses found in case with OK status. Diagnoses list: "
                            + diagnoses.toString());
                }
                diagnoses.clear();
            }
        }

        if (statusCode != OperationSummary.StatusOK && diagnoses.size() == 0) {
            // put in low priority items if nothing specific found...
            if (statusCode == OperationSummary.StatusSlow) {
                if (diagnoses.size() == 0) {
                    summarizeSlowMethod(stats, diagnoses);
                    if (diagnoses.size() == 0) {
                        diagnoses.add(OperationSummary.EXCESS_WORK);
                    }
                }
            } else {
                diagnoses.add(0, OperationSummary.FAIL_PROCESSING);
            }
        } else if (statusCode == OperationSummary.StatusOK) {
            diagnoses.add(OperationSummary.OK);
            if (diagnoses.size() != 1) {
                logError("Pre-existing diagnoses found in case with OK status. Diagnoses list now: "
                        + diagnoses.toString());
            }
        }
        Collections.sort(diagnoses, diagnosisImportanceComparator);
        return new OperationSummaryImpl(operation, stats.getOperationCount(), stats.getOperationTime(), statusCode, diagnoses, wasSlow);
    }

    /**
     * 
     * @return true if the operation has ever failed in the database
     */
    private boolean summarizeDatabase(OperationPerfStats stats, List/* <String> */ diagnoses) {
        PerfStats totalDbStats = stats.getResourceStats(StatisticsType.DatabaseIdx);
        if (isSlow(totalDbStats, stats.getOperationCount())) {
            diagnoses.add(OperationSummary.SLOW_DATABASE);
        }

        if (totalDbStats.getFailureCount() == 0) {
            return false;
        }
        
        boolean connFailure = false;
        boolean stmtFailure = false;
        for (Iterator it = stats.getEntriesForType(StatisticsTypeImpl.Database); it.hasNext();) {
            Entry entry = (Entry) it.next();
            CompositePerfStats dbStats = (CompositePerfStats) entry.getValue();
            PerfStats connStats = dbStats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null);
            if (connStats.getFailureCount() > 0) {
                connFailure = true;
            }
            for (Iterator sit = dbStats.getEntriesForType(StatisticsTypeImpl.DatabaseStatement); sit.hasNext();) {
                Entry stmtEntry = (Entry) sit.next();
                PerfStats stmtStats = (PerfStats) stmtEntry.getValue();
                if (stmtStats.getFailureCount() > 0) {
                    stmtFailure = true;
                    // System.err.println("Found stmtStats with a failure");
                }
            }
        }
        if (connFailure) {
            diagnoses.add(0, OperationSummary.FAIL_DATABASE_CONNECTION);
        }
        if (stmtFailure) {
            diagnoses.add(0, OperationSummary.FAIL_DATABASE_STATEMENT);
        }
        return connFailure || stmtFailure;
    }

    /**
     * 
     * @return true if the operation has ever failed in a remote call
     */
    private boolean summarizeRemoteCalls(OperationPerfStats stats, List/* <String> */ diagnoses) {
        PerfStats totalRemoteCallStats = stats.getResourceStats(StatisticsType.RemoteCallIdx);
        if (isSlow(totalRemoteCallStats, stats.getOperationCount())) {
            diagnoses.add(OperationSummary.SLOW_REMOTE_CALL);
        }

        if (totalRemoteCallStats.getFailureCount() == 0) {
            return false;
        }
        
        boolean failure = false;
        for (Iterator it = stats.getEntriesForType(StatisticsTypeImpl.RemoteCall); it.hasNext();) {
            Entry entry = (Entry) it.next();
            PerfStats callStats = (PerfStats) entry.getValue();
            if (callStats.getFailureCount() > 0) {
                failure = true;
                break;
            }
        }

        if (failure) {
            diagnoses.add(0, OperationSummary.FAIL_REMOTE_CALL);
            return true;
        }
        return false;
    }

    /**
     * 
     * @return true if the operation has ever failed from contention
     */
    private boolean summarizeContention(OperationPerfStats stats, List/* <String> */ diagnoses) {
        stats = getSampledStats(stats);
        if (isSlow(stats.getResourceStats(StatisticsType.ContentionIdx), stats.getOperationCount())) {
            diagnoses.add(OperationSummary.SINGLE_THREAD_QUEUE);
        }
        return false;
    }
    
    private boolean summarizeDispatch(OperationPerfStats stats, List/* <String> */ diagnoses) {
        if (stats.isSlowDispatch()) {
            diagnoses.add(OperationSummary.SLOW_DISPATCH);
        }
        if (stats.isFailingDispatch()) {
            diagnoses.add(0, OperationSummary.FAILING_DISPATCH);
            return true;
        }
        return false;
    }
    
    private void summarizeSlowMethod(OperationPerfStats stats, List/* <String> */ diagnoses) {
        stats = getSampledStats(stats);
        Iterator it = stats.getDirectEntriesForType(StatisticsTypeImpl.SlowMethod);
        if (it.hasNext()) {
            Entry entry = (Entry) it.next();
            TreeTimeStats timeStats = (TreeTimeStats) entry.getValue();

            if (timeStats.hasSlowestSignficant(stats.getOperationCount(), getMinimumSlowFrac())) {
                diagnoses.add(OperationSummary.SLOW_METHOD);
            }
        }
    }

    private void summarizeCpuUsage(OperationPerfStats stats, List/* <String> */ diagnoses) {
        PerfStats cpuStats = getCpuStats(stats);
        if (cpuStats!=null && isSlow(cpuStats, stats.getOperationCount())) {
            diagnoses.add(OperationSummary.EXCESS_CPU);
        }
    }

    /**
     * 
     * @param operation
     * @param stats
     * @return a ProblemAnalysis. If the given operation isn't a problem, this may have little data.
     */
    public OperationAnalysis analyze(OperationDescription operation, OperationPerfStats stats, long monstart) {
        OperationSummary summary = summarize(operation, stats);
        
        OperationAnalysisImpl analysis = new OperationAnalysisImpl(summary, getScenarioSummary(stats), createComponentDecomposition(stats), 
                createResourceDecomposition(stats), stats.getProblemRequests(), monstart, getMeanCpuTime(stats));
        List/* <ProblemAnalysis> */ problems = analysis.problems();

        analysis.setSlowThresholdMillis(getSlowThresholdMillis());
        analysis.setMinimumSlowFrac(getMinimumSlowFrac());

        analyzeDatabase(operation, stats, problems);
        analyzeRemoteCalls(operation, stats, problems);
        analyzeFailures(operation, stats, problems);
        
        boolean wasSlow = isSlowScenarioStats(stats);
        if (wasSlow) {
            analyzeContention(operation, stats, problems);
            analyzeCpuUsage(operation, stats, problems);
            analyzeSlowDispatch(operation, stats, problems);
            
            // problem ordering matters: these problems need to be listed last so they are detected and reported correctly
            // if there are other causes of slowness
            analyzeSlowMethod(operation, stats, problems);            
            if (summary.statusCode() == OperationSummary.StatusSlow && problems.size() == 0) {
                problems.add(new ExcessWorkProblem(stats.getOperationTime(), stats.getOperationCount())); // catch-all
            }
        } else {
            // don't include slow analyses if ONLY slow when failing
            for (Iterator probs=problems.iterator(); probs.hasNext();) {
                Object prob = probs.next();
                if (prob instanceof SlowProblem) {
                    probs.remove();
                }
            }
        }

        //orderProblems(problems);
        return analysis;
    }
    
    private void analyzeSlowDispatch(OperationDescription operation, OperationPerfStats stats, List/* <ProblemAnalysis> */ problems) {
        if (stats.isSlowDispatch()) {
            problems.add(new SlowDispatchProblem(stats.getDispatchTime(), stats.getOperationCount()));
        }
    }
    
    private void analyzeFailures(OperationDescription operation, OperationPerfStats stats, List/* <ProblemAnalysis> */ problems) {
        ProblemAnalysis problem = problemFactory.makeFailureProblem(stats);
        if (problem != null) {
            problems.add(problem);
        }
        
        if (stats.isFailingDispatch()) {
            problem = problemFactory.makeFailureProblem(stats.getOperationStats());
            if (problem == null) {
                // unknown/lost failure...
                problem = new DefaultFailureProblem(stats.getCallDescription(), stats.getOperationStats().getFailureCount(), Collections.EMPTY_LIST);
            }
            problems.add(problem);
        }
    }

    /**
     * This method will enforce 2 guarantees:
     * 1. problems will be grouped by type, i.e., all problems of a given type will be contiguous
     * 2. problems will be ranked in descending order of importance, i.e., the worst problem type will go first
     * 
     * The right way to do this is to list problem TYPES and then have 1 or more instances of each
     * instead of having a list of multiple problems where types need to be grouped
     */
//    private void orderProblems(List problems) {
//        // sort in overall order
//        Collections.sort(problems, new OperationAnalysisComparator());
//        
//        // now move any problems of different types up to follow their first sibling 
//        for (int i=0; i<problems.size(); i++) {
//            for(;;) {
//                int j=i+1;
//                for (; j<problems.size() && problems.get(j).getClass() == problems.get(i).getClass(); j++) {
//                }
//                for (; j<problems.size() && problems.get(j).getClass() != problems.get(i).getClass(); j++) {
//                }
//                if (j==problems.size()) {
//                    break;
//                }
//                problems.
//            }
//        }
//        
//    }

    private PerfStats getCpuStats(OperationPerfStats stats) {
        TreeTimeStats timeStats = (TreeTimeStats)getSampledStats(stats).getPerfStats(StatisticsType.SlowMethodIdx, StatisticsType.SlowMethodKey);
        if (timeStats == null) {
            return null;
        }
        return timeStats.getCpuStats();
    }
    
    private double getMeanCpuTime(OperationPerfStats stats) {
        PerfStats cpuStats = getCpuStats(stats);
        if (cpuStats == null) return 0.;
        return cpuStats.getMeanTime();
    }

    private TimeDecomposition createComponentDecomposition(OperationPerfStats stats) {
        DefaultTimeDecomposition componentDecomposition = new DefaultTimeDecomposition();

        setPart(componentDecomposition, TimeDecomposition.DATABASE_ACCESS, stats.getResourceStats(StatisticsType.DatabaseIdx), stats);
        setPart(componentDecomposition, TimeDecomposition.REMOTE_CALLS, stats.getResourceStats(StatisticsType.RemoteCallIdx), stats);
        setPart(componentDecomposition, TimeDecomposition.OTHER_PROCESSING, stats.getNonResourceStats(), stats);

        return componentDecomposition;
    }
    
    private FrequencySummaryStats setPart(DefaultTimeDecomposition decomposition, int index, PerfStats partStats, OperationPerfStats topStats) {
        FrequencySummaryStats freqStats;
        if (partStats == null) {
            freqStats = new FrequencySummaryStats(topStats.getCount(), 0, 0L);
        } else {
            boolean failing = (topStats.getOperationStats().getFailingOperationCount()>0);
            freqStats = new FrequencySummaryStats(topStats.getOperationCount(), (failing ? partStats.getFailingOperationCount() : partStats.getSlowSingleOperationCount()), partStats.getAccumulatedTime()); 
        }
        decomposition.setPart(index, freqStats);
        return freqStats;
    }

    private TimeDecomposition createResourceDecomposition(OperationPerfStats stats) {
        DefaultTimeDecomposition resourceDecomposition = new DefaultTimeDecomposition();
        
        TreeTimeStats timeStats = (TreeTimeStats)getSampledStats(stats).getPerfStats(StatisticsType.SlowMethodIdx, StatisticsType.SlowMethodKey);        
        if (timeStats != null) {
            setPart(resourceDecomposition, TimeDecomposition.RUNNABLE_JAVA, timeStats.getStatsForState(ThreadState.RUNNABLE_JAVA), stats);
            setPart(resourceDecomposition, TimeDecomposition.RUNNABLE_NATIVE, timeStats.getStatsForState(ThreadState.RUNNABLE_NATIVE), stats);
            setPart(resourceDecomposition, TimeDecomposition.THREAD_WAIT, timeStats.getStatsForState(ThreadState.WAITING), stats);
        }
        setPart(resourceDecomposition, TimeDecomposition.BLOCKED, stats.getResourceStats(StatisticsType.ContentionIdx), stats);
        
        return resourceDecomposition;
    }    
    
    private static ScenarioSummary getScenarioSummary(OperationPerfStats stats) {
        SummaryStats scenarios[] = new SummaryStats[OperationPerfStats.NUMBER_OF_SCENARIOS];
        
        for (int i=0; i<OperationPerfStats.NUMBER_OF_SCENARIOS; i++) {
            PerfStats sourceStats = stats.getScenarioStats(i);
            scenarios[i] = new SummaryStats(sourceStats.getCount(), sourceStats.getAccumulatedTime());
        }

        return new ScenarioSummary(stats.getOperationCount(), stats.getOperationTime(), scenarios);
    }

    private boolean hasProcessingFailure(OperationSummary summary) {
        return (summary.statusCode() == OperationSummary.StatusFailing)
                && summary.analysisFindings().contains(OperationSummary.FAIL_PROCESSING);
    }

    private void analyzeDatabase(OperationDescription operation, final OperationPerfStats stats,
            List/* <ProblemAnalysis> */ problems) {
        int opCount = stats.getOperationCount();
        
        Iterable allDatabases = new Iterable() {
            public Iterator/* <PerfStats> */ iterator() {
                return stats.getEntriesForType(StatisticsTypeImpl.Database);
            }
        };

        // this SHOULD use virtual composite stats, so we don't double count slow connection & slow statement!
        PerfStats overallDbStats = stats.getResourceStats(StatisticsType.DatabaseIdx);
        
        analyzeAllDatabaseCalls(allDatabases.iterator(), new FailingCallAccumulator(), problems);
        
        if (isSlow(overallDbStats, opCount)) {
            List/* <Serializable> */ slowDatabases = new LinkedList/* <Serializable> */();
            ArrayList/* <Serializable> */ dbKeys = new ArrayList/* <Serializable> */();

            checkDatabaseInstances(opCount, allDatabases, slowDatabases, dbKeys);

            ArrayList/* <CallDescription> */ dbCalls = new ArrayList/* <CallDescription> */();
            if (slowDatabases.isEmpty()) {
                analyzeAllDatabaseCalls(allDatabases.iterator(), new NoticeableTimeCallAccumulator(opCount), dbCalls);
                problems.add(problemFactory.makeSlowDatabaseOverallProblem(dbCalls, dbKeys, overallDbStats, opCount));
            } else {
                //this could be optimized by storing the slowDatabase stats above so we could only iterate on
                //that subset
                if (!analyzeAllDatabaseCalls(allDatabases.iterator(), new SlowDatabaseCallAccumulator(opCount), problems)) {
                    // doesn't have a single slow call
                    addSlowSingleDatabases(problems, opCount, allDatabases.iterator(), dbCalls);
                }
            }
        }
    }

    private void checkDatabaseInstances(int opCount, Iterable allDatabases, List/* <Serializable> */ slowDatabases, ArrayList/* <Serializable> */ dbKeys) {
        for (Iterator it = allDatabases.iterator(); it.hasNext();) {
            Entry databaseEntry = (Entry) it.next();
            PerfStats databaseStats = (PerfStats) databaseEntry.getValue();
            Serializable databaseKey = databaseStats.getKey();
            if (isSlow(databaseStats, opCount)) {
                slowDatabases.add(databaseKey);
            } else {
                dbKeys.add(databaseKey);
            }
        }
    }

    private void addSlowSingleDatabases(List/* <ProblemAnalysis> */ problems, int opCount, Iterator it, ArrayList/* <CallDescription> */ dbCalls) {
        for (; it.hasNext();) {
            dbCalls.clear();

            Entry databaseEntry = (Entry) it.next();
            CompositePerfStats databaseStats = (CompositePerfStats) databaseEntry.getValue();
            analyzeSingleDatabase(databaseStats, new NoticeableTimeCallAccumulator(opCount), dbCalls);
            problems.add(problemFactory.makeSlowSingleDatabaseProblem(dbCalls, databaseStats, opCount));
        }
    }
        
    private /* <T> */ boolean analyzeAllDatabaseCalls(Iterator it, CallAccumulator/* <T> */ accumulator, List/* <T> */ matching) {
        boolean matched = false;
        while (it.hasNext()) {
            Entry databaseEntry = (Entry) it.next();
            CompositePerfStats databaseStats = (CompositePerfStats) databaseEntry.getValue();
            matched |= analyzeSingleDatabase(databaseStats, accumulator, matching);
        }
        return matched;
    }

    private /* <T> */ boolean analyzeSingleDatabase(CompositePerfStats databaseStats, CallAccumulator/* <T> */ accumulator, List/* <T> */ matching) {
        boolean matched = false;
        for (Iterator dbCallIt = databaseStats.getEntries(); dbCallIt.hasNext();) {
            Entry databaseCallEntry = (Entry) dbCallIt.next();
            PerfStats databaseCallStats = (PerfStats) databaseCallEntry.getValue();
            Serializable key = (Serializable) databaseCallEntry.getKey();
            if (accumulator.matches(databaseCallStats)) {
                matched = true;
                matching.add(accumulator.map(databaseCallStats));
            }
        }
        return matched;
    }

    private static interface CallAccumulator/* <T> */ extends NotSerializable {
        public boolean matches(PerfStats databaseCallStats);

        public /*T*/ Object map(PerfStats databaseCallStats);
    }

    private class SlowDatabaseCallAccumulator implements CallAccumulator/* <ProblemAnalysis> */ {
        private int opCount;

        public SlowDatabaseCallAccumulator(int opCount) {
            this.opCount = opCount;
        }

        public boolean matches(PerfStats databaseCallStats) {
            return isSlowSingleRequest(databaseCallStats, opCount);
        }

        public Object/*ProblemAnalysis*/ map(PerfStats databaseCallStats) {
            return problemFactory.makeSlowDatabaseCallProblem(databaseCallStats, opCount);
        }
    }

    private class NoticeableTimeCallAccumulator implements CallAccumulator/* <ProblemAnalysis> */ {
        private int opCount;

        public NoticeableTimeCallAccumulator(int opCount) {
            this.opCount = opCount;
        }

        public boolean matches(PerfStats databaseCallStats) {
            return databaseCallStats.getAccumulatedTime() >= opCount * MINIMUM_NANOS_FOR_DETAILS;
        }

        public Object/* ProblemAnalysis */ map(PerfStats databaseCallStats) {
            return problemFactory.makeSlowDatabaseCallProblem(databaseCallStats, opCount);
        }
    }

    private class FailingCallAccumulator implements CallAccumulator/* <ProblemAnalysis> */ {
        public Object/*ProblemAnalysis*/ map(PerfStats databaseCallStats) {
            return problemFactory.makeFailingDbCall(databaseCallStats);
        }

        public boolean matches(PerfStats databaseCallStats) {
            return databaseCallStats.getFailureCount() > 0;
        }
    }

    private void analyzeRemoteCalls(OperationDescription operation, OperationPerfStats stats,
            List/* <ProblemAnalysis> */problems) {
        List/* <ProblemAnalysis> */ remoteCallProblems = new LinkedList/* <ProblemAnalysis> */();
        Set/* <Serializable> */ remoteKeys = new HashSet/* <Serializable> */();
        boolean noSlowSingle = true;
        
        for (Iterator it = stats.getEntriesForType(StatisticsTypeImpl.RemoteCall); it.hasNext();) {
            Entry callEntry = (Entry) it.next();
            PerfStats callStats = (PerfStats) callEntry.getValue();

            if (callStats.getAccumulatedTime() >= stats.getOperationCount() * MINIMUM_NANOS_FOR_DETAILS) {
                ProblemAnalysis slowProblem = problemFactory.makeSlowRemoteCall(callStats, stats.getOperationCount());
                
                if (isSlowSingleRequest(callStats, stats.getOperationCount())) {
                    problems.add(slowProblem);
                    noSlowSingle = false;
                } else if (noSlowSingle) {
                    remoteCallProblems.add(slowProblem);

                    RemoteCallDescription remoteCall = (RemoteCallDescription) callStats.getCallDescription();
                    remoteKeys.add(remoteCall.getResourceKey());                    
                }                    
            }
            ProblemAnalysis problem = problemFactory.makeFailureProblem(callStats);
            if (problem != null) {
                problems.add(problem);
            }
        }
        if (noSlowSingle && isSlow(stats.getResourceStats(StatisticsType.RemoteCallIdx), stats.getOperationCount())) {
            problems.add(problemFactory.makeSlowRemoteOverallProblem(remoteCallProblems, new ArrayList/* <Serializable> */(remoteKeys), stats
                    .getResourceStats(StatisticsType.RemoteCallIdx)));
        }
    }

    private void analyzeContention(OperationDescription operation, OperationPerfStats stats,
            List/* <ProblemAnalysis> */problems) {
        stats = getSampledStats(stats);
        
        int count = stats.getOperationCount();
        if (isSlow(stats.getResourceStats(StatisticsType.ContentionIdx), count)) {
            TreeSet/* <PerfStats> */ orderedEntries = new TreeSet/* <PerfStats> */(new PerfStatsComparator());
            for (Iterator it = stats.getEntriesForType(StatisticsTypeImpl.Contention); it.hasNext();) {
                Entry entry = (Entry) it.next();
                PerfStats contentionStats = (PerfStats) entry.getValue();
                orderedEntries.add(contentionStats);
                if (orderedEntries.size() > 5) {
                    orderedEntries.remove(orderedEntries.last());
                }
            }

            if (orderedEntries.size() == 0) {
                logError("contention observed with no details for operation " + operation);
                return;
            }
            TreeSet/* <ProblemAnalysis> */ orderedProblems = new TreeSet/* <ProblemAnalysis> */(new SlowProblemComparator());
            for (Iterator it = orderedEntries.iterator(); it.hasNext();) {
                PerfStats contentionStats = (PerfStats) it.next();
                orderedProblems.add(problemFactory.makeContentionProblem(contentionStats, count));
            }
            double slowThreshDouble = TimeConversion.convertNanosToDoubleSeconds(slowThresholdNanos);
            if (((ContentionProblem) orderedProblems.first()).getMeanTime() >= slowThreshDouble) {
                for (Iterator it = orderedProblems.iterator(); it.hasNext();) {
                    ContentionProblem problem = (ContentionProblem) it.next();
                    if (problem.getMeanTime() < slowThreshDouble) {
                        break;
                    }
                    problems.add(problem);
                }
            } else {
                for (Iterator it = orderedProblems.iterator(); it.hasNext();) {
                    problems.add((ContentionProblem)it.next());
                }
            }
        }
    }

    private void analyzeSlowMethod(OperationDescription operation, OperationPerfStats stats,
            List/* <ProblemAnalysis> */problems) {
        stats = getSampledStats(stats);
        Iterator it = stats.getDirectEntriesForType(StatisticsTypeImpl.SlowMethod);
        if (it.hasNext()) {
            Entry entry = (Entry) it.next();
            TreeTimeStats timeStats = (TreeTimeStats) entry.getValue();

            SlowRequestDescriptor descriptor = timeStats.getSlowestSignficant(stats.getOperationCount(), getMinimumSlowFrac());
            if (descriptor != null) {
                problems.add(new SlowMethodProblem(descriptor));
            }
        }
    }

    private OperationPerfStats getSampledStats(OperationPerfStats stats) {
        return stats;
//        // contention & method sampled stats are only available on the top-most parent
//        // this should be updated to sample properly on children
//        while (stats.getParent() instanceof OperationPerfStats) {
//            stats = (OperationPerfStats)stats.getParent();
//        }
//        return stats;
    }
    
    // excess CPU could be done on a per request basis too
    private void analyzeCpuUsage(OperationDescription operation, OperationPerfStats stats,
            List/* <ProblemAnalysis> */problems) {
        PerfStats cpuStats = getCpuStats(stats);
        int totalCount = stats.getOperationCount();
        if (cpuStats!=null) {
            int slowCount = cpuStats.getSlowCount(); 
            if (isSlow(slowCount, totalCount)) {
                ExcessCpuProblem xcprob = new ExcessCpuProblem();
                xcprob.setTotalTime(stats.getOperationTime());
                xcprob.setCpuTime(cpuStats.getAccumulatedTime());
                xcprob.setCount(stats.getOperationCount());      
                xcprob.setCausedSlowFrequency(totalCount==0 ? 0 : ((double)slowCount)/((double)(totalCount)));
                problems.add(xcprob);
            }
        }
    }

    public ProblemFactory getProblemFactory() {
        return problemFactory;
    }

    public void setProblemFactory(ProblemFactory problemFactory) {
        this.problemFactory = problemFactory;
    }
}
