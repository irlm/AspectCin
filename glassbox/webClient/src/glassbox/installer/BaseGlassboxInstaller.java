package glassbox.installer;

import glassbox.installer.ant.AntInstaller;
import glassbox.installer.util.WebappResourceLocator;
import glassbox.installer.web.helper.InstallValidationHelper;
import glassbox.util.DetectionUtils;

import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseGlassboxInstaller implements GlassboxInstaller {
    public final static String VERSION_START_TOKEN = "---";
    
	private static final String CONFIG_FILENAME = "installer.properties";
    
	private static final Log log = LogFactory.getLog(BaseGlassboxInstaller.class);

	public static final String INSTALL_LOG_NAME = "glassboxInstall.txt";

	private File customScript;

	private File customLibDir;

	protected ServletContext context;
	
	protected InstallValidationHelper installValidator = new InstallValidationHelper();

	public BaseGlassboxInstaller() {
		super();
	}
	
	public static interface Predicate {
	    public boolean matches(ClassLoader classloader);
	}
	
	/**
	 * Utility method to help match environments for exact ClassLoader names
	 * 
	 * @param className
	 */
	public boolean checkClassloadersNameMatchExactly(final String className) {
	    return checkClassloaders(new Predicate() { 
	        public boolean matches(ClassLoader classloader) {
	            log.debug("Testing match "+className+" "+(classloader.getClass().getName().equals(className)));
	            return classloader.getClass().getName().equals(className);
	        }
	    });
	}
	
    /**
     * Utility method to help match environments for ClassLoader prefixes
     * 
     * @param classPrefix
     */
    public boolean checkClassloadersNameMatchPrefix(final String classPrefix) {
        return checkClassloaders(new Predicate() { 
            public boolean matches(ClassLoader classloader) {
                log.debug("Testing prefix "+classPrefix+" "+(classloader.getClass().getName().startsWith(classPrefix)));
                return classloader.getClass().getName().startsWith(classPrefix);
            }
        });
    }
    
    public String getConfigureInstallerPageName() {
    	return "configureInstaller_BaseGlassboxInstaller";
    }
    
    public String getInstallerResultsPageName() {
    	return "installerResults_BaseGlassboxInstaller";
    }
    
    /**
     * Utility method to help match environments for ClassLoader substrings
     * 
     * @param classPrefix
     */
    public boolean checkClassloadersNameContains(final String classSubstring) {
        return checkClassloaders(new Predicate() { 
            public boolean matches(ClassLoader classloader) {
                log.debug("Testing contains "+classSubstring+" "+(classloader.getClass().getName().indexOf(classSubstring)!=-1));
                return classloader.getClass().getName().indexOf(classSubstring)!=-1;
            }
        });
    }
    
	public boolean checkClassloaders(Predicate predicate) {
		ClassLoader classloader = getClass().getClassLoader();
        //log.info("--->  Child Classloader: " + format(classloader));
        while ((classloader = classloader.getParent()) != null) {
        	if (predicate.matches(classloader))
        		return true;
        }
        
        return false;
	}
	
	/**
	 * Build the list of properties for install scripting
	 * 
	 * Properties set are:
	 * lib.dir
	 * bin.dir
	 * glassbox.dir
	 * launch.command
	 * glassbox.version (from installer.properties file)
	 * install.resource.dir
	 * target.system
	 * java14 (if true) 
	 * 
	 * @return
	 */
	protected Map getInstallProperties() {
		Map result = new HashMap(readProperties());
		
		safePropertySetter(result, "java.opts.var", getJavaOptsName());
		safePropertySetter(result, "lib.dir", this.getLibDirectory().getAbsolutePath());
		safePropertySetter(result, "bin.dir", this.getBinDirectory().getAbsolutePath());
		safePropertySetter(result, "glassbox.home", this.getGlassboxHome().getAbsolutePath());
		safePropertySetter(result, "launch.command", this.getLaunchCommand());
		safePropertySetter(result, "installer.resource.dir", this.getInstallerResourceDir().getAbsolutePath());
		safePropertySetter(result, "script.prefix", this.getScriptPrefix());
		
		if (DetectionUtils.isJava14())
			safePropertySetter(result, "java14", "true");
		
		return result;
	}
	
	protected void safePropertySetter(Map map, String key, String value) {
		if (value != null)
			map.put(key, value);
	}

    protected File getRootResourceDir() {
        return new File(WebappResourceLocator.getInstance().getApplicationHome(this.getContext()));
    }
    
	protected File getInstallerResourceDir() {
		return new File(getRootResourceDir(), "install");
	}

	private Properties readProperties() {
		Properties result = new Properties();
		InputStream is = null;
		try {
			try {
				if (System.getProperty(CONFIG_FILENAME) != null)
					is = new FileInputStream(System.getProperty(CONFIG_FILENAME));
				
				if (is == null)
					is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILENAME);
				
				if (is != null)
					result.load(is);
			} catch (IOException io) {
				log.error("IOException in Creating Configuration Factory: "+
						"  attempted Configuration file name: "
						+ CONFIG_FILENAME, io);
			}			
		} finally {
			if (is != null)
				try {is.close();} catch (IOException ex) {}
		}
		
		return result;
	}
	
	public void customParameters(Map parameterMap) {
		// no op
	}
	
	public void reset() {
		this.setCustomScriptToWrap(null);
		this.setCustomLibDirectory(null);
	}
	public File getScript() {
		File result = getCustomScriptToWrap();
		
		if (result == null)
			result = getDefaultScript();
		
		return result;
	}
    
	public File getLibDirectory() {
		File result = getCustomLibDirectory();
		
		if (result == null)
			result = getDefaultLibDirectory();
		
		return result;
	}

	public File getDefaultLibDirectory() {
		return new File(getContainerHome(), "lib");
	}

	public File getBinDirectory() {
		File result;
		
		File customScript = getCustomScriptToWrap();
		if (customScript != null) {
			result = customScript.getParentFile();
		} else {
			result = getDefaultBinDirectory();			
		}
		
		return result;
	}
	
	public File getDefaultBinDirectory() {
		return new File(getContainerHome(), "bin");
	}
	
	public String getFormattedEnvVars() {
		StringBuffer result = new StringBuffer();
		String opts = getJavaOptsName(); 
		
        result.append("set GLASSBOX_BIN_DIR="+getBinDirectory().getAbsolutePath()+"<br/><br/>\n");
        result.append("set GLASSBOX_LIB_DIR="+getLibDirectory().getAbsolutePath()+"<br/><br/>\n");
        result.append("set GLASSBOX_HOME="+getGlassboxHome().getAbsolutePath()+"<br/><br/>\n");
        
		if (DetectionUtils.isJava14()) {
			if (isWindows()) {
				result.append("set "+opts+"=%"+opts+"% \"-Xbootclasspath/p:%GLASSBOX_LIB_DIR%\\java14Adapter.jar\" " +
						"\"-Xbootclasspath/a:%GLASSBOX_LIB_DIR%\\createJavaAdapter.jar;%GLASSBOX_LIB_DIR%\\aspectj14Adapter.jar;" +
						"%GLASSBOX_LIB_DIR%\\aspectjweaver.jar;%GLASSBOX_LIB_DIR%\\glassboxMonitor.jar;%GLASSBOX_LIB_DIR%\\aspectjrt.jar\" " +
						"-Daspectwerkz.classloader.preprocessor=org.aspectj.ext.ltw13.ClassPreProcessorAdapter " +
						"\"-Dglassbox.install.dir=%GLASSBOX_HOME%\""+extraEnvVars()+"<br/>\n");
			} else {
				result.append("export "+opts+"=\"$"+opts+" -Xbootclasspath/p:$GLASSBOX_LIB_DIR/java14Adapter.jar " +
						"-Xbootclasspath/a:$GLASSBOX_LIB_DIR/createJavaAdapter.jar:$GLASSBOX_LIB_DIR/aspectj14Adapter.jar:" +
						"$GLASSBOX_LIB_DIR/aspectjweaver.jar:$GLASSBOX_LIB_DIR/glassboxMonitor.jar:$GLASSBOX_LIB_DIR/aspectjrt.jar " +
						"-Daspectwerkz.classloader.preprocessor=org.aspectj.ext.ltw13.ClassPreProcessorAdapter " +
						"-Dglassbox.install.dir=$GLASSBOX_HOME\"" +extraEnvVars()+"<br/>\n");
			}			
			
		} else {// Java 1.5+
			if (isWindows()) {
				result.append("set "+opts+"=%"+opts+"% \"-javaagent:%GLASSBOX_LIB_DIR%\\aspectjweaver.jar\" " +
						"\"-Dglassbox.install.dir=%GLASSBOX_HOME%\"" +extraEnvVars()+"<br/>\n");
			} else {
				result.append("export "+opts+"=\"$"+opts+" -javaagent:$GLASSBOX_LIB_DIR/aspectjweaver.jar " +
						"-Dglassbox.install.dir=$GLASSBOX_HOME "+extraEnvVars()+"\"<br/>\n");
			}			
		}
		
		return result.toString();
	}
	
	protected String extraEnvVars() {
	    return "";
	}
	
	public File getDefaultScript() {
		File scriptFile = new File(getDefaultBinDirectory(), getDefaultScriptPrefix()+getPlatformExtension());
		if (scriptFile.isFile())
			return scriptFile;
		else 
			return null;
	}

    abstract public File getContainerHome();
    
    public String getJavaOptsSubst() {
    	if (isWindows())
    		return "%@"+getJavaOptsName()+"@%";
    	else
    		return "$@"+getJavaOptsName()+"@";
    }

	protected String getJavaOptsName() {
		return "JAVA_OPTS";
	}

	/**
     * If null no container script is identified
     * 
     * @return null or the complete launch command including args
     */
    public String getLaunchCommand() {
    	File script = getScript();
    	
    	if (script != null) {
    		return script.getAbsolutePath();
    	} else {
    		return null;
    	}
    }
    

	/**
     * If null then no script provided
     * 
     * @return null or the complete launch command including args
     */
    public String getGlassboxLaunchCommand() {
    	File script = getScript();
    	
    	if (script != null) {
    	    
    		File parent = script.getParentFile();
    		String name = script.getName();
    		
    		int dotIndex = name.lastIndexOf('.');
    		if (dotIndex != -1)
    			name = name.substring(0, dotIndex);
    		
    		//testing
    		if (!getScriptPrefix().equals(name)) {
    		    log.error("unequal prefixes "+name+", "+getScriptPrefix());
    		}    		    
    		
    		name = getScriptPrefix() + "_with_glassbox" + getDefaultPlatformExtension();
    		
    		return new File(parent, name).getAbsolutePath();
    	} else {
    		return null;
    	}
    }

    /**
     * 
     * @return
     */
    public String getScriptPrefix() {
    	File customScript = getCustomScriptToWrap();
    	if (customScript == null) {
    		return getDefaultScriptPrefix();
    	} else {
    		String name = customScript.getName();
    		String baseName;
    		
    		int dotIndex = name.lastIndexOf('.');
    		
    		if (dotIndex == -1)
    			baseName = name;
    		else
    			baseName = name.substring(0, dotIndex);
    		
    		return baseName;
    	}
    }
    
    /**
     */
	public String getDefaultScriptPrefix() {
		return getTargetSystemLabel().toLowerCase();
	}

	public File getGlassboxHome() {
		return new File(this.getLibDirectory(), "glassbox");
	}

	public File getInstallLog() {
		return new File(getGlassboxHome(), INSTALL_LOG_NAME);
	}

	public boolean isInstalled() {
		return installValidator.getErrorStatus() == InstallValidationHelper.INSTALL_OK;		
	}
	
    public boolean isPartiallyInstalled() {
    	return installValidator.detectPartialInstall(this, context);
    }

    public void install() throws InstallException {
    	AntInstaller antInstaller = 
    		new AntInstaller(
    				getInstallerResourceDir(), 
    				getTargetSystemLabel(), 
    				getInstallProperties());
    	
    	log.info("Install Glassbox with "+antInstaller);
    	antInstaller.install();
	}

	public String getFullJavaVersion() {
        return DetectionUtils.getFullJavaVersion();
    }
    
    public String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

	
	public String getVersion() {
		return getVersion(getInstallLog());
	}
	
    public void setCustomScriptToWrap(File customScript) {
    	if (customScript == null) {
    		this.customScript = null;
    		return;
    	}
    	
    	if (!customScript.exists()) {
    		// TODO Symlinks seem to fail this test!
    		throw new RuntimeException("The script file ["+customScript.getPath()+"] does not exist.");
    	}
   
    	if (!customScript.getParentFile().canWrite())
    		throw new RuntimeException("The directory ["+customScript.getParentFile().getPath()+"] is read-only.");
    	
    	this.customScript = customScript;    	
    }
    public File getCustomScriptToWrap() {
    	return this.customScript;
    }
    
    public void setCustomLibDirectory(File customLibDirectory) {
    	if (customLibDirectory == null) {
    		this.customLibDir = null;
    		return;
    	}

    	if (!customLibDirectory.isDirectory())
    		throw new RuntimeException("The path ["+customLibDirectory.getPath()+"] is not a directory.");

    	if (!customLibDirectory.canWrite())
    		throw new RuntimeException("The directory ["+customLibDirectory.getPath()+"] is read-only.");

    	this.customLibDir = customLibDirectory;
    }
    public File getCustomLibDirectory() {
    	return this.customLibDir;
    }


    private String getVersion(File file) {
        String version = "No Version Info.";
        String path = file.getAbsolutePath();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String next = null;
            while ((next = reader.readLine()) != null) {
                if (next.indexOf(VERSION_START_TOKEN) >= 0) {
                    int startIndex = next.indexOf(":");
                    int lastIndex = next.lastIndexOf(VERSION_START_TOKEN);

                    if (startIndex > 0 && lastIndex > startIndex) {
                        return next.substring((startIndex + 2), (lastIndex - 1));
                    }
                }
            }
        } catch (Exception e) {
            try {
                path = file.getCanonicalPath();
            } catch (Throwable t) {
                ;
            }
            String msg = "Can't read Glassbox installer file: ";
            if (e instanceof FileNotFoundException) {
                msg = "Glassbox installation log file missing: ";
            }
            log.info(msg + path);
            log.debug("Cause ", e);
        }
        return version;
    }

    static class ContainerLookupLogic {
    	ServletContext context;
		ContainerLookupLogic(ServletContext context) {
    		this.context = context;
    	}
		
    }

	public ServletContext getContext() {
		if (context == null)
			throw new NullPointerException("fail fast"); //fail fast
		
		return context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
	}

	public String getPlatformExtension() {
		return getDefaultPlatformExtension();
	}

	public static String getDefaultPlatformExtension() {
		if (isWindows())
			return ".bat";
		else
			return ".sh";
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os != null && os.indexOf("indow") >= 0) {
			return true;
		}
		return false;
	}
}