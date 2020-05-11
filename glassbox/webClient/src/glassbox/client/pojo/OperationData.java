/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the terms of the Lesser Gnu Public License
 * v2.1, which accompanies this distribution and is available at http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.pojo;

import glassbox.analysis.api.OperationSummary;
import glassbox.client.helper.MessageHelper;
import glassbox.track.api.OperationDescription;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;

public class OperationData implements Serializable {
        
    private OperationSummary operationSummary;
    private String agentName = null;
    private String agentDescription = null;
    private String agentUrl = null;
    private String viewed = "*"; // deprecated
    
    public OperationData(String agentName, String agentDescription, String agentUrl, OperationSummary operationSummary) {
        this.agentName = agentName;
        this.agentDescription = agentDescription;
        this.agentUrl = agentUrl;
        this.operationSummary = operationSummary;            
    }
    
    public OperationData(String agentUrl, OperationSummary operationSummary) {
        this(null, null, agentUrl, operationSummary);            
    }

    public OperationSummary getOperationSummary() {
        return operationSummary;
    }	
    
    public String getOperationId() {
        return (String)getKey();
    }
    
    public Object getKey() {
        return operationSummary.getOperation() + operationSummary.getOperation().getContextName() + agentUrl;            
    }
    
    public OperationDescription getOperationKey()  {
        return operationSummary.getOperation();            
    }
    
    public String getOperationName(){
        return operationSummary.getOperation().getOperationName();   
    }
    
    public String getOperationShortName() {
        return operationSummary.getOperation().getShortName();
    }

    public String getOperation() {
        return operationSummary.getOperation().getShortName();
   }
    
    public void setUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }
    
    public String getAgentUrl() {
        return agentUrl;
    }
    
    /**
     * 
     * Note: this is used to display the value of the server column in the UI
     */
    public String getServer() {
        return agentName;
    }
        
    public Double getAverageExecutionTimeAsDouble() {
        return new Double(operationSummary.getAvgExecutionTime());
    }
    
    public String getAverageExecutionTime() {
        return TimeConversion.formatTimeInSeconds(operationSummary.getAvgExecutionTime());
    }
        
    public String getAverageTime() {
        return this.getAverageExecutionTime();
    }
    
    public Integer getExecutions() {            
        return new Integer(operationSummary.getCount());
    }

    public String getStatus() {
        switch(operationSummary.statusCode()) {
            case OperationSummary.StatusSlow : return "SLOW";
            case OperationSummary.StatusFailing : return "FAILING";
            case OperationSummary.StatusOK : return "OK";
            default : throw new Error("Unknown status code: " + operationSummary.statusCode());
        }
    }
    
    public int getStatusCode() {
        return operationSummary.statusCode();
    }

    public List getAnalyses() {
        ArrayList messageList = new ArrayList();
        Iterator findingsIt = operationSummary.analysisFindings().iterator();
        while(findingsIt.hasNext()) {
            String key = (String)findingsIt.next();
            messageList.add(MessageHelper.getString(key));
        }
        return messageList;
    }
    
    /*
     * Convenience method for DWR/Views.
     */
    public String getAnalysis() {            
        return getDisplayedAnalysis();
    }
    
    public void setAnalysis(String analysis) {
        
    }
    
    public String getPrimaryAnalysis() {            
        return MessageHelper.getString((String)operationSummary.analysisFindings().get(0));
    }
    
    /*
     * Assistance for DWR/Views.
     * 
     */
    public String getViewed() {
        return viewed;
    }
    public void setViewed(String viewed) {
        this.viewed = viewed;
    }
    
    public String getDisplayedAnalysis() {
        return (this.isOk() ? "" : this.getPrimaryAnalysis()) + (this.hasMultipleAnalyses() ? "+" : "");
    }
    
    public boolean hasMultipleAnalyses() {
        return operationSummary.analysisFindings().size() > 1;
    }
    
    public int getNumberAnalyses() {
        return operationSummary.analysisFindings().size();
    }
    
    public boolean isOk()
    {
        return (operationSummary.statusCode() == OperationSummary.StatusOK);
    }

    public boolean isSlow()
    {
        return (operationSummary.statusCode() == OperationSummary.StatusSlow);
    }

    public boolean isFailing()
    {
        return (operationSummary.statusCode() == OperationSummary.StatusFailing);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OperationData)) return false;
        return getOperationSummary().getOperation().equals(((OperationData)obj).getOperationSummary().getOperation());
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getAgentDescription() {
        return agentDescription;
    }

    public void setAgentDescription(String agentDescription) {
        this.agentDescription = agentDescription;
    }

    public void setApplicationName(String applicationName) {
    	// huh? needed for DWR....?
    }

    public pointcut getApplicationName() : execution(String getApplicationName());
    
    /** @deprecated */
    public String getApplication() {  
    	return getApplicationName();
    }
    
    public String getApplicationName() {
    	return getOperationSummary().getOperation().getContextName();
    }
   
}