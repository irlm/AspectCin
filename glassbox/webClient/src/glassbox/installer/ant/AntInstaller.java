/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.ant;

import glassbox.thread.context.SavedContextLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default properties used in Ant script:
 * lib.dir (for Glassbox jars)
 * glassbox.home (for Glassbox properties files)
 * bin.dir (for installation scripts)
 * launch.command
 * scripts.install.dir (the bin scripts to use)
 * glassbox.version
 * host.url
 * install.resource.dir
 * script.prefix (to prepend onto template script files)
 * java.opts.var (for the env variable to override script defaults)
 * java14 (presence triggers unless checks)
 * 
 */
public class AntInstaller {

	private static final Log log = LogFactory.getLog(AntInstaller.class);
	public static final String DEFAULT_SYSTEM = "default";

	File installResourceDir;
	String targetSystem;
	Map properties;

	public AntInstaller(File installResourceDir, Map properties) {
		this(installResourceDir, "default", properties);
	}
	
    public AntInstaller(File installResourceDir, String targetSystem, Map properties) {
        this.installResourceDir = installResourceDir;
        this.targetSystem = targetSystem;
        this.properties = properties;
    }

    public static void main(String[] args) {
    	File file = new File(args[0]);
    	AntInstaller installer = new AntInstaller(file, new HashMap());
    	installer.install();
    }
    
    public void install() {
		// HACK: Why? because Ant tries to find all the optional tasks,
		// then load the classes that implement them. 
		// When that happens across versions, trouble follows
    	// ... Why does doing it twice work? Good question. Read the Ant code and tell me! -JDH
    	try {
    		install2();
		} catch (Throwable th) {
			log.info("Got an Error, trying again...");
			install2();
			
//			Wow, can't even use this code...
			
//			// Why not just catch LinkageError in the first place?
//			// It doesn't work, don't know why... -JDH
//			if (th instanceof LinkageError) {
//				log.info("Got a LinkageError, trying again...");
//				install2();
//			} else {
//				throw new RuntimeException(th.getMessage(), th);
//			}
		}
    }
	public void install2() {
        SavedContextLoader savedContextLoader = new SavedContextLoader();
        try {
            ClassLoader loader=null;
			try {
				loader = buildClassLoader();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
            
            Installer installer = null;
			try {
				installer = (Installer)Class.forName("glassbox.installer.ant.AntInstaller2", true, loader).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}

    		installer.init(installResourceDir, targetSystem, properties);
    		installer.install();
        } finally {
            savedContextLoader.restore();
        }    
    }

	ClassLoader buildClassLoader() throws MalformedURLException {
		ClassLoader parent = getClass().getClassLoader();

		URL antURL =  findResource("install/ant-library/ant.jar");
		URL antLauncherURL =  findResource("install/ant-library/ant-launcher.jar");
		
		if (antURL==null || antLauncherURL==null) 
			throw new NullPointerException("Can't get Ant installer resources!");
		
		URL[] urls = {antURL, antLauncherURL};
		
        return new OverridingURLClassLoader("glassbox.installer.ant.AntInstaller2", urls, parent);
	}
	
	URL findResource(String name) {
		URL resource = getClass().getClassLoader().getResource(name);
		if (resource!=null) return resource;
		
		resource = getClass().getResource(name);
		if (resource!=null) return resource;

		resource = getClass().getResource("/"+name);
		if (resource!=null) return resource;

		resource = getClass().getClassLoader().getResource("/"+name);
		if (resource!=null) return resource;

		resource = Thread.currentThread().getContextClassLoader().getResource(name);
		if (resource!=null) return resource;

		resource = Thread.currentThread().getContextClassLoader().getResource("/"+name);
		if (resource!=null) return resource;
		
		if (resource==null)
			throw new IllegalStateException("Couldn't find resource: "+name);
		
		return resource;
	}
}
