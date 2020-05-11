/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.ajmock;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class Thrower {
    static Unsafe theUnsafe = getUnsafe();
    
    protected static Unsafe getUnsafe ()
    {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe)field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException("can't get Unsafe instance", ex);
        }
    }

    public static void throwException(Throwable throwable) {
        theUnsafe.throwException(throwable);
    }
}
