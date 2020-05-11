package glassbox.config;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

public class GlassboxInitializerTest extends TestCase {
    public void testNullConfigDir() {
        Properties p = System.getProperties();
        p.remove(GlassboxInitializer.CONFIG_DIR_PROPERTY);
        System.setProperties(p);
        assertNull(GlassboxInitializer.getConfigDir());
    }

    public void testEmptyConfigDir() {
        testConfigDir("", "");
    }

    public void testSimpleConfigDir() {
        testConfigDir("test", "test");
    }

    public void testSimpleConfigDirEndSlash() {
        testConfigDir("test"+File.separator, "test");
    }
    public void testSimpleConfigDirEndFwd() {
        testConfigDir("test/", "test");
    }
    public void testSimpleConfigSeveral() {
        testConfigDir("c:\\boom\\baz\\a/"+File.separator, "c:\\boom\\baz\\a");
    }
    
    private void testConfigDir(String val, String expected) {        
        System.setProperty(GlassboxInitializer.CONFIG_DIR_PROPERTY, val);
        assertEquals(expected, GlassboxInitializer.getConfigDir());
    }

}
