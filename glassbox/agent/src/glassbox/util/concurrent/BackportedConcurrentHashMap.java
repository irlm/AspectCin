/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.concurrent;

import java.util.Map;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

public class BackportedConcurrentHashMap extends ConcurrentHashMap implements IConcurrentMap {

    public BackportedConcurrentHashMap() {
    }

    public BackportedConcurrentHashMap(int arg0, float arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    public BackportedConcurrentHashMap(int arg0, float arg1) {
        super(arg0, arg1);
    }

    public BackportedConcurrentHashMap(int arg0) {
        super(arg0);
    }

    public BackportedConcurrentHashMap(Map arg0) {
        super(arg0);
    }
    
}
