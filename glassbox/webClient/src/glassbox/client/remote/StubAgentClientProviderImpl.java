/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;


import glassbox.analysis.api.*;
import glassbox.track.api.OperationDescription;

import java.util.List;
import java.util.Set;

public class StubAgentClientProviderImpl implements AgentClientProvider {

    public String getAgentInstanceName() {
        return "STUB Agent";
    }

    public Set selectOperations() {

        return null;
    }
    
    public ConfigurationSummary selectConfiguration() {

        return null;
    }
    
    public void resetStatistics() {
    }

    public void setActive(final boolean active) {        
    }

    public boolean isActive() {
        return true;
    }
    
    public boolean getLastFailed() {
        return false;
    }

    public int getState() {
        return NO_REQUEST;
    }

    /* (non-Javadoc)
     * @see glassbox.client.remote.AgentClientProvider#getConnectionURL()
     */
    public String getConnectionURL() {
        return null;
    }

    public OperationAnalysis findOperationAnalysis(OperationDescription operation) {

        return null;
    }
    
    public String getAgentInstanceDescription() {
        return null;
    }


    class OperationSummarySample implements OperationSummary {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public OperationDescription getOperation() {
            return null;
        }

        public int statusCode() {
            return 0;
        }

        public List analysisFindings() {
            return null;
        }

        /* (non-Javadoc)
         * @see glassbox.analysis.api.OperationSummary#isFailing()
         */
        public boolean isFailing() {
            return false;
        }

        /* (non-Javadoc)
         * @see glassbox.analysis.api.OperationSummary#isProblem()
         */
        public boolean isProblem() {
            return false;
        }
        
        /* (non-Javadoc)
         * @see glassbox.analysis.api.OperationSummary#isSlow()
         */
        public boolean isSlow() {
            return false;
        }

        /* (non-Javadoc)
         * @see glassbox.analysis.api.OperationSummary#getAvgExecutionTime()
         */
        public double getAvgExecutionTime() {
            return 0;
        }

        /* (non-Javadoc)
         * @see glassbox.analysis.api.OperationSummary#getCount()
         */
        public int getCount() {
            return 0;
        }

    }

}

