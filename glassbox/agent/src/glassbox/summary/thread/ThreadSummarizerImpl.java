/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary.thread;

import glassbox.monitor.thread.OperationSample;
import glassbox.monitor.thread.ThreadSnapshot;
import glassbox.track.api.*;

import java.util.Iterator;
import java.util.Map.Entry;

public class ThreadSummarizerImpl implements ThreadSummarizer {

    static int MAX_CONTENTION_TRACES = 3;
    
    /**
     * Tracks both slow methods & contention...
     */
    public void summarize(OperationSample sample, CompositePerfStats stats) {
        if (sample != null) {
            synchronized(sample) {
                // in future, we should identify the "baseline" stack trace at this point ...
                summarizeContention(sample, stats);
                TreeTimeStats timeStats = (TreeTimeStats)stats.getPerfStats(StatisticsType.SlowMethodIdx, StatisticsType.SlowMethodKey);
                timeStats.recordSample(sample);
            }
        }
    }
    
    // TODO: update to be more sophisticated in counting locks that are held multiple times concurrently multiple 
    protected void summarizeContention(OperationSample sample, CompositePerfStats stats) {
        ThreadSnapshot curSnap = null;
        ThreadSnapshot lastSnap = null;

        for (Iterator iter = sample.getSnapshots().iterator(); iter.hasNext();) {
            ThreadSnapshot snapshot = (ThreadSnapshot) iter.next();
            if (curSnap == null) {
                if (snapshot.getLockName() != null) {
                    curSnap = snapshot;
                }
            } else if (!curSnap.getState().equals(snapshot.getState())) {
                summarizeLock(sample, stats, curSnap, lastSnap);
                if (snapshot.getLockName() != null) {
                    curSnap = snapshot; 
                } else {
                    curSnap = null;
                }
            }
            lastSnap = snapshot;
        }
        if (curSnap != null) {
            summarizeLock(sample, stats, curSnap, lastSnap);
        }
    }    

    private void summarizeLock(OperationSample sample, CompositePerfStats stats, ThreadSnapshot curSnap, ThreadSnapshot snapshot) {
        ThreadState tState = curSnap.getState();
        PerfStats contentionStats = stats.getPerfStats(StatisticsTypeImpl.Contention, tState);
        contentionStats.recordUsage(curSnap.getUsageInfo().getEventTime(), snapshot.getUsageInfo().getEventTime(), sample.getInterval());        

        // this logic is similar to that of FailureCollection
        if (stats.getDirectCountForType(StatisticsTypeImpl.Contention) > MAX_CONTENTION_TRACES) {
            eliminateLeast(stats, tState);
        }
    }
    
    private void eliminateLeast(CompositePerfStats parent, ThreadState latest) {
        PerfStats least = null; 
        for (Iterator it=parent.getDirectEntriesForType(StatisticsTypeImpl.Contention); it.hasNext();) {
            Entry entry = (Entry)it.next();
            PerfStats stats = (PerfStats)entry.getValue();
            if (stats == null) {
                logError("null stats");
                it.remove();
                continue;
            }
            if (stats.getKey() != latest) {
                if (least==null || (least.getSlowCount()>stats.getSlowCount() || 
                        (least.getSlowCount()==stats.getSlowCount() && least.getAccumulatedTime()>stats.getAccumulatedTime()))) {
                    least = stats;
                }
            }
        }
        if (least != null) {
            parent.removePerfStats(StatisticsTypeImpl.Contention, least.getKey());
        }
    }
   
    private static final long serialVersionUID = 1L;
}
