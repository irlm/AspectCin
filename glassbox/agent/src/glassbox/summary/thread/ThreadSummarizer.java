/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary.thread;

import glassbox.monitor.thread.OperationSample;
import glassbox.track.api.CompositePerfStats;

public interface ThreadSummarizer {
    void summarize(OperationSample sample, CompositePerfStats stats);
}
