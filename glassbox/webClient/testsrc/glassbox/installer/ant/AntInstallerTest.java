package glassbox.installer.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class AntInstallerTest extends TestCase {
	
	File installResourceDir;
	File tempInstallDir;
	File installBinDir;
	File installLibDir;
	File installGlassboxDir;
	String targetSystem = "default";

	protected void setUp() throws Exception {
		super.setUp();
		
		installResourceDir = findResourceDir();
		tempInstallDir = buildTempDir();
	}

	File buildTempDir() throws IOException {
		File temp = File.createTempFile("glassbox", ".tmp");
		temp.delete();
		temp.mkdir();
		temp.deleteOnExit();
		
		return temp;
	}

	File findResourceDir() {
		
		// TODO JDH: Must use better way to get install directory!!! This is fragile.
		return new File(new File("web"), "install");
//		URL url = this.getClass().getClassLoader().getResource("install/default/build.xml");	
//		return new File(url.getFile()).getParentFile().getParentFile();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public Map buildBasicProperties() {
		Map result = new HashMap();
		
		installBinDir = new File(tempInstallDir, "bin");
		installLibDir = new File(tempInstallDir, "lib");
		installGlassboxDir = new File(installLibDir, "glassbox");

		result.put("bin.dir", installBinDir.getAbsolutePath());
		result.put("lib.dir", installLibDir.getAbsolutePath());
		result.put("glassbox.home", installGlassboxDir.getAbsolutePath());
		result.put("script.prefix", "default");

		
		result.put("glassbox.version", "2.0.0");
		
		return result;
	}

	/**
	 * TODO fix classpath issues so this runs inside CruiseControl
	 */
	public void testLibraryInstall() {
		AntInstaller installer = new AntInstaller(this.installResourceDir, targetSystem, buildBasicProperties());
		installer.install();
	}

	public void testScriptInstall() {
		Map properties = buildBasicProperties();
		
		// add script.file to trigger generation of install
		properties.put("launch.command", "foo");
		
		AntInstaller installer = new AntInstaller(this.installResourceDir, targetSystem, properties);
		installer.install();
		
		File script1 = new File(installBinDir, "default_with_glassbox.sh");
		File script2 = new File(installBinDir, "default_with_glassbox.bat");
				
		assertTrue(script1.isFile() && script2.isFile());
	}

	public void testScriptPrefixInstall() {
		Map properties = buildBasicProperties();
		
		// add script.file to trigger generation of install
		properties.put("launch.command", "foo");
		properties.put("script.prefix", "");
		
		AntInstaller installer = new AntInstaller(this.installResourceDir, targetSystem, properties);
		installer.install();
		
		File script1 = new File(installBinDir, "_with_glassbox.sh");
		File script2 = new File(installBinDir, "_with_glassbox.bat");
				
		assertTrue(script1.isFile() && script2.isFile());
	}

	public void testScriptInstallWithAltTargetSystem() {
		Map properties = buildBasicProperties();
		
		// add script.file to trigger generation of install
		properties.put("launch.command", "foobarr");
		properties.put("script.prefix", "fred");
		
		AntInstaller installer = new AntInstaller(this.installResourceDir, "fred", properties);
		installer.install();
		
		File script1 = new File(installBinDir, "fred_with_glassbox.sh");
		File script2 = new File(installBinDir, "fred_with_glassbox.bat");
				
		assertTrue(script1.isFile() && script2.isFile());
	}
}
