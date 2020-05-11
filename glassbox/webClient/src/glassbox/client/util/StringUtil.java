/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.util;

public class StringUtil {

    /**
     * Equivalent to the Java 5 String.replace(CharSequence, CharSequence) method
     */
    public static String replace(String original, String oldVal, String newVal) {
        int lastPos = 0;
        for (;;) {
            lastPos = original.indexOf(oldVal, lastPos);
            if (lastPos==-1) {
                break;
            }            
            original = original.substring(0, lastPos)+newVal+original.substring(lastPos+oldVal.length());
            lastPos+=newVal.length();
        }
        return original;
    }

}
