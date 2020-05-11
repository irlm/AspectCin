package glassbox.bootstrap.log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import junit.framework.TestCase;

public class InitializeLogTest extends TestCase {

    public void testGetJarLoc() throws MalformedURLException, UnsupportedEncodingException {
        testJarLocs("/test/this/file.zip");
        testJarLocs("/simple.jar");
        testJarLocs("//wi\ndows/unc/share/a.zip");
        if (File.separatorChar=='\\') {
            // these tests are only valid for Windows
            testJarLocs("c:\\base.jar");
            testJarLocs("D:\\program%20files\\base.jar");
        }
    }
    
    public void testGetJarLocErrs() throws MalformedURLException, UnsupportedEncodingException {
        assertNull(InitializeLog.getJarLoc(new URL("file:/c:/base.jar!")));
        assertNull(InitializeLog.getJarLoc(new URL("file:/c:/base.jar")));
        assertNull(InitializeLog.getJarLoc(new URL("file:/foo.txt")));
    }
    
    private void testJarLocs(String root) throws MalformedURLException, UnsupportedEncodingException {
        String expected = URLDecoder.decode(root, "UTF-8");
        String actual = InitializeLog.getJarLoc(new URL("jar:file:"+root+"!/test.txt"));
        assertEquals(expected, actual);
        //this fails on a Sun VM, but it looks like JRockIt 1.4 accepts it...
        //assertEquals(URLDecoder.decode(root, "UTF-8"), InitializeLog.getJarLoc(new URL("zip:"+root+"!/test.txt")));
    }
    
    public void testBadJarLoc() {
        try {
            String res = InitializeLog.getJarLoc(new URL("jar:file!/test.txt"));
            fail("Should have thrown exception, but returned "+res);
        } catch (Exception e) {
            ; // success
        }
    }
}
