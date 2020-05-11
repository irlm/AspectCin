package glassbox.config.extension.api;

import java.util.*;

/**
 * This class is used to configure application-specific extensions to Glassbox.
 * Simply adding monitors doesn't need it: you can just deploy a monitor jar with aspects to the classpath,
 * and an app can simply call on the response API.
 * 
 * This registry is designed to support deeper extensions, like adding custom operations.
 * 
 * @author Ron Bodkin
 *
 */
public class PluginRegistryLocator {
    
    private static PluginRegistry registry = new QueueRegistrationsRegistry();
    private static final long serialVersionUID = 1L;
    
    public static synchronized PluginRegistry getRegistry() {
        return registry;
    }
    
    public static synchronized void setRegistry(PluginRegistry newRegistry) {
        Collection oldPlugins = null;
        ConnectionProvider connectionProvider = null;
        String oldTitle = null;
        //deal with redeployment - yuck
        if (registry != null) {
            oldPlugins = registry.getOperationPlugins();
            connectionProvider = registry.getConnectionProvider();
            oldTitle = registry.getGlassboxTitle();
        }
        
        PluginRegistry oldRegistry = registry;
        registry = newRegistry;

        registry.setConnectionProvider(connectionProvider);
        registry.setGlassboxTitle(oldTitle);
        if (oldPlugins != null) {
            for (Iterator it=oldPlugins.iterator(); it.hasNext();) {
                OperationPlugin plugin=(OperationPlugin)it.next();
                newRegistry.addOperationPlugin(plugin);
                oldRegistry.removeOperationPlugin(plugin);
            }
        }
    };
}
