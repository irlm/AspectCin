/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.ant;

import glassbox.util.DetectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

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
public class AntInstaller2 implements Installer {

	public final static String CONST = "FOO";
	
	private static final Log log = LogFactory.getLog(AntInstaller2.class);
	DefaultLogger fileLogger = null;
	File installResourceDir;
	String targetSystem;
	Map properties;
	public static final String DEFAULT_SYSTEM = "default";

	public AntInstaller2() {
	}
	
    public void init(File installResourceDir, String targetSystem, Map properties) {
        this.installResourceDir = installResourceDir;
        this.targetSystem = targetSystem;
        this.properties = properties;
    }

	public void install() {

		Project project=null;
		project = getProject();
    	
		try {
			project.fireBuildStarted();
            
            project.executeTarget("install.info");
            
            project.executeTarget("install");

			project.fireBuildFinished(null);
		} catch (BuildException e) {
			project.fireBuildFinished(e);
			log.error(e);
		}
    }

    Project getProject() {
		Project project = new Project();

		DefaultLogger consoleLogger = getLogger(System.err, System.out);
		project.addBuildListener(consoleLogger);
    	project.addBuildListener(createFileLogger());

    	File buildFile = getBuildFile();
		project.init();
    	
    	setProperties(project);
		
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		project.addReference("ant.projectHelper", helper);
		
		try {
		helper.parse(project, buildFile);
		} catch (Throwable th) {
			th.printStackTrace();
			throw new RuntimeException(th);
		}
    	
    	project.setUserProperty("ant.file", buildFile.getAbsolutePath());
    		
    	return project;
    }
    
	protected DefaultLogger createFileLogger() {
		DefaultLogger fileLogger = new DefaultLogger();
		
		File glassboxHome = new File((String)properties.get("glassbox.home"));
		if (!glassboxHome.exists()) glassboxHome.mkdirs();
		
		File logFile = new File(glassboxHome, "glassboxInstall.txt");
		PrintStream ps=null;
		try {
			ps = new PrintStream(new FileOutputStream(logFile));
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			log.warn("Can't write install log file.", e);
		}

		fileLogger.setErrorPrintStream(ps);
		fileLogger.setOutputPrintStream(ps);
		fileLogger.setMessageOutputLevel(Project.MSG_INFO);
		return fileLogger;
	}

    File getBuildFile() {
    	File result;
    	
    	File targetBuildFile = new File(new File(installResourceDir, targetSystem.toLowerCase()), "build.xml");
    	
    	if (targetBuildFile.isFile())
    		result = targetBuildFile;
    	else 
    		result = new File(new File(installResourceDir, DEFAULT_SYSTEM), "build.xml");
    	
    	log.warn("AntInstaller using build file: "+result.getAbsolutePath());
    	
    	return result;
	}

    void setProperties(Project project) {
		
		// find scripts directory (based on platform and runtime...)
		project.setProperty("java.opts.var", "JAVA_OPTS");
		project.setProperty("install.resource.dir", this.installResourceDir.getAbsolutePath());
		project.setProperty("scripts.install.dir", getScriptsDir().getAbsolutePath());
		project.setProperty("host.url", getHostURL());
		project.setProperty("target.system", this.targetSystem);

		for (Iterator iterator = this.properties.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			
			project.setProperty(key, (String)properties.get(key));
		}
	}
    
	String getHostURL() {
    	String hostIp = "127.0.0.1";
        
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostIp = addr.getHostAddress();
        } catch (Exception e) {
            ;// just use local host...
        }
        
        return hostIp;
	}

	/**
	 * this provides an ordering for looking up the previous version of the jvm
	 */
	static Map jvmVersions = new HashMap();
	static {
		jvmVersions.put("1.6", "1.5");
//		jvmVersions.put("1.5", "1.4"); // going to this as fallback doesn't make sense
	}
	
	File getScriptsDir() {
		File result;
		
    	String javaRuntime = Double.toString(DetectionUtils.getJavaRuntimeVersion());

    	while (javaRuntime != null) {
    		result = findScriptsDir(javaRuntime);
    		
    		if (result != null)
    			return result;
    		
    		javaRuntime = (String) jvmVersions.get(javaRuntime);
    	}
    	
    	return null;
	}

	private File findScriptsDir(String javaRuntime) {
		File result;
		
		File targetScriptsDir = new File(new File(installResourceDir, targetSystem.toLowerCase()), javaRuntime);
    	
    	if (targetScriptsDir.isDirectory()) {
    		result = targetScriptsDir;
    	} else { 
    		result = new File(new File(installResourceDir, DEFAULT_SYSTEM), javaRuntime);
    		
    		if (!result.isDirectory()) result = null;
    	}
    	
		return result;
	}

	protected DefaultLogger getLogger(PrintStream errorOut, PrintStream stdOut) {
		DefaultLogger fileLogger = new DefaultLogger();
		fileLogger.setErrorPrintStream(errorOut);
		fileLogger.setOutputPrintStream(stdOut);
		fileLogger.setMessageOutputLevel(Project.MSG_INFO);
		return fileLogger;
	}

}