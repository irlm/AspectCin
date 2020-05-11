package glassbox.installer.perContainer;

import java.io.File;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

/**
 * This applies to OC4J versions 9.x and 10.1.2.x
 * 
 * System Properties from 9.x:
 * oracle.j2ee.container.name=Oracle Application Server Containers for J2EE 10g (9.0.4.0.0)
 * oracle.xdkjava.compatibility.version=9.0.3
 * oracle.j2ee.home=/home/jheintz/projects/glassbox/glassbox-smoke/oc4j9/j2ee/home
 * oracle.j2ee.container.version=9.0.4.0.0
 * 
 * System Properties from 10.1.2.x:
 * oracle.j2ee.container.name=Oracle Application Server Containers for J2EE 10g (10.1.2.0.2)
 * oracle.xdkjava.compatibility.version=9.0.3
 * oracle.j2ee.home=/home/jheintz/projects/glassbox/glassbox-smoke/oc4j10.1.2/j2ee/home
 * oracle.j2ee.container.version=10.1.2.0.2
 * 
 * @author jheintz
 *
 */
public class OC4JEvermindGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
		return new File(System.getProperty("oracle.j2ee.home"));
	}

	public File getDefaultScript() {
		return new File(getContainerHome(), "oc4j.jar");
	}

	public String getLaunchCommand() {
		return "java "+getJavaOptsSubst()+" -jar oc4j.jar -userThreads";
	}
	
	public File getDefaultBinDirectory() {
		return getContainerHome();
	}
	
	public String getTargetSystemLabel() {
		return "OC4J";
	}

	public boolean matchesContext(ServletContext context) {
		String version = System.getProperty("oracle.j2ee.container.version", "");
		return version.startsWith("9.") || version.startsWith("10.1.2") ||            
		    checkClassloadersNameMatchPrefix("com.evermind.naming.ContextClassLoader");
	}

}
