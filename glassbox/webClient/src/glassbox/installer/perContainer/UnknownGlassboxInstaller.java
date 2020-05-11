package glassbox.installer.perContainer;

import java.io.File;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

public class UnknownGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
		//TODO Can we even try to get this?
		return null;
	}

	public File getDefaultScript() {
		return null;
	}

	public boolean matchesContext(ServletContext context) {
		return false;
	}

	public String getTargetSystemLabel() {
		return "Unknown";
	}
	
}
