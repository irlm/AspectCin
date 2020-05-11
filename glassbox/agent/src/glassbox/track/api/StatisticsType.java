/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.io.Serializable;

public interface StatisticsType {

    /**
     * UiRequest maps into keys of type OperationDescriptionImpl 
     */
    public static final int UiRequestIdx = 0;

    /**
     * Database maps into keys of type String (either a JDBC URL OR a string representation of DataSource: in future it would be better to map the latter into a JDBC config. string 
     */
    public static final int DatabaseIdx = 1;

    /**
     * Database connection maps into a single key of value <code>null</code>, i.e., there
     * is always a single entry of this type.
     */
    public static final int DatabaseConnectionIdx = 2;

    /**
     * Database statement maps into keys of type String (the SQL). In future, we might track key parameters as part of the key too... 
     */
    public static final int DatabaseStatementIdx = 3;

    /**
     * Contention maps into keys of type ThreadState 
     */
    public static final int ContentionIdx = 4;

    /**
     * Remote call maps into keys of type String (typically endpoint URL) 
     */
    public static final int RemoteCallIdx = 5;

    /**
     * Slow method maps into keys of a constant value: SlowMethodKey 
     */
    public static final int SlowMethodIdx = 6;
    
    public static final String SlowMethodKey = "time";

    PerfStats makePerfStats(Serializable key);
    
    int getIndex(); 
    
    CallDescription getCall(PerfStats stats);
    
    String getLayer();
    
    /** @returns the statistics type for the containing resource (e.g., database or remote service), or null if none */ 
    StatisticsType getParentStatsType();
}