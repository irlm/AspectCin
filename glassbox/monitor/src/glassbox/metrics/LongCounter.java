/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

public class LongCounter extends AbstractMetric implements LongMetric {

    private long value;

    public LongCounter(String name) {
        super(name);
    }

    public long getValue() {
        return value;
    }

    public synchronized long increment() {
        return ++value;
    }

    public synchronized long incrementBy(long delta) {
        return (value += delta);
    }

    public Object getType() {
        return Long.TYPE;
    }

    public Object getDimensions() {
        return "count";
    }
}
