package glassbox.installer.perContainer;

import glassbox.installer.BaseGlassboxInstaller;
import glassbox.util.DetectionUtils;

import java.io.*;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TomcatGlassboxInstaller extends BaseGlassboxInstaller {
    private static final Log log = LogFactory.getLog(TomcatGlassboxInstaller.class);

    public static final int WINDOWS_EXE_YES = 1;
    public static final int WINDOWS_EXE_MAYBE = 2;
    public static final int WINDOWS_EXE_NO = 3;
    int windowsExeInstall = 3;
    
    /**
     * This is set from configure page form parameters
     */
    boolean forceWindowsExe = false;

    /**
	 * Tomcat 4 and 5 uses common/lib
	 * Tomcat 6 uses lib
	 */
	public File getDefaultLibDirectory() {
		File containerHome = getContainerHome();
		
		File common = new File(containerHome, "common");
		
		if (common.isDirectory())
			return new File(common, "lib");
		else
			return new File(containerHome, "lib");
	}

	public File getContainerHome() {
        return new File(getCatalinaHomeBaseProperty());
	}

	public File getDefaultScript() {
		if (!isWindows())
			return super.getDefaultScript();
		
        File binDirectory = getDefaultBinDirectory();
		File exeFile = findFile(binDirectory, "tomcat", ".exe");
		File batchFile = findFile(binDirectory, "startup", getPlatformExtension());
		
		if (exeFile != null && batchFile != null) {
			this.windowsExeInstall = WINDOWS_EXE_MAYBE;
			return batchFile;
		} else if (exeFile != null && batchFile == null) {
			this.windowsExeInstall = WINDOWS_EXE_YES;
			return exeFile;
		} else if (exeFile == null && batchFile != null) {
			this.windowsExeInstall = WINDOWS_EXE_NO;
			return batchFile;
		}
		
		return null;
	}

	public String getLaunchCommand() {
		if (this.windowsExeInstall == WINDOWS_EXE_YES || this.forceWindowsExe) 
			return null; // exe install should not generate wrapper scripts
		else
			return super.getLaunchCommand();

	}
	
	/**
	 * Need to empty the script.prefix property because the build.xml handles it specially
	 */
	protected Map getInstallProperties() {
		Map installProperties = super.getInstallProperties();
		installProperties.put("script.prefix", "");
		
		return installProperties;
	}
	
	File findFile(File binDirectory, final String prefix, final String suffix) {
		File[] listFiles = binDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File parent, String name) {
                name = name.toLowerCase();
                return name.startsWith(prefix.toLowerCase()) && name.endsWith(suffix.toLowerCase());
            }
        });
		
		if (listFiles.length > 0)
			return listFiles[0];
		else
			return null;
		
	}

	
	/**
	 * If this is a Tomcat EXE install with no custom script
	 * then display a special page
	 */
	public String getInstallerResultsPageName() {
		if (this.windowsExeInstall == WINDOWS_EXE_YES || this.forceWindowsExe) 
	    	return "installerResults_TomcatEXEInstaller";
		else
			return super.getInstallerResultsPageName();
	}
	
	public String getConfigureInstallerPageName() {
		if (this.windowsExeInstall == WINDOWS_EXE_YES) {
		    forceWindowsExe = true; // the detection value changes when we unpack the wrapper scripts... this is a workaround: I'd sooner we didn't unpack them
	    	return "configureInstaller_TomcatEXEInstaller";
		} else if (this.windowsExeInstall == WINDOWS_EXE_MAYBE) {
		    	return "configureInstaller_TomcatEXEChoiceInstaller";
		} else {
			return super.getConfigureInstallerPageName();
		}
	}
	
	public String getTargetSystemLabel() {
		return "Tomcat";
	}

	public String getDefaultScriptPrefix() {
		return "startup";
	}
	
	public String getFormattedExeVars() {
		StringBuffer result = new StringBuffer();
		String opts = getJavaOptsName(); 
		
        result.append("-Dglassbox.install.dir="+getGlassboxHome().getAbsolutePath()+"<br/><br/>\n");
        result.append("-Djava.rmi.server.useCodebaseOnly=true<br/><br/>\n");
        
		if (DetectionUtils.isJava14()) {
			throw new RuntimeException("Java 14 not currently supported for Tomcat Windows Service Installation.");
		} else {// Java 1.5+
			if (isWindows()) {
				result.append("-javaagent:"+getLibDirectory().getAbsolutePath()+"\\aspectjweaver.jar<br/>\n");
			} else {
				result.append("-javaagent:"+getLibDirectory().getAbsolutePath()+"/aspectjweaver.jar<br/>\n");
			}			
		}
		
		return result.toString();
	}
	
	public void customParameters(Map parameterMap) {
		try {
			String[] paramValues = (String[]) parameterMap.get("tomcatType");
			log.info("Setting customParameters: tomcatType="+paramValues[0]);
			this.forceWindowsExe = "service".equals(paramValues[0]);
		} catch (Exception ex) {}

		log.info("TomcatGlassboxInstaller.forWindowsExe="+forceWindowsExe);
//		throw new RuntimeException("break");
	}
	
	public boolean matchesContext(ServletContext context) {
        boolean result = 
            (getCatalinaHomeBaseProperty() != null) || checkClassloadersNameMatchPrefix("org.apache.catalina.loader");
        
        //initialize
        if (result) {
        	getDefaultScript();
        }
        
        return result;
	}

	String getCatalinaHomeBaseProperty() {
		return System.getProperty("catalina.home", System.getProperty("catalina.base", ""));
	}

    protected String extraEnvVars() {
        return " -Djava.rmi.server.useCodebaseOnly=true";
    }
}
