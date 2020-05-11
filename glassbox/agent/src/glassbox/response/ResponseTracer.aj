/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the 
 * terms of the
 * Lesser Gnu Public License v2.1, which accompanies this distribution and is available at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.summary.StatsSummarizer;
import glassbox.track.api.FailureDescription;
import glassbox.track.api.PerfStats;
import glassbox.util.logging.api.LogManagement;
import glassbox.util.timing.api.TimeConversion;

public aspect ResponseTracer {

    private static final boolean enabled = Boolean.getBoolean("glassbox.response.trace");
    
    declare precedence: StatsSummarizer.SummarizeTopLevelStats, ResponseTracer;

    private ThreadLocal indentHolder = new ThreadLocal() {
        public Object initialValue() {
            return new int[] { 0 };
        }
    };

    after(PerfStats stats, Response response, StatsSummarizer summarizer) returning: 
      StatsSummarizer.startStats(stats, response, summarizer) && if (enabled) {
        String extra = "";
        if(stats.isOperationKey()) {
            extra = ", may be opkey";
        }
        if (summarizer.getPrimaryOperationStats()!=null) {
            extra += ", existing primary";
        }
        logInfo(indent() + "Started response " + response.getKey() + " on "+ Thread.currentThread() + extra);
        logDebug("app = "+response.getApplication());
    }

    after(Response response, StatsSummarizer summarizer) returning: 
      StatsSummarizer.endTopLevelStats(*, response, summarizer) && if (enabled) {
        Object pk = (summarizer.getPrimaryOperationStats() == null ? null : summarizer.getPrimaryOperationStats()
                .getKey());

        logInfo(unindent() + "Finished response " + response.getKey() + ", primary operation = " + pk
                + ", elapsed = " + TimeConversion.formatTime(response.getDuration())+" on "+Thread.currentThread());
        logDebug("app = "+response.getApplication());
    }

    after(PerfStats stats, Response response, StatsSummarizer summarizer) returning: 
      StatsSummarizer.endNestedStats(stats, response, summarizer) && if (enabled) {
        int[] indent = (int[]) indentHolder.get();
        String opStatus = "";
        if (stats.isOperationThisRequest()) {
            if (stats == summarizer.getPrimaryOperationStats()) {
                opStatus = " (primary operation key)";
            } else {
                opStatus = " (secondary operation key)";
            }
        }
        FailureDescription failure = (FailureDescription)response.get(Response.FAILURE_DATA);
        if (failure != null) {
            opStatus = opStatus + ", failed: "+failure.getSummary();
        }
                
        logInfo(unindent() + "Finished response " + response.getKey() + opStatus + ", elapsed = "
                + TimeConversion.formatTime(response.getDuration()));
        logDebug("app = "+response.getApplication());
    }

    private static aspect DebugData {
        private static final boolean enabled = ResponseTracer.enabled && aspectOf().isDebugEnabled();
        
        after() returning: adviceexecution() && within(ResponseTracer) && !within(DebugData) && if(enabled) {
            logDebug("on " + Thread.currentThread().getName(), new Throwable());
        }
        
    }

    private String indent() {
        int[] indent = (int[]) indentHolder.get();
        return spaces(indent[0]++);
    }

    private String unindent() {
        int[] indent = (int[]) indentHolder.get();
        return spaces(--indent[0]);
    }

    private String spaces(int nspaces) {
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < nspaces; i++) {
            spaces.append(' ');
        }
        return spaces.toString();
    }
}
