package glassbox.installer.perContainer;

import glassbox.installer.BaseGlassboxInstaller;
import glassbox.installer.GlassboxInstallerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Important? system properties:
 * wls.home=/home/jheintz/projects/glassbox/glassbox-smoke/bea/wlserver_10.0/server
 * platform.home=/home/jheintz/projects/glassbox/glassbox-smoke/bea/wlserver_10.0
 * weblogic.home=/home/jheintz/projects/glassbox/glassbox-smoke/bea/wlserver_10.0/server
 * weblogic.Name=examplesServer
 * 
 * ServletContext.getResource("/")="file:/home/jheintz/projects/glassbox/glassbox-smoke/bea/wlserver_10.0/samples/domains/wl_server/servers/examplesServer/tmp/_WL_user/_appsdir_glassbox_war/346ozk/war/"
 * 
 */
public class WeblogicGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
        return getWebLogicDomainDir(this.getContext());
	}
	
	public String getTargetSystemLabel() {
		return "WebLogic";
	}
	
	public String getDefaultScriptPrefix() {
		return "startWebLogic";
	}	

	protected String getJavaOptsName() {
		return "JAVA_OPTIONS";
	}
	    
	    
    private static final Log log = LogFactory.getLog(GlassboxInstallerFactory.class);
	
    public boolean matchesContext(ServletContext context) {	    
		boolean result = (System.getProperty("weblogic.home") != null &&
							getWebLogicDomainDir(context) != null) || 
							checkClassloadersNameMatchPrefix("weblogic.");

		if (log.isDebugEnabled()) {
		    log.debug("Is it weblogic? "+result+": Do ClassLoaders match? "+checkClassloadersNameMatchPrefix("weblogic."));
		}
		return result;
	}

	protected File getInstallerResourceDir() {
		File root = getExplodedResourceFile(this.getContext());
		return new File(root, "install");
	}
	
	/**
	 * Search for the domains directory in a Weblogic installation. Returns null if not found.
	 */
	public static File getWebLogicDomainDir(ServletContext context) {
	    File basedir = getExplodedResourceFile(context);
	    
	    File current = basedir;
	    while (current != null) {
	    	File bin = new File(current, "bin");
	    	if (bin.isDirectory()) {
		    	File[] listFiles = bin.listFiles(new FilenameFilter() {
		            public boolean accept(File dir, String name) {
		                return name!=null && name.toLowerCase().startsWith("startWebLogic.");
		            }                    
		        });
	    		
		    	if (listFiles.length > 0)
		    		return current;
	    	}

	    	current = current.getParentFile();
	    }
	    
        //log.warn("Can't find Weblogic domain directory from "+basedir);
	    
		return null;
	}
	
	public static File getExplodedResourceFile(ServletContext context) {
		try {
			URL url = context.getResource("/");
			String path = url.getFile();
			
			if (path.startsWith("file:"))
				path = path.substring("file:".length(), path.length());

			return new File(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
