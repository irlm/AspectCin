package glassbox.installer.util;

import java.io.File;

import junit.framework.TestCase;

public class WebappResourceLocatorTest extends TestCase {

	public void testTempDir() throws Exception {
		File tempDir = WebappResourceLocator.makeTempDir();
		assertTrue(tempDir.exists());
		assertTrue(tempDir.isDirectory());
	}
}
