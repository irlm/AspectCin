package glassbox.util;

import junit.framework.TestCase;

public class DetectionUtilsTest extends TestCase {

    public void testVersionParsing() {
        assertEquals(6.0, DetectionUtils.getJavaRuntimeVersion("6.0.0_05 b"), 1e-9);
        assertEquals(6.0, DetectionUtils.getJavaRuntimeVersion("6.0.0"), 1e-9);
        assertEquals(6.1, DetectionUtils.getJavaRuntimeVersion("6.1"), 1e-9);
        assertEquals(1.5, DetectionUtils.getJavaRuntimeVersion("1.5.0_09"), 1e-9);
        assertEquals(1.4, DetectionUtils.getJavaRuntimeVersion("1.4.2_99"), 1e-9);
        assertEquals(1.4, DetectionUtils.getJavaRuntimeVersion("1.4"), 1e-9);
        assertEquals(1.3, DetectionUtils.getJavaRuntimeVersion("1.3.1_99"), 1e-9);
    }
}
