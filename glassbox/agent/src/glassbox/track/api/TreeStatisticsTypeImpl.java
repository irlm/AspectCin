/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;

public class TreeStatisticsTypeImpl extends StatisticsTypeImpl {

    public TreeStatisticsTypeImpl(int index) {
        super(index);
    }

    protected PerfStatsImpl createPerfStats(Serializable key) {
        return new TreeTimeStatsImpl();
    }
    private static final long serialVersionUID = 1L;
}