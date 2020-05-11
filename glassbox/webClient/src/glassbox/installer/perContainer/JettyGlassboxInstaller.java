package glassbox.installer.perContainer;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

public class JettyGlassboxInstaller extends BaseGlassboxInstaller {

    /**
	 * Override!
	 */
	public String getLaunchCommand() {
		return "java "+getJavaOptsSubst()+" -jar start.jar";
	}
    
	public File getDefaultBinDirectory() {
		return getContainerHome();
	}
	public File getContainerHome() {
		return new File(getJettyHomeProperty());
	}

	public File getDefaultScript() {
		return new File(getContainerHome(), "start.jar");
	}

	public boolean matchesContext(ServletContext context) {
		return getJettyHomeProperty() != null;
	}

	String getJettyHomeProperty() {
		return System.getProperty("jetty.home");
	}

	public String getTargetSystemLabel() {
		return "Jetty";
	}

}
