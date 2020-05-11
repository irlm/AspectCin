package glassbox.installer.ant;

import java.io.File;
import java.util.Map;

public interface Installer {
	public void init(File installResourceDir, String targetSystem, Map properties);
	
	public void install();
}
