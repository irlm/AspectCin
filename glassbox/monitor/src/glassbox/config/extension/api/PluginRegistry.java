package glassbox.config.extension.api;

import java.util.Collection;

/**
 * This interface is used to configure application-specific extensions to Glassbox.
 * Simply adding monitors doesn't need it: you can just deploy a monitor jar with aspects to the classpath,
 * and an app can simply call on the response API.
 * 
 * A registry supports deeper extensions, like adding custom operations.
 * 
 * @author Ron Bodkin
 *
 */
public interface PluginRegistry {
    public void addPluginListener(PluginRegistryListener listener);
    public void removePluginListener(PluginRegistryListener listener);
    
    public void addOperationPlugin(OperationPlugin operationPlugin) throws ConfigurationException;
    public void removeOperationPlugin(OperationPlugin operationPlugin) throws ConfigurationException;
    
    public void setConnectionProvider(ConnectionProvider connectionProvider) throws ConfigurationException;
    public ConnectionProvider getConnectionProvider();
    
    public OperationPlugin lookupOperationPlugin(String key);
    
    public Collection getOperationPlugins();

    public String getGlassboxTitle();
    public void setGlassboxTitle(String title);
}
