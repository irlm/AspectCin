/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util;

public class DetectionUtils {
    
    public static String getFullJavaVersion() {
        String runtime = System.getProperty("java.version");
        if (runtime == null) {
            return System.getProperty("java.runtime.version");
        }
        return runtime;
    }
    
    public static boolean isJava14() {
    	return getJavaRuntimeVersion() < 1.5;
    }

    public static boolean isJava5() {
    	return !isJava14();
    }

    
    public static double getJavaRuntimeVersion() {
        String runtime = getFullJavaVersion();
        return getJavaRuntimeVersion(runtime);
    }
    
    public static double getJavaRuntimeVersion(String runtime) {
        int majorVersionPos = runtime.indexOf('.');
        int minorVersionPos = runtime.indexOf('.', majorVersionPos+1);
        if (minorVersionPos == -1) {
            minorVersionPos = runtime.length();
        }
        return Double.parseDouble(runtime.substring(0, minorVersionPos));
    }

}
