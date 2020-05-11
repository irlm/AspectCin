/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsTypeImpl implements Serializable, StatisticsType {
    // per the JLS 12.4.2, we can depend on the static fields being initialized in order...
    private static ArrayList/*<StatisticsTypeImpl>*/ allTypes = new ArrayList();
    private static List readOnlyAllTypes = null;

    /**
     * UiRequest maps into keys of type OperationDescriptionImpl 
     */
    public static final StatisticsType UiRequest = new UIStatisticsType();

    /**
     * Database maps into keys of type String (either a JDBC URL OR a string representation of DataSource: in future it would be better to map the latter into a JDBC config. string 
     */
    public static final StatisticsType Database = new DatabaseStatisticsType();

    /**
     * Database connection maps into a single key of value <code>null</code>, i.e., there
     * is always a single entry of this type.
     */
    public static final StatisticsType DatabaseConnection = new DatabaseConnectionStatisticsType();

    /**
     * Database statement maps into keys of type String (the SQL). In future, we might track key parameters as part of the key too... 
     */
    public static final StatisticsType DatabaseStatement = new DatabaseStatementStatisticsType();

    /**
     * Contention maps into keys of type ThreadState 
     */
    public static final StatisticsType Contention = new SimpleStatisticsType(ContentionIdx);

    /**
     * Remote call maps into keys of type String (typically endpoint URL) 
     */
    public static final StatisticsType RemoteCall = new RemoteCallStatisticsType ();
    
    /**
     * Contention maps into keys of type ThreadState 
     */
    public static final StatisticsType SlowMethod = new TreeStatisticsTypeImpl(SlowMethodIdx);

    public static StatisticsType getStatsType(int idx) {
        return (StatisticsType)allTypes.get(idx);
    }
    
    private int index;
    
    protected StatisticsTypeImpl(int index) {
        this.index = index;
        allTypes.add(this);
        readOnlyAllTypes = Collections.unmodifiableList(allTypes);
    }
    
    protected StatisticsTypeImpl() {}
    
    public int getIndex() { 
        return index; 
    }
    
    /** do <em>not</em> modify the returned list!! */
    public static List getAllTypes() {
        return readOnlyAllTypes;
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.StatisticsType#makePerfStats()
     */
    public PerfStats makePerfStats(Serializable key) {
        PerfStatsImpl stats = createPerfStats(key);
        stats.setType(this);
        stats.setKey(key);
        return stats;
    }

    protected PerfStatsImpl createPerfStats(Serializable key) {
        if (key instanceof OperationDescription) {
            return new OperationPerfStatsImpl();
        }
        return new CompositePerfStatsImpl();
    }
    
    public CallDescription getCall(PerfStats stats) {
        return null;
    }
    
    public String getLayer() {
        return "processing";
    }
    
    /* (non-Javadoc)
     * @see glassbox.track.api.StatisticsType#getParentStatsType()
     */
    public StatisticsType getParentStatsType() {
        return null;
    }

    // we have to support serializing these guys... the easier way is to define equals properly
    public boolean equals(Object other) {
        if (!(other instanceof StatisticsTypeImpl)) {
            return false;
        }
        StatisticsTypeImpl othStats = (StatisticsTypeImpl)other;
        return othStats.getIndex() == getIndex();
    }
    
    public int hashCode() {
        return index*3247234+2302897;
    }

    public String toString() {
        return "StatisticsTypeImpl "+index+" of "+getClass();
    }
    
    private static final long serialVersionUID = 1;
}

class UIStatisticsType extends UofwStatisticsType {
    public UIStatisticsType() { super(UiRequestIdx); };
    public CallDescription getCall(PerfStats stats) {
        return (OperationDescriptionImpl)stats.getKey();
    }
    public String getLayer() {
        return "ui";
    }
}

class DatabaseStatisticsType extends VirtualCompositeStatisticsType {
    public DatabaseStatisticsType() { super(DatabaseIdx); }
    public CallDescription getCall(PerfStats stats) {
        return new DefaultCallDescription(stats.getKey(), "database access", CallDescription.REMOTE_CALL);
    }
    public String getLayer() {
        return "resource";
    }
}

class DatabaseStatementStatisticsType extends VirtualCompositeStatisticsType {
    public DatabaseStatementStatisticsType() { super(DatabaseStatementIdx); }
    public CallDescription getCall(PerfStats stats) {
        if (stats.getParent() == null) {
            // this can happen, e.g., for internal database testing code that didn't explicitly get a connection
            // like Weblogic 9 and pointbase... just ignore it silently
            return null;
        }
        return new DefaultCallDescription(stats.getParent().getKey(), stats.getKey(), CallDescription.DATABASE_STATEMENT);
    }
    public String getLayer() {
        return "resource";
    }
    public StatisticsType getParentStatsType() {
        return Database;
    }
}

class DatabaseConnectionStatisticsType extends VirtualCompositeStatisticsType {
    public DatabaseConnectionStatisticsType() { super(DatabaseConnectionIdx); }
    public CallDescription getCall(PerfStats stats) {
        return new DefaultCallDescription(stats.getParent().getKey(), "database connection", CallDescription.DATABASE_CONNECTION);
    }
    public String getLayer() {
        return "resource";
    }
    public StatisticsType getParentStatsType() {
        return Database;
    }
}

class RemoteCallStatisticsType extends VirtualCompositeStatisticsType {
    public RemoteCallStatisticsType() { super(RemoteCallIdx); }
    public CallDescription getCall(PerfStats stats) {
        return new RemoteCallDescription(stats.getKey());
    }
    public String getLayer() {
        return "resource";
    }
}

class SimpleStatisticsType extends StatisticsTypeImpl {
    public SimpleStatisticsType(int index) { super(index); }

    protected PerfStatsImpl createPerfStats(Serializable key) {
        return new PerfStatsImpl();
    }
}
