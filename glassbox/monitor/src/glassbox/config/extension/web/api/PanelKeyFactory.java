package glassbox.config.extension.web.api;

public interface PanelKeyFactory {

    String getExecutiveSummaryPanel();

    String getTechnologySummaryPanel();

    String getFailingTechnologyDetailsPanel();

    String getSlaViolationTechnologyDetailsPanel();

    String getNormalTechnologyDetailsPanel();

    String getCommonSolutionsPanel();

    String getRuledOutPanel();

    String getInputURLPanel();

    String getTimeSpentPanel();

}