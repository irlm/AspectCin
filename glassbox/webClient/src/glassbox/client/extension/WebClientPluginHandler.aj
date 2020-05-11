package glassbox.client.extension;

import glassbox.analysis.api.OperationAnalysis;
import glassbox.analysis.api.ProblemAnalysis;
import glassbox.client.helper.*;
import glassbox.client.helper.problems.ProblemHelper;
import glassbox.client.pojo.OperationData;
import glassbox.client.pojo.Preferences;
import glassbox.config.extension.api.*;
import glassbox.config.extension.api.ConnectionProvider.Connection;
import glassbox.config.extension.web.api.PanelKeyFactory;
import glassbox.track.api.OperationDescription;
import glassbox.util.timing.api.TimeConversion;
import glassbox.velocity.RefreshableSingletonFileResourceLoader;

import java.util.*;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public aspect WebClientPluginHandler {

    private RefreshableSingletonFileResourceLoader velocityLoader;
    private String[] problemKeys;
    private ConnectionHelper connectionHelper;
    private Object connectionProvider;
    private Timer refreshTimer;
    private Set previousConnections = new HashSet(); // all connections that have been set up by this handler    
    private Preferences preferences;
    private Object pluginListener;
    private static final long REFRESH_INTERVAL = 60L * TimeConversion.MILLISECONDS_PER_SECOND; // one minute...

    pointcut getProblemHelperSignature() :
        within(ProblemDisplayHelper+) && execution(ProblemHelper ProblemDisplayHelper.getProblemHelper(ProblemAnalysis));

    declare error: 
        within(ProblemDisplayHelper+) && execution(public * ProblemDisplayHelper.getProblemHelper(..)) && !getProblemHelperSignature():
        "plugin interface is broken after a refactoring";

    pointcut getProblemHelper(ProblemAnalysis problem) : getProblemHelperSignature() && args(problem);

    ProblemHelper around(ProblemAnalysis problem) : getProblemHelper(problem) && if(problem.getPlugin()!=null) {
        return problem.getPlugin().getProblemHelper(problem);
    }

    pointcut getPanelKeyFactory(OperationDescription operation) :
        within(DisplayHelper+) && execution(PanelKeyFactory getPanelKeyFactory(OperationDescription)) && args(operation);

    PanelKeyFactory around(OperationDescription operation) : getPanelKeyFactory(operation) && if(operation.getPlugin()!=null) {
        PanelKeyFactory factory = operation.getPlugin().getPanelKeyFactory(operation);
        if (factory != null) {
            return factory;
        }
        return proceed(operation);
    }

    pointcut getOperationFormatter(OperationAnalysis analysis) :
        within(DisplayHelper+) && execution(Object getOperationFormatter(OperationAnalysis)) && args(analysis);

    Object around(OperationAnalysis analysis) : getOperationFormatter(analysis) {
        if (analysis != null) {
            OperationPlugin plugin = analysis.summary().getOperation().getPlugin();
            if (plugin != null) {
                Object helper = plugin.getOperationFormatter(analysis);
                if (helper != null) {
                    return helper;
                }
            }
        }
        return proceed(analysis);
    }

    String[] around() : execution(public String[] ProblemDisplayHelper.getAllProblemKeys()) && !cflow(execution(* *(..)) && within(WebClientPluginHandler)) {
        return problemKeys;
    }

    private void connectListener(glassbox.config.extension.api.PluginRegistry registry) {
        pluginListener = makePluginRegistryListener();
        registry.addPluginListener((PluginRegistryListener)pluginListener);
    }
    
    private Object makePluginRegistryListener() {
        return new PluginRegistryListener() {

            public void addedOperationPlugin(OperationPlugin operationPlugin, glassbox.config.extension.api.PluginRegistry pluginRegistry) {
                WebClientPluginHandler.this.addedOperationPlugin(operationPlugin, pluginRegistry);                
            }

            public void removedOperationPlugin(OperationPlugin operationPlugin, glassbox.config.extension.api.PluginRegistry pluginRegistry) {
                WebClientPluginHandler.this.removedOperationPlugin(operationPlugin, pluginRegistry);                
            }

            public void setConnectionProvider(ConnectionProvider connectionProvider, glassbox.config.extension.api.PluginRegistry pluginRegistry) {
                WebClientPluginHandler.this.setConnectionProvider(connectionProvider, pluginRegistry);                
            }

            public void setGlassboxTitle(String glassboxTitle, glassbox.config.extension.api.PluginRegistry pluginRegistry) {
                WebClientPluginHandler.this.setGlassboxTitle(glassboxTitle, pluginRegistry);
            }
            
        };
    }
    
    public void addedOperationPlugin(OperationPlugin plugin, glassbox.config.extension.api.PluginRegistry registry) {
        recalcKeys(registry);
        addPluginBundle(plugin);
    }

    protected void addPluginBundle(OperationPlugin plugin) {
        ResourceBundle bundle = plugin.getResourceBundle();
        if (bundle != null) {
            MessageHelper.addBundle(bundle);
        }
        if (plugin.getTemplatePath() != null) {
            velocityLoader.addPath(plugin.getTemplatePath());
        }
    }

    public void removedOperationPlugin(OperationPlugin plugin, glassbox.config.extension.api.PluginRegistry registry) {
        recalcKeys(registry);

        ResourceBundle bundle = plugin.getResourceBundle();
        if (bundle != null) {
            MessageHelper.removeBundle(bundle);
        }
        if (plugin.getTemplatePath() != null) {
            velocityLoader.removePath(plugin.getTemplatePath());
        }
    }
    
    private void recalcKeys(glassbox.config.extension.api.PluginRegistry registry) {
        ArrayList held = new ArrayList();
        String toAdd[] = ProblemDisplayHelper.getAllProblemKeys();
        held.addAll(Arrays.asList(toAdd));
        for (Iterator it = registry.getOperationPlugins().iterator(); it.hasNext();) {
            OperationPlugin next = (OperationPlugin) it.next();
            toAdd = next.getAllProblemKeys();
            if (toAdd != null) {
                held.addAll(Arrays.asList(toAdd));
            }
        }
        problemKeys = (String[]) held.toArray(new String[0]);
    }

    public void configure() {
        glassbox.config.extension.api.PluginRegistry registry = PluginRegistryLocator.getRegistry();
        if (registry != null) {
            connectListener(registry);
            recalcKeys(registry);
   
            for (Iterator it = registry.getOperationPlugins().iterator(); it.hasNext();) {
                OperationPlugin plugin = (OperationPlugin) it.next();
                addPluginBundle(plugin);
            }
   
            setConnectionProvider(registry.getConnectionProvider(), registry);
            setGlassboxTitle(registry.getGlassboxTitle(), registry);
        }
    }

    public void destroy() {
        PluginRegistryLocator.getRegistry().removePluginListener((PluginRegistryListener)pluginListener);
        
        clearTimer();
        connectionProvider = null;        
    }

    public RefreshableSingletonFileResourceLoader getVelocityLoader() {
        return velocityLoader;
    }

    public void setVelocityLoader(RefreshableSingletonFileResourceLoader velocityLoader) {
        this.velocityLoader = velocityLoader;
    }

    public ConnectionHelper getConnectionHelper() {
        return connectionHelper;
    }

    public void setConnectionHelper(ConnectionHelper connectionHelper) {
        this.connectionHelper = connectionHelper;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider, glassbox.config.extension.api.PluginRegistry registry) {
        this.connectionProvider = connectionProvider;

        if (connectionProvider != null) {
            refreshConnections();
        }
        reschedule();
    }

    private synchronized void refreshConnections() {        
        Set current = new HashSet();
        List connections = ((ConnectionProvider)connectionProvider).getConnections();
        for (Iterator it = connections.iterator(); it.hasNext();) {
            Connection connection = (Connection) it.next();
            if (ConnectionProvider.LOCAL_PROTOCOL.equals(connection.getProtocol())) {
                connectionHelper.removeLocalConnectionsTransiently();
            }
            connectionHelper.buildAndAddTempConnection(connection.getName(), connection.getName(),
                    connection.getHostName(), connection.getProtocol(), connection.getPort());
            previousConnections.remove(connection.getName());
            current.add(connection.getName());
        }
        for (Iterator it = previousConnections.iterator(); it.hasNext();) {
            connectionHelper.deleteConnection((String) it.next());
        }
        previousConnections = current;
    }

    private void reschedule() {
        clearTimer();
        if (connectionProvider != null) {
            TimerTask monitorTask = new TimerTask() {
                public void run() {
                    refreshConnections();
                }
            };
            refreshTimer = new Timer("Administration Connection Refresh", true);
            refreshTimer.schedule(monitorTask, 0, REFRESH_INTERVAL);
        }
    }
    
    private synchronized void clearTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }        
    
    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public void setGlassboxTitle(String glassboxTitle, glassbox.config.extension.api.PluginRegistry pluginRegistry) {
        if (preferences != null) {
            preferences.setGlassboxTitle(glassboxTitle);
        }
    }

    String around() : OperationData.getApplicationName() {
        String result = proceed();
        String gbTitle = preferences.getGlassboxTitle();
        if (gbTitle == null || !("Glassbox Web Client".equals(result))) {
            return result;
        }
        return gbTitle;
    }
}
