package glassbox.analysis.api;

import glassbox.config.extension.api.OperationPlugin;
import glassbox.config.extension.api.PluginRegistryLocator;

public abstract class AbstractProblemAnalysis implements ProblemAnalysis {

    private String pluginKey;
    private static final long serialVersionUID = 1;
    
    public void setPluginKey(String key) { pluginKey = key; }
    public String getPluginKey() { return pluginKey; }

    public OperationPlugin getPlugin() {
        return PluginRegistryLocator.getRegistry().lookupOperationPlugin(getPluginKey());
    }
    public void setPlugin(OperationPlugin plugin) {
        setPluginKey(plugin==null ? null : plugin.getKey());
    }
    
}
