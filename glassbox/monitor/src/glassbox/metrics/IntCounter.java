/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

public class IntCounter extends AbstractMetric implements IntMetric {

    private int value;
    
    public IntCounter(String name) {
        super(name);
    }

    public int getValue() {
        return value;
    }

    public int increment() {
        return ++value;
    }

    public int incrementBy(int delta) {
        return (value += delta);
    }

    public Object getType() {
        return Integer.TYPE;
    }
    
    public Object getDimensions() {
        return "count";
    }

}
