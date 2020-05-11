/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.util;

import junit.framework.Assert;

public class TestHelper {

    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        Assert.assertEquals(expected.length, actual.length);
        for (int i=0; i<expected.length; i++) {
            Assert.assertEquals("elements "+i+" differ ", expected[i], actual[i]);            
        }
    }

}
