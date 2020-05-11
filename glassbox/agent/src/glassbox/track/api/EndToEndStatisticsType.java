/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;
import java.util.List;

public class EndToEndStatisticsType extends StatisticsTypeImpl {
    public static final EndToEndStatisticsType instance = new EndToEndStatisticsType();
    private static final long serialVersionUID = 1L;
    
    private EndToEndStatisticsType() {}
    
    public int getIndex() {
        return -1; // not in maps...
    }

    public String getLayer() {
        return "dispatch";
    }

    protected PerfStatsImpl createPerfStats(Serializable operationKey) {
        return new DispatchFailuresPerfStatsImpl();
    }
    
    public CallDescription getCall(PerfStats stats) {
        return new OperationDescriptionImpl((OperationDescriptionImpl)stats.getKey()) {
            private static final long serialVersionUID = 1L;
            
            public int callType() {
                return DISPATCH;
            }
        };
    }

    private static class DispatchFailuresPerfStatsImpl extends PerfStatsImpl {
        private static final long serialVersionUID = 1L;
        
        public DispatchFailuresPerfStatsImpl() {
            super(true);
        }

        public List getWorstFailures() {
            return super.getWorstFailures();
        }

    }

}
