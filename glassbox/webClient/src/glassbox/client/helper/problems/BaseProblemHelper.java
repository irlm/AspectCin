/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.*;
import glassbox.client.helper.DisplayHelper;
import glassbox.client.helper.MessageHelper;
import glassbox.client.pojo.OperationAnalysisData;
import glassbox.client.pojo.OperationData;
import glassbox.track.api.CallDescription;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

public abstract class BaseProblemHelper implements Serializable, ProblemHelper {

    protected ProblemAnalysis problem = null;
    protected OperationData operationData = null;
    protected OperationAnalysisData analysisData = null;
    protected List problemsOfType;
    protected boolean symptom = false;  
    
    public abstract String getProblemKey();

    public String getVerdict() {
        return MessageHelper.getString("verdict.problem."+getProblemKey());
    }
    
    public String getProblemTypeName() {
        return MessageHelper.getString(getProblemKey());
    }

    public String getProblemTypeIcon() {
        return MessageHelper.getString(getProblemKey() + ".icon");
    }
    
    public String getNamedIcon(String name) {
        return MessageHelper.getString(name + ".icon");
    }

    public String getProblemSummary() {
        Object[] args = { operationData.getOperationShortName() };
        MessageFormat form = new MessageFormat(MessageHelper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }

    public String getProblemTypeInfo() {
        return MessageHelper.getString("problem.type.info." + getProblemKey());
    }

    public String getProblemDescription() {
        return formatDescription(null);
    }

    protected String formatSummary(Object[] args) {
        String formatStr = (problemsOfType.size() > 1 ? "summary.info_n." : "summary.info_1.");
        MessageFormat msg = new MessageFormat(MessageHelper.getString(formatStr + getProblemKey()));
        return msg.format(args);
    }

    protected String formatDescription(Object[] args) {
        MessageFormat msg = new MessageFormat(MessageHelper.getString("problem.describe." + getProblemKey()));
        return msg.format(args);
    }

    public List getCommonSolutions() {
        ArrayList commonSolutions = new ArrayList();
        for (int i = 1; i <= getCommonSolutionLength(); i++) {
            commonSolutions.add(getCommonSolutionStatement(i));
        }
        return commonSolutions;
    }

    public int getCommonSolutionLength() {
        return Integer.valueOf(MessageHelper.getString("comsol." + getProblemKey() + ".num")).intValue();
    }

    public String getCommonSolutionStatement(int i) {
        return MessageHelper.getString("comsol." + getProblemKey() + ".entry_" + i);
    }

    public String getCommonSolutionTitle() {
        return MessageHelper.getString("comsol." + getProblemKey() + ".title");
    }

    public StackTraceElement[] getStackTrace() {
        return null;
    }

    public List getRelatedURLs() {
        return null;
    }

    public String getProblemDetailPanel() {
        return MessageHelper.getString("empty.panel");
    }

    public List getEvents() {
        return problem.getEvents();
    }

    public ProblemAnalysis getProblem() {
        return problem;
    }

    public void setProblem(ProblemAnalysis problem) {
        this.problem = problem;
    }

    /** helper method that is only valid for single call problems */
    protected String getAffectedResources(String resourceType) {
        if (problemsOfType.size() == 1) {
            CallDescription requestDescriptor = ((SingleCallProblem) problem).getCall();
            StringBuffer buf = new StringBuffer();
            buf.append(requestDescriptor.getResourceKey());
            buf.append(" ");
            buf.append(resourceType);
            return buf.toString();
        } else {
            ArrayList resources = new ArrayList(problemsOfType.size());
            for (Iterator it = problemsOfType.iterator(); it.hasNext();) {
                SingleCallProblem problem = (SingleCallProblem) it.next();
                CallDescription requestDescriptor = problem.getCall();
                Object key = requestDescriptor.getResourceKey();
                if (!resources.contains(key)) {
                    resources.add(key);
                }
            }

            return getAffectedResources(resourceType, resources);
        }
    }

    protected String getAffectedResources(String resourceType, List resources) {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        for (Iterator it = resources.iterator(); it.hasNext(); i++) {
            if (i > 0) {
                if (i + 1 < resources.size()) {
                    buf.append(", ");
                } else {
                    buf.append(" and ");
                }
            }
            buf.append(it.next());
        }
        buf.append(" ");
        buf.append(resourceType);
        if (resources.size() > 1) {
            buf.append("s");
        }
        return buf.toString();
    }

    /** helper method that is only valid for single call database problems */
    protected String getAffectedDatabases() {
        return getAffectedResources("database");
    }

    protected String getAffectedRemoteServices() {
        return getAffectedResources("remote service");
    }

    /** helper method that is only valid for single call database problems */
    protected CallDescription getCall() {
        return ((SingleCallProblem) problem).getCall();
    }

    /** helper method that is only valid for default failure problems */
    protected DefaultFailureProblem getFailure() {
        return (DefaultFailureProblem) problem;
    }

    /**
     * @return the analysisData
     */
    public OperationAnalysisData getAnalysisData() {
        return analysisData;
    }

    /**
     * @param analysisData
     *            the analysisData to set
     */
    public void setAnalysisData(OperationAnalysisData analysisData) {
        this.analysisData = analysisData;
    }

    /**
     * @return the operationData
     */
    public OperationData getOperationData() {
        return operationData;
    }

    /**
     * @param operationData
     *            the operationData to set
     */
    public void setOperationData(OperationData operationData) {
        this.operationData = operationData;
    }

    /**
     * @return all problems of the same type
     */
    public List getProblemsOfType() {
        return problemsOfType;
    }

    /**
     * @param problemsOfType the list of all problems of the same type to set
     */
    public void setProblemsOfType(List problemsOfType) {
        this.problemsOfType = problemsOfType;
    }

    /**
     * @return the symptom
     */
    public boolean isSymptom() {
        return symptom;
    }

    /**
     * @param symptom the symptom to set
     */
    public void setSymptom(boolean symptom) {
        this.symptom = symptom;
    }
    
    public boolean isVisible() {
        return true;
    }
    
    public void setSlowProblemCount(int slowProblemCount) {
    }
}
