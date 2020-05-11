/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.web;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import glassbox.agent.control.api.GlassboxService;
import glassbox.bootstrap.log.InitializeLog;
import glassbox.client.remote.AgentConnectionException;
import glassbox.client.remote.LocalAgentClient;
import glassbox.config.GlassboxInitializer;
import glassbox.installer.GlassboxInstallerFactory;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import uk.ltd.getahead.dwr.create.SpringCreator;

public class ContextLoaderServlet extends org.springframework.web.context.ContextLoaderServlet {
    
    public static final String NO_DATABASE_URL = "nodb";
    
    public void init() throws ServletException {
        InitializeLog.initializeLogging();
        initializeAgent();
        
        // set the default persistent configuration DS to the glassbox install directory
        if (System.getProperty("glassbox.config.ds", null) == null) {
            String installDir = GlassboxInitializer.CONFIG_DIR;
            if (installDir != null) {
                String suffix;
                String prefix;
                if (usingHsqlDb172OrLater()) {
                    suffix=";shutdown=true";
                    prefix="file:";
                } else {
                    suffix="";
                    prefix="";
                }
                System.setProperty("glassbox.config.ds", "jdbc:hsqldb:"+prefix+installDir+File.separatorChar+"glassbox.db"+suffix);
            } else {
                System.setProperty("glassbox.config.ds", NO_DATABASE_URL);
            }
        }
        super.init();
        SpringCreator.setOverrideBeanFactory(context);
//        //TEST code
//        try{
//        PluginRegistryLocator.getRegistry().addOperationPlugin(new TestPlugin());
//        PluginRegistryLocator.getRegistry().setGlassboxTitle("Test Custom Glassbox");
//        } catch(Exception e) { e.printStackTrace(); }
    }
    
    public static boolean usingHsqlDb172OrLater() {
        // dumb test for HSQLDB 1.7.2 or later
        try {
            Class dbClass = Class.forName("org.hsqldb.Database");
            dbClass.getMethod("isFilesReadOnly", null);
            return true;
        } catch (NoSuchMethodException nme) {
            return false;
        } catch (Throwable t) {
            LogFactory.getLog(ContextLoaderServlet.class).error("Can't access HSQL database", t);
            throw new RuntimeException("Can't access HSQL database", t);
        }
    }
    
    private WebApplicationContext context;
    
    protected ContextLoader createContextLoader() {
        return new ContextLoader() {
            protected WebApplicationContext createWebApplicationContext(
                    ServletContext servletContext, ApplicationContext parent) throws BeansException {
                return context = super.createWebApplicationContext(servletContext, parent);
            }
        };
    }
    
    private void initializeAgent() {
        GlassboxInstallerFactory factory = GlassboxInstallerFactory.getInstance();
        if (factory.getInstaller(this.getServletContext()).isInstalled()) {
            try {
                LocalAgentClient.initialize();
            } catch(AgentConnectionException ace) {
                // this is OK: it just means that we haven't been installed yet!
            } catch (Exception e) {
                org.apache.commons.logging.LogFactory.getLog(getClass()).error("Unable to start Glassbox agent!", e);
            }
        }
    }

    public void destroy() {
        super.destroy();
        LocalAgentClient.shutdown();
    }        

}
