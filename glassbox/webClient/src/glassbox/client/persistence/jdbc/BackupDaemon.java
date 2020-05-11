/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.persistence.jdbc;

import glassbox.web.ContextLoaderServlet;

import java.util.TimerTask;

public class BackupDaemon extends TimerTask {
	
	protected ConfigurationDAO configurationDAO = null;
    private boolean enabled = false;
    private boolean dirty = false;
	
    public void init() {
        enabled = ContextLoaderServlet.usingHsqlDb172OrLater();
        
        String configDS = System.getProperty("glassbox.config.ds");
        if (configDS==null || configDS.indexOf("jdbc:hsqldb:")!=0) {
            enabled = false;
        }        
    }
    
    public void run() {		
        if (enabled && dirty && configurationDAO != null && configurationDAO.hasConfiguration()) {
            dirty=false;
            configurationDAO.defragment();
        }
	}

	public ConfigurationDAO getConfigurationDAO() {
		return configurationDAO;
	}

	public void setConfigurationDAO(ConfigurationDAO configurationDAO) {
		this.configurationDAO = configurationDAO;
	}

     
    public void destroy() throws Exception {
        configurationDAO = null;
        try {
            //see http://blog.taragana.com/index.php/archive/how-to-close-all-connections-in-hsqldb-to-prevent-a-locking-defect/3/
            org.hsqldb.DatabaseManager.closeDatabases(0);
            org.hsqldb.DatabaseManager.getTimer().shutDown();
        } catch (Throwable t) {
            ; // nothing we can do further: we sometimes get an NPE, presumably because the databases are already shut down
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
