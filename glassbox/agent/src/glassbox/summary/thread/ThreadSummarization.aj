/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.summary.thread;

import glassbox.monitor.thread.OperationSample;
import glassbox.monitor.thread.ThreadMonitor;
import glassbox.track.api.CompositePerfStats;

public aspect ThreadSummarization {
    
    //making this pointcut use call allows us to integrate with jMock's proxies without load-time weaving
    //we don't summarize for nested stopMonitoring calls ... the !withincode test avoids overhead but cflow would be more general 
    after(CompositePerfStats stats) returning (OperationSample sample): 
      call(* ThreadMonitor.stopMonitoring(..)) && args(.., stats) && if(stats!=null) && !withincode(* ThreadMonitor+.stopMonitoring(..)) {
        summarizer.summarize(sample, stats);
    }
    
    public void setSummarizer(ThreadSummarizer summarizer) {
        if (summarizer == null) {
            throw new IllegalArgumentException("Pass a no-op summarizer or disable ThreadSummarization");
        }
        this.summarizer = summarizer;
    }
    
    public ThreadSummarizer getSummarizer() {
        return summarizer;
    }
    
    private ThreadSummarizer summarizer = new ThreadSummarizerImpl();
    
    private static final long serialVersionUID = 1L;
}
