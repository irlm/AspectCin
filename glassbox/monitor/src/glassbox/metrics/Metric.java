/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

public interface Metric {    
    static final String COUNT = "count";
    static final String TIME = "time";
    /** COUNT / TIME */
    static final String RATE = "rate";
    
    String getName();
    
    /**
     * 
     * @return Integer.TYPE, Long.Type, Double.Type, Float.Type, Class (for big num etc.)
     */ 
    Object getType();
    
    /**
     * 
     * @return COUNT, TIME, etc.
     */
    Object getDimensions();
}
