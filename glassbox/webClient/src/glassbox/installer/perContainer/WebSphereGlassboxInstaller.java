package glassbox.installer.perContainer;

import glassbox.installer.BaseGlassboxInstaller;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Important? system properties:
 * was.install.root=C:/Program Files/IBM/WebSphere/AppServer
 * server.root=C:\Program Files\IBM\WebSphere\AppServer\profiles\AppSrv01
 * was.repository.root=C:\Program Files\IBM\WebSphere\AppServer\profiles\AppSrv01\config
 * 
 * Servlet Properties
 * getServerInfo()=IBM WebSphere Application Server/6.1
 * getRealPath("/")=C:\Program Files\IBM\WebSphere\AppServer\profiles\AppSrv01\installedApps\homebodyNode01Cell\glassbox.ear\glassbox.war
 * getRealPath("/glassbox")=C:\Program Files\IBM\WebSphere\AppServer\profiles\AppSrv01\installedApps\homebodyNode01Cell\glassbox.ear\glassbox.war\glassbox
 * getResource("/")=file:/C:/Program Files/IBM/WebSphere/AppServer/profiles/AppSrv01/installedApps/homebodyNode01Cell/glassbox.ear/glassbox.war/
 * 
 * ClassLoaders:
 * com.ibm.ws.classloader.CompoundClassLoader
 * com.ibm.ws.classloader.JarClassLoader
 * com.ibm.ws.classloader.ProtectionClassLoader
 * com.ibm.ws.bootstrap.ExtClassLoader
 * org.eclipse.osgi.framework.adaptor.core.CDSBundleClassLoader
 * sun.misc.Launcher$AppClassLoader
 * sun.misc.Launcher$ExtClassLoader
 * 
 */
public class WebSphereGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
        return getWebSphereProfileDir(this.getContext());
	}
	
	public String getTargetSystemLabel() {
		return "WebSphere";
	}
	
	public String getDefaultScriptPrefix() {
		return "";
	}	

	protected String getJavaOptsName() {
		return "JAVA_OPTIONS";
	}
	    
	    
    private static final Log log = LogFactory.getLog(WebSphereGlassboxInstaller.class);
	
    public boolean matchesContext(ServletContext context) {	    
		boolean result = (System.getProperty("was.install.root") != null &&
							getWebSphereProfileDir(context) != null) || 
							checkClassloadersNameMatchPrefix("com.ibm.ws.classloader.");

		return result;
	}
	
	/**
	 * Search for the domains directory in a Weblogic installation. Returns null if not found.
	 */
	public static File getWebSphereProfileDir(ServletContext context) {
		return new File(System.getProperty("was.repository.root"));
	}
}
