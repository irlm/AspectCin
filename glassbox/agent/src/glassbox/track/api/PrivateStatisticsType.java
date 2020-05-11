/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;

public class PrivateStatisticsType extends StatisticsTypeImpl {

    public static final PrivateStatisticsType instance = new PrivateStatisticsType();
    private static final long serialVersionUID = 1L;
    
    private PrivateStatisticsType() {}
    
    public int getIndex() {
        return -1; // not in maps...
    }

    public String getLayer() {
        return "internal";
    }

    protected PerfStatsImpl createPerfStats(Serializable key) {
        return new PerfStatsImpl(true);
    }

}
