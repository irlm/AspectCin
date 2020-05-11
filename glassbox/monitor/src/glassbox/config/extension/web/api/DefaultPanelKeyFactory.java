package glassbox.config.extension.web.api;

public class DefaultPanelKeyFactory implements PanelKeyFactory  {
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getExecutiveSummaryPanel()
     */
    public String getExecutiveSummaryPanel() {
        return "default.executiveSummary.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getTechnologySummaryPanel()
     */
    public String getTechnologySummaryPanel() {
        return "default.technologySummary.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getFailingTechnologyDetailsPanel()
     */
    public String getFailingTechnologyDetailsPanel() {
        return "failing.technologyDetails.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getSlaViolationTechnologyDetailsPanel()
     */
    public String getSlaViolationTechnologyDetailsPanel() {
        return "slow.technologyDetails.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getNormalTechnologyDetailsPanel()
     */
    public String getNormalTechnologyDetailsPanel() {
        return "normal.technologyDetails.panel";
    }
 
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getCommonSolutionsPanel()
     */
    public String getCommonSolutionsPanel() {
        return "default.commonSolutions.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getRuledOutPanel()
     */
    public String getRuledOutPanel() {
        return "default.ruledOut.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getInputURLPanel()
     */
    public String getInputURLPanel() {
        return "default.inputurl.panel";
    }
    
    /* (non-Javadoc)
     * @see glassbox.client.helper.PanelKeyFactory#getTimeSpentPanel()
     */
    public String getTimeSpentPanel() {
        return "default.timespent.panel";
    }    
    
}
