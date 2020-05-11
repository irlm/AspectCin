/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import java.io.Serializable;

import glassbox.track.api.StatisticsRegistry;

public aspect TraceAllocations {
    private static final boolean isEnabled = Boolean.getBoolean("glassbox.track.TraceAllocations");
    private static final int FREQUENCY = 100;
    private int counter = 0;

    before(Serializable key, StatisticsRegistry registry) : execution(* StatisticsRegistry+.createPerfStats(..)) && args(.., key) && this(registry) && if(isEnabled) {
        if ((counter++ % FREQUENCY) == 0) {
            getLogger().info("Allocated statistics: "+key+", registry size: "+registry.size());
        }
    }
}
