package glassbox.installer.perContainer;

import java.io.File;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

/**
 * 
 * Important? system properties:
 * jboss.server.lib.url=file:/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/default/lib/
 * jboss.server.base.url=file:/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/
 * jboss.server.base.dir=/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server
 * catalina.home=/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/default 
 * jboss.server.temp.dir=/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/default/tmp
 * jboss.lib.url=file:/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/lib/
 * jboss.server.home.url=file:/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/default/
 * jboss.server.home.dir=/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/server/default
 * jboss.home.url=file:/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA/
 * jboss.home.dir=/home/jheintz/projects/glassbox/glassbox-smoke/jboss-4.0.5.GA
 * 
 * 
 */
public class JBossGlassboxInstaller extends BaseGlassboxInstaller {

	public File getContainerHome() {
        return new File(getJBossServerHomeProperty());
	}

	public File getDefaultBinDirectory() {
		return new File(getJBossHomeDirectory(), "bin");
	}

	private File getJBossHomeDirectory() {
		return new File(System.getProperty("jboss.home.dir"));
	}
	
	public File getDefaultLibDirectory() {
		return new File(getJBossHomeDirectory(), "lib");
	}

	public String getTargetSystemLabel() {
		return "JBoss";
	}

	public String getDefaultScriptPrefix() {
		return "run";
	}
	
	public boolean matchesContext(ServletContext context) {
        return (getJBossServerHomeProperty() != null) || checkClassloadersNameMatchPrefix("org.jboss.");
	}

	String getJBossServerHomeProperty() {
		return System.getProperty("jboss.server.home.dir");
	}

}
