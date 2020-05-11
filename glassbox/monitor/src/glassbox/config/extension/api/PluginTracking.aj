package glassbox.config.extension.api;

import glassbox.analysis.api.ProblemAnalysis;
import glassbox.track.api.OperationDescription;

public aspect PluginTracking {
    // removed ITD field so it is serialized/unserialized properly... try to fix
    public static interface PluginHolder {
        public void setPluginKey(String key);
        public String getPluginKey();
        public OperationPlugin getPlugin();
        public void setPlugin(OperationPlugin plugin);
    }
//    
//    declare parents: OperationDescription extends PluginHolder;
//    declare parents: ProblemAnalysis extends PluginHolder;
//    
//    public OperationPlugin PluginHolder.getPlugin() {
//        return PluginRegistryLocator.getRegistry().lookupOperationPlugin(getPluginKey());
//    }
//    public void PluginHolder.setPlugin(OperationPlugin plugin) {
//        setPluginKey(plugin==null ? null : plugin.getKey());
//    }
//    
//    after(PluginHolder original, PluginHolder copy) returning : 
//        execution(PluginHolder+.new(PluginHolder+)) && this(copy) && args(original) {
//        copy.setPluginKey(original.getPluginKey());
//    }
    
}
