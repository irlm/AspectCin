package glassbox.client.helper.problems;

import glassbox.analysis.api.ProblemAnalysis;
import glassbox.track.api.OperationDescription;

import java.text.MessageFormat;
import java.util.*;

public class DynamicProblemHelper implements ProblemHelper {
    protected ProblemAnalysis problem;
    protected OperationDescription operation;
    protected Object analysisData;
    protected List problemsOfType;
    protected boolean symptom = false; 
    protected String problemKey;
    protected MessageHelper helper;
    
    public String getProblemKey() {
        return problemKey;
    }
    
    public void setProblemKey(String problemKey) {
        this.problemKey = problemKey;
    }

    public static interface MessageHelper {
        public String getString(String key);
        public Enumeration getKeys();
    }
    
    public void setMessageHelper(MessageHelper helper) {
        this.helper = helper;
    }

    public String getVerdict() {
        return helper.getString("verdict.problem."+getProblemKey());
    }
    
    public String getProblemTypeName() {
        return helper.getString(getProblemKey());
    }

    public String getProblemTypeIcon() {
        return helper.getString(getProblemKey() + ".icon");
    }
    
    public String getNamedIcon(String name) {
        return helper.getString(name + ".icon");
    }

    public String getProblemSummary() {
        Object[] args = { operation.getShortName() };
        MessageFormat form = new MessageFormat(helper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }

    public String getProblemTypeInfo() {
        return helper.getString("problem.type.info." + getProblemKey());
    }

    public String getProblemDescription() {
        return formatDescription(null);
    }

    protected String formatSummary(Object[] args) {
        String formatStr = (problemsOfType.size() > 1 ? "summary.info_n." : "summary.info_1.");
        MessageFormat msg = new MessageFormat(helper.getString(formatStr + getProblemKey()));
        return msg.format(args);
    }

    protected String formatDescription(Object[] args) {
        MessageFormat msg = new MessageFormat(helper.getString("problem.describe." + getProblemKey()));
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
        return Integer.valueOf(helper.getString("comsol." + getProblemKey() + ".num")).intValue();
    }

    public String getCommonSolutionStatement(int i) {
        return helper.getString("comsol." + getProblemKey() + ".entry_" + i);
    }

    public String getCommonSolutionTitle() {
        return helper.getString("comsol." + getProblemKey() + ".title");
    }

    public StackTraceElement[] getStackTrace() {
        return null;
    }

    public List getRelatedURLs() {
        return null;
    }

    public String getProblemDetailPanel() {
        return helper.getString(getProblemKey()+".problem.panel");
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

    /**
     * @return the analysisData
     */
    public Object getAnalysisData() {
        return analysisData;
    }

    /**
     * @param analysisData
     *            the analysisData to set
     */
    public void setAnalysisData(Object analysisData) {
        this.analysisData = analysisData;
    }

    /**
     * @return the operationData
     */
    public OperationDescription getOperation() {
        return operation;
    }

    /**
     * @param operationData
     *            the operationData to set
     */
    public void setOperation(OperationDescription operation) {
        this.operation = operation;
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