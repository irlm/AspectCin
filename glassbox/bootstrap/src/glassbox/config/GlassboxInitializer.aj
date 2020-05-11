/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 7, 2005
 */
package glassbox.config;

import glassbox.bootstrap.log.BootstrapLog;
import glassbox.thread.context.SavedContextLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author Ron Bodkin
 */
public class GlassboxInitializer {
    protected static boolean failed = false;
	protected static ApplicationLifecycleAware initializer;
    private static File directory;
    public static final String CONFIG_DIR_PROPERTY = "glassbox.install.dir";    
	public static final String CONFIG_DIR = getConfigDir();
	public static String getConfigDir() {
	    String installDir = System.getProperty(CONFIG_DIR_PROPERTY);
        if (installDir!= null) {
            while (installDir.endsWith(File.separator) || installDir.endsWith("/")) {
                installDir = installDir.substring(0, installDir.length()-1);
            }
        }
        return installDir;	    
    }
    /**
     * Hook for external clients to explicitly start us.
     * @return service object 
     */
    public synchronized static Object start(boolean systemLevel) {
        if (initializer == null) {
            return doStart(systemLevel);
        } else {
            return initializer.getService();
        }
    }
    
    protected static Object doStart(boolean systemLevel) {
        SavedContextLoader savedContextLoader = new SavedContextLoader(); 
        try {
            initializer = getInitialization();
            return initializer.startUp(directory, systemLevel);
        } catch (Exception e) {
            if (CONFIG_DIR != null) {
                BootstrapLog.error("Glassbox initialization error after installation.", e);
            } else {
                BootstrapLog.debug("Glassbox not starting up: hasn't been installed or environment not set up.", e);
            }
            throw new AspectConfigurationException("Failure: GLASSBOX Monitor cannot find and/or start Agent.  Check JAVA_OPTS first.", e);
        } finally {
            savedContextLoader.restore();
        }    
    }        
    
    public synchronized static void stop() {
        if (initializer != null) {
            initializer.shutDown();    
            initializer = null;
        }
    }
    
    //XXX TODO test defaults for various Catalina installs and even on other app servers
    // e.g., test server type, then default to right directory convention
    protected static String getDefaultJarLocation() {
        String propertyLocs[] = new String[] { "catalina.home", "catalina.base", "jboss.home" };
        String baseDir = null;
        for (int i = 0; i < propertyLocs.length; i++) {
            String dirLoc = System.getProperty(propertyLocs[i]);
            if (dirLoc != null) {
                if ((new File(dirLoc)).canRead()) {
                    baseDir = dirLoc;
                    break;
                }
            }            
        }

        try {
            if (baseDir == null) {
                baseDir = "..";
                String envLocs[] = new String[] { "CATALINA_BASE", "CATALINA_HOME", "BASE_DIR", "WL_HOME", "JBOSS_HOME" };
                for (int i = 0; i < envLocs.length; i++) {
                    String dirLoc = System.getenv(envLocs[i]);
                    if (dirLoc != null) {
                        if ((new File(dirLoc)).canRead()) {
                            baseDir = dirLoc;
                            break;
                        }
                    }            
                }
            }
        } catch (Error e) {
            // not supported on Java 1.3/1.4
        } catch (UnsupportedOperationException olderExc) {
            // not supported on Java 1.3/1.4
        }
            
        File tomcatDefault = new File(baseDir, "shared/lib/glassbox");
        if (tomcatDefault.canRead()) {
            return tomcatDefault.getPath(); 
        }
        tomcatDefault = new File(baseDir, "common/lib/glassbox");
        if (tomcatDefault.canRead()) {
            return tomcatDefault.getPath(); 
        }
        
        // if no nested directory found, then assume no child loader needed: this is the NORM now
        // we typically run from inside a webapp directory
        return null;
    }
    
	protected static ApplicationLifecycleAware findInitialization() {
		// this is the bit that needs to set up a private classloader, construct Glassbox subsystem within it
		// and return the created reference
        
        //currently uses a non-delegating classloader here, to ensure isolation
        //longer term OSGI is more attractive

        try {
            String agentJarLocation = System.getProperty("glassbox.agent.location", null);
            // don't try default locations: require -D flag for obscure cases where the nondelegating loader is required
//            if (agentJarLocation == null) {
//                agentJarLocation = getDefaultJarLocation();
//            }
            ClassLoader childLoader;
            if (agentJarLocation != null) {
                childLoader = createChildLoader(agentJarLocation);
            } else {
                childLoader = GlassboxInitializer.class.getClassLoader();
            }
                
            Thread.currentThread().setContextClassLoader(childLoader);
            
            return (ApplicationLifecycleAware)Class.forName("glassbox.config.GlassboxInitializationImpl", true, childLoader).newInstance();
        } catch (Throwable t) {
            throw new AspectConfigurationException("Can't initialize glassbox", t);
        }
	}
    
    protected static ClassLoader createChildLoader(String agentJarLocation) throws MalformedURLException {
        ClassLoader parent = GlassboxInitializer.class.getClassLoader();
        URL jarDirUrl = parent.getResource(agentJarLocation);
        if (jarDirUrl != null) {
            directory = new File(jarDirUrl.getPath());
        } else {
            directory = new File(agentJarLocation);
        }
        String[] jars = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {                      
                return name.length() > 4 && name.substring((name.length() - 4), name.length()).equals(".jar");
            }
        });
        URL URLs[];
        if (jars == null) {
            URLs = new URL[1];
        } else {
            URLs = new URL[1+jars.length];
            for (int i = 0; i < jars.length; i++) {
                String jar = jars[i];
                URLs[i+1] = new File(directory, jar).toURI().toURL();
            }
        }
        // the URL ClassLoader requires a trailing forward slash (it's an URL) to properly recognize a directory!
        URLs[0] = directory.toURI().toURL();//new URL("file", null, directory.getCanonicalPath()+"/");
        
        ClassLoader childLoader = new NondelegatingURLClassLoader(URLs, parent);
        
        BootstrapLog.info("Glassbox using home directory: "+directory+" with URLs:");
        for (int i=0; i<URLs.length; i++) {
            BootstrapLog.info(URLs[i].toString());
        }
        
        return childLoader;
    }        
    
    protected static ApplicationLifecycleAware getInitialization() {        
        Thread shutThread = new Thread() {
            public void run() {
                GlassboxInitializer.stop();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutThread);
        return findInitialization();
    }

}
