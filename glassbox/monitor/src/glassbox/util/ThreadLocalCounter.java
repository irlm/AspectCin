/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util;

public class ThreadLocalCounter extends ThreadLocal {
    public Object initialValue() { return new int[] { 0 }; }
    public int increment() {
        int[] held = (int[])get();
        return ++held[0];
    }
    public int decrement() {
        int[] held = (int[])get();
        return --held[0];
    }
    public int getValue() {
        int[] held = (int[])get();
        return held[0];
    }
};

