/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.web.helper;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import glassbox.agent.control.api.GlassboxService;
import glassbox.client.remote.AgentConnectionException;
import glassbox.client.remote.LocalAgentClient;
import glassbox.config.GlassboxInitializer;
import glassbox.installer.GlassboxInstaller;
import glassbox.util.DetectionUtils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InstallValidationHelper {
	public static final String INSTALL_OK = "0";
    protected static final String GLASSBOX_PROPERTIES_FILENAME = "glassbox.properties";
	private RuntimeMXBean runtimeMXBean;
	private static final Log log = LogFactory.getLog(InstallValidationHelper.class);
	private static final String PARAM_PREFIX = GlassboxInitializer.CONFIG_DIR_PROPERTY;
    private static final String CONNECTION_STRING = "service:jmx:rmi://localhost:7232/jndi/rmi://localhost:7232/GlassboxTroubleshooter";
    public static final String PORT_DELIM = "!";
    public final static String LINE_BREAK = "<br/>\n";
    private Socket skt;

	/**
	 * public String getErrorStatus(String connectionStatus)
	 * @param connectionStatus
	 * @return the status and the first error condition found
	 * 
	 * The error codes are as follows:
	 * 
	 * 1 - glassbox.install.dir parameter not set or set incorrectly
	 * 2 - JMX/RMI connection failure
	 * 3 - javaagent parameter not set or set incorrectly
	 * 4 - bootclasspath entries set incorrectly
	 * 5 - agent.jar file not on the classpath
	 * 6 - glassboxMonitor.jar file not on the classpath
	 * 7 - aspectwerkz.classloader.preprocessor not set or set incorrectly
	 */
	public String getErrorStatus() {
		String errorStatus = INSTALL_OK;
		String aspectError = checkRuntimeAspectParameters();
		String glassboxInstallDirError = isGlassboxInstallDirSet();
		String aspectWerkzError = isAspectWerkzProcessorSet();
		
		if(glassboxInstallDirError != null) {
			errorStatus = "1" + glassboxInstallDirError;
		} else if(aspectError != null) {
			if(useJavaAgent()) {
				errorStatus = "3" + aspectError;
			} else if(!isBEA()) {
				errorStatus = "4" + aspectError;
			}
		} else if(!canLoadClass("glassbox.agent.control.GlassboxServiceImpl")) {
			errorStatus = "5";
		} else if(!canLoadClass("glassbox.monitor.MonitoredType")) {
			errorStatus = "6";
		} else if(aspectWerkzError != null) {
			errorStatus = "7" + aspectWerkzError;
		}
		
		log.debug("error status: " + errorStatus);
		return errorStatus;
	}
	
	public boolean canLoadClass(String className) {
		try {
			Class.forName(className);
		} catch(Exception e) {
			log.error("Could not load class " + className, e);
			return false;
		}
		return true;
	}
	
	public String checkRuntimeAspectParameters() {
		String error = null;
		if(useJavaAgent()) {
			error = isJavaagentSet();
		} else if(!isBEA()) {
			error = isBootClasspathSet();
		}
		return error;
	}
	
	protected String isJavaagentSet() {
		String error = null;
		boolean found = false;
		if(runtimeMXBean == null) {
			runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		}
		List args = runtimeMXBean.getInputArguments();
		for(Iterator iter = args.iterator(); iter.hasNext(); ) {
			String arg = (String) iter.next();
			log.debug("startup parameter: " + arg);
			if(arg.indexOf("-javaagent:") >= 0) {
				found = true;
				log.debug("aspectjweaver jar present: " + (arg.indexOf("aspectjweaver.jar") < 0 ? "yes" : "no"));
				// work-around to handle paths with spaces
				if(arg.indexOf("aspectjweaver.jar") < 0) {
					String nextArg = (String) iter.next();
					while(!nextArg.startsWith("-")) {
						arg += " " + nextArg;
						log.debug("concatenated startup parameter: " + arg);
						nextArg = (String) iter.next();
					}
				}
				error = checkAspectJWeaverJarPresent(arg);
			}
		}
		if(!found) {
			error = "the <i>-javaagent</i> parameter was not set";
		}	
		return error;
	}
	
	protected String isBootClasspathSet() {
		String error = null;
		String bootClassPath = System.getProperty("sun.boot.class.path");
		StringBuffer errorBuf = new StringBuffer();
		String java14Error = isJarFilePresent(bootClassPath, "java14Adapter.jar");
		if(java14Error != null) {
			errorBuf.append(java14Error);
			errorBuf.append(LINE_BREAK);
		}
		String createJavaError = isJarFilePresent(bootClassPath, "createJavaAdapter.jar");
		if(createJavaError != null) {
			errorBuf.append(createJavaError);
			errorBuf.append(LINE_BREAK);
		}
		String aspectj14Error = isJarFilePresent(bootClassPath, "aspectj14Adapter.jar");
		if(aspectj14Error != null) {
			errorBuf.append(aspectj14Error);
			errorBuf.append(LINE_BREAK);
		}
		String aspectjweaverError = isJarFilePresent(bootClassPath, "aspectjweaver.jar");
		if(aspectjweaverError != null) {
			errorBuf.append(aspectjweaverError);
			errorBuf.append(LINE_BREAK);
		}
		if(errorBuf.length() > 0) {
			error = errorBuf.toString();
		}
		return error;
	}
	
	protected String isJarFilePresent(String bootClassPath, String jarFileName) {
		String error = null;
		int jarFileIndex = bootClassPath.indexOf(jarFileName);
		if(jarFileIndex >= 0) {
			int startIndex = bootClassPath.substring(0, jarFileIndex).lastIndexOf(File.pathSeparator);
			int endIndex = bootClassPath.indexOf(File.pathSeparator, jarFileIndex);
			if(endIndex < 0) {
				// We fell off the end
				endIndex = bootClassPath.length();
			}
			try {
				File jarFile = new File(bootClassPath.substring(startIndex + 1, endIndex));
				if(!jarFile.exists()) {
					error = "Could not find " + jarFileName + " in path " + jarFile.getParentFile().getCanonicalPath();
				}
			} catch(Exception e) {
				log.error("Exception thrown", e);
				error = "Exception thrown: " + e.getMessage();
			}
		} else {
			error = "Required jar file " + jarFileName + " not found in boot classpath";
		}
		return error;
	}
	
	private boolean useJavaAgent() {
		return DetectionUtils.getJavaRuntimeVersion() >= 1.5;
	}
	
	private boolean isBEA() {
		return System.getProperty("java.vm.vendor").indexOf("BEA") >= 0;
	}
	
	private String checkAspectJWeaverJarPresent(String arg) {
		String error = null;
		log.debug("aspectjweaver jar present: " + (arg.indexOf("aspectjweaver.jar") < 0 ? "yes" : "no"));
		if(arg.indexOf("aspectjweaver.jar") < 0)
			return "The aspectjWeaver agent jar file was not specified in the parameter list";

		File file = new File(arg.substring(arg.indexOf(':') + 1));
		try {
			JarInputStream input = new JarInputStream(new FileInputStream(file));
			JarEntry entry = null;
			boolean found = false;
			while((entry = input.getNextJarEntry()) != null) {
				if(entry.getName().indexOf("LTWeaver") >= 0) {
					log.debug("aspectjweaver jar is valid");
					found = true;
					break;
				}
			}
			if(!found) {
				error = "aspectjweaver jar is not valid: LTWeaver class not found";	
			}
		} catch (FileNotFoundException e) {
			error = "aspectjweaver.jar was not found";
			log.error(error, e);
		} catch (IOException e) {
			error = "java.io.IOException thrown";
			log.error(error, e);
		}
		
		return error;
	}
	
	public String isGlassboxInstallDirSet() {
		String error = null;
		
		String installDir = System.getProperty(PARAM_PREFIX);
		if(installDir != null) {
			error = isGlassboxInstallDirValid(installDir);
		} else {
			error = "the <i>-D" + PARAM_PREFIX + "</i> parameter was not set";
		}
		
		return error;
	}
	
	private String isGlassboxInstallDirValid(String installPath) {
		String error = null;
		if(!new File(installPath).exists()) {
			error = "The glassbox install dir parameter setting does not point to a valid directory:</br>\n" + installPath;
		} else if(!new File(installPath + File.separator + "glassbox.properties").exists()) {
			error = "The glassbox.properties file is not present in the glassbox install directory";
		}
		
		return error;
	}
	
	public String isAspectWerkzProcessorSet() {
		String error = null;
		if( !(useJavaAgent() || isBEA()) ) {
			String prop = System.getProperty("aspectwerkz.classloader.preprocessor");
			if(prop == null) {
				error = "the <i>-Daspectwerkz.classloader.preprocessor</i> parameter was not set";
			} else if(!prop.equals("org.aspectj.ext.ltw13.ClassPreProcessorAdapter")) {
				error = "the <i>-Daspectwerkz.classloader.preprocessor</i> parameter was set incorrectly." + 
					LINE_BREAK +
					"It should be set to <i>org.aspectj.ext.ltw13.ClassPreProcessorAdapter</i>";
			}
		}
		return error;
	}
	
	public void setRuntimeMXBean(RuntimeMXBean runtimeMXBean) {
		this.runtimeMXBean = runtimeMXBean;
	}
    
    public boolean detectPartialInstall(GlassboxInstaller installer, ServletContext context) {
    	return findFile(installer, context, GLASSBOX_PROPERTIES_FILENAME) ||
    		findFile(installer, context, "glassboxMonitor.jar") ||
     		getErrorStatus().equals(INSTALL_OK);
    }
    
    protected boolean findFile(GlassboxInstaller installer, ServletContext context, String fileName) {
    	boolean found = false;
    	// search working dir first
    	File dbPropFile = new File(fileName);
    	if(dbPropFile.exists()) {
    		found = true;
    	} else {
    		File containerDir = installer.getContainerHome();
    		found = searchSubDirs(containerDir, fileName);
    	}
    	
    	if (found)
    		log.info("Found! " + fileName);
    	
    	return found;
    }

    
    protected boolean searchSubDirs(File file, String soughtFileName) {
    	return searchSubDirs(file, soughtFileName, 2);
    }

    protected boolean searchSubDirs(File file, String soughtFileName, int depthCheck) {
    	log.trace("Searching for "+soughtFileName+", in "+file);
    	boolean found = false;
    	
    	// we don't want to check everywhere...
    	if (depthCheck == 0) {
    		return false;
    	} else {
    		depthCheck--;
    	}
    	
    	try {
    		if (file == null) {
    			// do nothing
    		} else if(!file.isDirectory() && file.getName().equals(soughtFileName)) {
	    		found = true;
	    	} else if (!"install".equals(file.getName())) { // don't look inside the install directory of Glassbox!
	    		File[] children = file.listFiles();
	    		if (children != null) {
		    		for(int i=0; i<children.length; i++) {
		    			File child = children[i];
		    			if (child != null) { // null check is from some strange linux device directory walking...
							found = searchSubDirs(child, soughtFileName, depthCheck);
			    			if(found) {
			    				break;
			    			}	    				
		    			}
		    		}	    			
	    		}
	    	}

		} catch (Exception e) {
			log.error("Exception occurred searching for file.", e);
			throw new RuntimeException(e);
		}
		return found;
    }
    
    // This method is for testing only
    public void setSocket(Socket skt) {
    	this.skt = skt;
    }

    // this doesn't detect much: only some outright failure to grab the socket where no one else has it :-(
    public String testConnection(String URL) {
        try {
            LocalAgentClient.getInitializedService();
        } catch (AgentConnectionException e1) {
            return "7232" + PORT_DELIM + " "+e1.getMessage();
        } catch (InterruptedException e1) {
            return "7232" + PORT_DELIM + " "+e1.getMessage();
        }

        String error = null;
        if(URL == null) {
            URL = getServiceURL();  
        }
        log.debug("Test Connection for: "+URL);
        
        int hostIndex = URL.lastIndexOf("//") + 2;
        int portIndex = URL.lastIndexOf(":");
        int endPortIndex = URL.lastIndexOf("/");
        if(hostIndex > 0 && portIndex > hostIndex && endPortIndex > portIndex) {
            String hostName = URL.substring(hostIndex, portIndex);
            int port = Integer.parseInt(URL.substring(portIndex + 1, endPortIndex));
            try {
                if(skt == null) {
                    skt = new Socket(hostName, port); //skt.setSoTimeout(2000);
                } else {
                    skt.connect(new InetSocketAddress(hostName, port), 2000);
                }
            } catch(Exception e) {
                error = port + PORT_DELIM + e.getMessage();
            } finally {           
                try {
                    if(skt!=null)
                        skt.close();
                } catch (IOException e) {                      
                    error = port + PORT_DELIM + e.getMessage();
                }
            }
        } else {
            error = "7232" + PORT_DELIM + "Attempted to connect to the agent via a malformed JMX URL:<br/>\n" + URL;
        }
        skt=null;
        log.debug("Connect test result: "+error);
        return error;
    }
    
    protected String getServiceURL() {
        Properties props = new Properties();
        
        try {
            props.load(new FileInputStream(GlassboxInitializer.getConfigDir() + File.separator + "glassbox.properties"));
        } catch(Exception e) {
            // Just return the default
        }
        
        return props.getProperty("glassboxJmxServerConnector.serviceUrl", CONNECTION_STRING);
    }
       
}