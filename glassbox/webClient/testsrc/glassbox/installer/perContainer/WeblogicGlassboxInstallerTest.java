package glassbox.installer.perContainer;

import junit.framework.TestCase;

public class WeblogicGlassboxInstallerTest extends TestCase {
	public void testURLPathMangling() throws Exception {
		String inpath = "file:/foo/bar/baz/";
		
		String outpath = inpath.substring("file:".length(), inpath.length());
		
		String expected = "/foo/bar/baz/";
		
		assertEquals(expected, outpath);
	}
}
