/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Java5ConcurrentHashMap extends ConcurrentHashMap implements IConcurrentMap {

    public Java5ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    public Java5ConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public Java5ConcurrentHashMap() {
        super();
    }

    public Java5ConcurrentHashMap(Map arg0) {
        super(arg0);
    }

    private static final long serialVersionUID = 1L;
}
