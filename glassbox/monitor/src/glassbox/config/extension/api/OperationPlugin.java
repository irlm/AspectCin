package glassbox.config.extension.api;

import glassbox.analysis.api.OperationAnalysis;
import glassbox.analysis.api.ProblemAnalysis;
import glassbox.client.helper.problems.ProblemHelper;
import glassbox.config.extension.web.api.PanelKeyFactory;
import glassbox.track.api.OperationDescription;

import java.util.ResourceBundle;
import java.util.Set;

/**
 * This version of an operation plugin must be installed across a cluster to be useful... 
 * In future it would be better to allow separating display logic (that needs to be distributed) from analysis (that does not) 
 *
 */
public interface OperationPlugin {
    String getKey();
    Set getOperations();
    OperationAnalysis analyze(OperationDescription description, long monstart);
    ProblemHelper getProblemHelper(ProblemAnalysis problem);
    PanelKeyFactory getPanelKeyFactory(OperationDescription operation);
    Object getOperationFormatter(OperationAnalysis analysis);
    String[] getAllProblemKeys();
    ResourceBundle getResourceBundle();
    String getTemplatePath();
}
