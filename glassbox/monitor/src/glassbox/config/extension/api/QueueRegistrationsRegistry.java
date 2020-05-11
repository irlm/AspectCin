/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */

package glassbox.config.extension.api;

import java.util.*;

// it would probably be easier to just depend directly on a singleton registry...
public class QueueRegistrationsRegistry implements PluginRegistry {
    protected Map operationPlugins = Collections.synchronizedMap(new HashMap());
    protected List listeners = Collections.synchronizedList(new ArrayList());
    protected ConnectionProvider connectionProvider;
    protected String glassboxTitle = null;
    private static final long serialVersionUID = 1L;
    
    public void addPluginListener(PluginRegistryListener listener) {
        listeners.add(listener);
    }
    
    public void removePluginListener(PluginRegistryListener listener) {
        listeners.remove(listener);
    }
    
    public void addOperationPlugin(OperationPlugin operationPlugin) throws ConfigurationException {
        String key = operationPlugin.getKey();
        if (key == null) {
            throw new IllegalArgumentException("Plugins must have a unique, non-null key");
        }
        operationPlugins.put(key, operationPlugin);
        for (Iterator it=listeners.iterator(); it.hasNext();) {
            PluginRegistryListener listener = (PluginRegistryListener)it.next();
            listener.addedOperationPlugin(operationPlugin, this);
        }
    }
    
    public void removeOperationPlugin(OperationPlugin operationPlugin) throws ConfigurationException {
        operationPlugins.remove(operationPlugin.getKey());            
        for (Iterator it=listeners.iterator(); it.hasNext();) {
            PluginRegistryListener listener = (PluginRegistryListener)it.next();
            listener.removedOperationPlugin(operationPlugin, this);
        }
    }
    
    public OperationPlugin lookupOperationPlugin(String key) {
        return (OperationPlugin)operationPlugins.get(key);
    }
    
    public Collection getOperationPlugins() {
        return Collections.unmodifiableCollection(operationPlugins.values());
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) throws ConfigurationException {
        this.connectionProvider = connectionProvider;
        for (Iterator it=listeners.iterator(); it.hasNext();) {
            PluginRegistryListener listener = (PluginRegistryListener)it.next();
            listener.setConnectionProvider(connectionProvider, this);
        }
    }

    public void setGlassboxTitle(String title) {
        glassboxTitle = title;
        for (Iterator it=listeners.iterator(); it.hasNext();) {
            PluginRegistryListener listener = (PluginRegistryListener)it.next();
            listener.setGlassboxTitle(glassboxTitle, this);
        }
    }
    
    public String getGlassboxTitle() {
        return glassboxTitle;
    }
}