package glassbox.installer.perContainer;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

/**
 * This applies to OC4J version 10.1.3.x
 * 
 * System Properties:
 * oracle.j2ee.container.name=Oracle Containers for J2EE 10g (10.1.3.2.0) 
 * oracle.xdkjava.compatibility.version=9.0.3
 * oracle.home=/home/jheintz/projects/glassbox/glassbox-smoke/oc4j
 * oracle.j2ee.home=/home/jheintz/projects/glassbox/glassbox-smoke/oc4j/j2ee/home
 * oracle.j2ee.container.version=10.1.3.2.0
 * 
 * @author jheintz
 *
 */
public class OC4JGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
		return new File(System.getProperty("oracle.home"));
	}

	public File getDefaultScript() {
		return new File(getDefaultBinDirectory(), "oc4j"+getPlatformExtension());
	}

	public String getTargetSystemLabel() {
		return "OC4J";
	}

	public String getLaunchCommand() {
		return super.getLaunchCommand();
	}

	public boolean matchesContext(ServletContext context) {
		return System.getProperty("oracle.j2ee.container.version", "").startsWith("10.1.3") ||
		    checkClassloadersNameMatchPrefix("oracle.classloader.PolicyClassLoader") ||
		    checkClassloadersNameContains("ora.v"); // needs to be tested
	}

	protected Map getInstallProperties() {
		Map result = super.getInstallProperties();
		
		result.put("java.opts.var", "OC4J_JVM_ARGS");
		
		return result;
	}
	
	/**
	 * Override!
	 */
	public String getPlatformExtension() {
		if (isWindows())
			return ".cmd";
		else
			return "";
	}
}
