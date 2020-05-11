package glassbox.config.extension.api;

public interface PluginRegistryListener {
    public void addedOperationPlugin(OperationPlugin operationPlugin, PluginRegistry pluginRegistry);
    public void removedOperationPlugin(OperationPlugin operationPlugin, PluginRegistry pluginRegistry);
    
    public void setConnectionProvider(ConnectionProvider connectionProvider, PluginRegistry pluginRegistry);
    public void setGlassboxTitle(String glassboxTitle, PluginRegistry pluginRegistry);
}
