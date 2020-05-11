/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 7, 2005
 */
package glassbox.config;

import glassbox.agent.control.api.GlassboxService;
import glassbox.bootstrap.log.BootstrapLog;
import glassbox.bootstrap.log.InitializeLog;
import glassbox.monitor.AbstractMonitor;
import glassbox.util.logging.api.LogManagement;
import glassbox.util.org.sl4j.CommonsLoggingFactory;

import java.io.File;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * @author Ron Bodkin
 */
public class GlassboxInitializationImpl implements ApplicationLifecycleAware {

    static {
        InitializeLog.initializeLogging();
    }
    
    /**
     * 
     * @return initialization object
     */
    public Object startUp(File homeDir, boolean systemLevel) {
        //Spring relies on the context ClassLoader being set properly to load resources...

        ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();        
        BootstrapLog.debug("About to start up Glassbox in "+homeDir+", my ClassLoader is "+getClass().getClassLoader()+": parent loader is "+getClass().getClassLoader().getParent());
        BootstrapLog.debug("Context ClassLoader is "+ ctxcl + ((ctxcl != null) ? ": parent ="+ctxcl.getParent() : ""));

        try {
            LogManagement.setLoggerFactory(new CommonsLoggingFactory());
        } catch (LinkageError noMonitorError) { 
            ;  //ok
        }
            
        GlassboxSpringConfigurator configurator = GlassboxSpringConfigurator.aspectOf();
        // only actually initialize it if it wasn't already initialized, e.g., by a test case
        if (configurator.getApplicationContext() == null) {
            configurator.setApplicationContext(createApplicationContext());
        }
        
        if (systemLevel) {
            //Make sure that RMI shuts down...
            ensureRmiShutdown();
        }
        
        AbstractMonitor.setAllDisabled(false);
        
        logSystemSettings();
        
        //resetStatsAfter(20000, configurator.getApplicationContext()); // ms to delay before activating stats gathering... currently 20sec
        
        return getService();
    }

    public void shutDown() {
        AbstractMonitor.setAllDisabled(true);
    	GlassboxSpringConfigurator configurator = GlassboxSpringConfigurator.aspectOf();
        ApplicationContext ctx = configurator.getApplicationContext();
        if (ctx instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) ctx).close();
            configurator.setApplicationContext(null);
        }
    }

    /*
     * Logs the current build settings to the log file
     */
    protected void logSystemSettings() {
        Properties properties = new Properties();
        Log log = LogFactory.getLog(getClass());
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("glassbox_build.properties"));
            log.info("Glassbox Agent has started successfully.");
            log.info("Glassbox Build number: "+ properties.get("build.number")+". Built on "+properties.get("build.date")+" "+properties.get("build.time"));
        } catch (Exception e) {
            log.info("Cannot find Glassbox Build information.");
        }
    }
    
    public static final String CONFIG_LOCATIONS = "glassbox.config.configLocations";

    private static final String DEFAULT_LOCATIONS_PATTERN = "/beans.xml"; //"classpath*:/**/beans.xml"; // work-around apparent spring bug

    protected ApplicationContext createApplicationContext() {
        String configFilePattern = null;
        try {
            configFilePattern = System.getProperty(CONFIG_LOCATIONS);
            if (configFilePattern == null) {
                configFilePattern = DEFAULT_LOCATIONS_PATTERN;
            }
        } catch (SecurityException secEx) {
            configFilePattern = DEFAULT_LOCATIONS_PATTERN;
        }
        try {
            return new ClassPathXmlApplicationContext(StringUtils.commaDelimitedListToStringArray(configFilePattern));
        } catch (Error err) {
            BootstrapLog.error("Error initializing Glassbox. Please reinstall or contact support.", err);
            return null;
        }
    }
    
   
    protected void resetStatsAfter(final long sleepMillis, final ApplicationContext appcon) {
    	Thread starter = new Thread( new Runnable() {
            public void run() {
            	try {
            		Thread.sleep(sleepMillis);
            	} catch (InterruptedException ie) { }
            	((GlassboxService)appcon.getBean("glassboxService")).reset(); // clear stats
            }
    	});
    	starter.start();
    }

    
    /*
     * Rmi Shutdown Hack for JVM bug on Tomcat
     * 
     */
    
    private final long interval = 1000; // check for shutdown once per second

    private ThreadGroup[] allThreadGroups = new ThreadGroup[200];
    private Thread[] allThreads = new Thread[500];
    private Thread lastThread; 

    protected void ensureRmiShutdown() {
        Timer checkForShutdownTimer = new Timer(true);
        checkForShutdownTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (systemShuttingDown()) {
                    shutDown();
                }
            }
        }, interval, interval);
    }

    // this method looks at all the threads in the system, to see if the system is waiting for RMI to let it shutdown 
    public boolean systemShuttingDown() {
        try {
            int threadNcount = 0;
            ThreadGroup tg = Thread.currentThread().getThreadGroup();
            while (tg.getParent() != null) {
                tg = tg.getParent();
            }

            int nGroups = tg.enumerate(allThreadGroups, true);
            if (nGroups == allThreadGroups.length) {
                // we might have more than our initial size!
                return false;
            }
            for (int i = 0; i < nGroups; i++) {
                int nThreads = allThreadGroups[i].enumerate(allThreads);
                if (nThreads == allThreads.length) {
                    // we might have more than our initial size!
                    clearThreads();
                    return false;
                }
                for (int j = 0; j < nThreads; j++) {
                    // Sun VM's use a thread named DestroyJavaVM...
                    if (!allThreads[j].isDaemon() && !"RMI Reaper".equals(allThreads[j].getName()) && !"DestroyJavaVM".equals(allThreads[j].getName())
                            // JRockIt seems to use Thread-## as a name... so we will shutdown if that's the only other non-deamon thread left running 
                            && (allThreads[j].getName().indexOf("Thread-")!=0 || threadNcount++ > 0)) {
                        if (isDebugEnabled()) {
                            if (lastThread != allThreads[j]) {
                                lastThread = allThreads[j];
                                logDebug("Alive: "+lastThread.getName()+", "+lastThread.isDaemon());
                            }
                        }
                        clearThreads();
                        return false;
                    }
                    //TODO: investigate what IBM/other VM's do...
                }
            }
            return true;
        } catch (SecurityException _) {
            getLogger().debug("Unable to ensure system shutdown");
        }
        return false;
    }

    private void clearThreads() {
        Arrays.fill(allThreads, null);
        Arrays.fill(allThreadGroups, null);
    }
    
    /* (non-Javadoc)
     * @see glassbox.config.ApplicationLifecycleAware#getService()
     */
    public Object getService() {
        return GlassboxSpringConfigurator.aspectOf().getApplicationContext().getBean("glassboxService");
    }
    
    private static final long serialVersionUID = 2L;

}
