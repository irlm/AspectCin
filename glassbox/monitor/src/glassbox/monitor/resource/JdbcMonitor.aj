/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.monitor.resource;

import glassbox.analysis.resource.jdbc.DatabaseEventFactory;
import glassbox.config.Bean;
import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitoredType;
import glassbox.response.Response;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import javax.sql.DataSource;

/**
 * Monitor jdbc calls.
 */
// Too big for a single type to handle... needs to be refactored into a set of cooperating abstractions in
// glassbox.monitor.resource.jdbc, e.g., connection & statement monitors
public aspect JdbcMonitor extends AbstractMonitor implements Bean {
    public final static String DYNAMIC_SQL_DESCRIPTION = "creating dynamic SQL statement";
    
    // if false, we won't hold data, e.g., if forbidden by security policy
    // not yet configurable... as of 24-feb-06
    private static final boolean trackingData = true;

     //ITD forms
    public static interface TrackedConnection extends MonitoredType /* don't add logging */ {
    }
    declare parents: Connection+ && !java.sql.* implements TrackedConnection;
    private transient Serializable TrackedConnection.key;
    
    public static interface TrackedStatement extends MonitoredType /* don't add logging */ {
    }    
    declare parents: Statement+ && !java.sql.* implements TrackedStatement;
    
    private transient Serializable TrackedStatement.key;
    private transient Connection TrackedStatement.connection;
    private transient ArrayList TrackedStatement.parameters; // only used for prepared statements
    private transient List TrackedStatement.batchLists;

    public static interface TrackedResultSet extends MonitoredType /* don't add logging */ {
    }
    declare parents: ResultSet+ && !java.sql.* implements TrackedResultSet;
    private transient List TrackedResultSet.parameters;
    
    //hokey use of prefixes
    private static final String PREPARE_PREFIX = "Prepare: ";
    private static final String NAVIGATE_PREFIX = "Fetch: ";
    
    private DatabaseEventFactory databaseEventFactory;
    
    private NameResolver nameResolver = new NullNameResolver();
    
    public pointcut dynamicSqlExec(Statement statement, String SQL) :
        within(Statement+) && execution(* Statement.execute*(..) throws SQLException) && this(statement) && args(SQL, ..);
            
	public pointcut topLevelDynamicSqlExec(Statement statement, String SQL) : 
        dynamicSqlExec(statement, SQL) && !cflowbelow(dynamicSqlExec(*, *));

    // batch SQL pointcuts
    
    public pointcut batchSqlExec(Statement statement) :
        within(Statement+) && execution(* Statement.batchSqlExec()) && this(statement) && 
        if(hasBatchListForStatement(statement) && aspectOf().getStatementConnection(statement)!=null);
    
    public pointcut topLevelBatchSqlExec(Statement statement) : 
        batchSqlExec(statement) && !cflowbelow(batchSqlExec(*));
    
    public pointcut preparedStatementExec(PreparedStatement statement) :
        within(PreparedStatement+) && execution(* PreparedStatement.execute*(..) throws SQLException) && 
        this(statement) && if(aspectOf().getStatementKey(statement)!=null);
    
    public pointcut topLevelPreparedStatementExec(PreparedStatement statement) : 
        preparedStatementExec(statement) && !cflowbelow(preparedStatementExec(*));
    
    public pointcut prepareStatement(String SQL, Connection connection) :
        within(Connection+) && execution(PreparedStatement+ Connection.*(String, ..)) && args(SQL, ..) && 
        this(connection);

    public pointcut topLevelPrepareStatement(String SQL, Connection connection) :
        prepareStatement(SQL, connection) && !cflowbelow(prepareStatement(*, *));

    public pointcut clearPreparedStatement() :
        within(PreparedStatement+) && (execution(* PreparedStatement.close()) || execution(* clearParameters()));

    public pointcut createStatementExec(Connection connection) :
        within(Connection+) && execution(Statement+ Connection.create*(..)) && this(connection);

    public pointcut topLevelCreateStatementExec(Connection connection) :
        createStatementExec(connection) && !cflowbelow(createStatementExec(*));
	
    public pointcut addBatchStatement(Statement statement, String SQL) :
        within(Statement+) && execution(* Statement+.addBatch(..)) && args(SQL) && this(statement) && if(SQL!=null);
    public pointcut topLevelAddBatchStatement(Statement statement, String SQL) :
        addBatchStatement(statement, SQL) && !cflowbelow(addBatchStatement(*, *)); 
    
    public pointcut clearBatchStatement(Statement statement) :
        within(Statement+) && execution(* Statement+.clearBatch()) && this(statement);
    public pointcut topLevelClearBatchStatement(Statement statement) :
        clearBatchStatement(statement) && !cflowbelow(clearBatchStatement(*));
    
    //we should also track data source information that's not obtained via JNDI  
    public pointcut dataSourceConnection(DataSource dataSource) : 
        within(DataSource+) && execution(* DataSource.getConnection(..)) && this(dataSource);
    public pointcut topLevelDataSourceConnection(DataSource dataSource) :
        dataSourceConnection(dataSource) && !beneathConnection();
    
    public pointcut directConnection(String url) :
        (within(Driver+) && execution(* Driver.connect(..))  || within(DriverManager+) && execution(* DriverManager.getConnection(..))) && 
        args(url, ..);
    
    public pointcut connection() : dataSourceConnection(*) || directConnection(*);
    public pointcut beneathConnection() : cflowbelow(connection());

    public pointcut topLevelDirectConnection(String url) :
        directConnection(url) && !beneathConnection();
    
    public pointcut resultSetNavigation(ResultSet resultSet) :
        within(ResultSet+) && this(resultSet) &&
        (execution(* ResultSet.next()) || execution(* ResultSet.previous()) || execution(* ResultSet.first()) || execution(* ResultSet.last()) || 
         execution(* ResultSet.afterLast()) || execution(* ResultSet.beforeFirst()) || execution(* ResultSet.refreshRow()) || 
         execution(* ResultSet.relative(*)) || execution(* ResultSet.absolute(*)) || execution(* ResultSet.moveTo*()));

    pointcut trackedResultSetNavigation(ResultSet resultSet) :
        resultSetNavigation(resultSet) && if(aspectOf().getStatementKey(resultSet.getStatement()) != null);
            
    public pointcut topLevelTrackedResultSetNavigation(ResultSet resultSet) :
        trackedResultSetNavigation(resultSet) && !cflowbelow(trackedResultSetNavigation(*)) && !beneathConnection();
    
    // this could probably be simplified for the various statement operations
    protected pointcut monitorEnd() : 
        (connection() && !beneathConnection()) || topLevelDynamicSqlExec(*, *) || topLevelBatchSqlExec(*) || 
        topLevelPreparedStatementExec(*) || topLevelPrepareStatement(*, *) || topLevelCreateStatementExec(*) || 
        topLevelTrackedResultSetNavigation(*);
    
    after(Statement statement, String SQL) returning : addBatchStatement(statement, SQL) {
        getBatchListForStatement(statement).add(SQL);
    }
    
    after(Statement statement) returning : clearBatchStatement(statement) {
        clearBatchListForStatement(statement);
    }
    
    // possible issue - what if a data source has the same string name as a valid JDBC URL?
    before(DataSource dataSource) : topLevelDataSourceConnection(dataSource) {
        begin(null, Response.RESOURCE_DATABASE_CONNECTION, dataSourceKey(dataSource));
    }
		
    before(String url) : topLevelDirectConnection(url) {
        begin(null, Response.RESOURCE_DATABASE_CONNECTION, url);
    }
    
    before(PreparedStatement statement) : topLevelPreparedStatementExec(statement) {
        Response response = statementExecResponse(statement, getStatementKey(statement));
        if(trackingData) {
            List parameters = getParameterList(statement);
            response.set(Response.PARAMETERS, parameters);
        }
        response.start();
    }
    
    before(Statement statement) : topLevelBatchSqlExec(statement) {
        String sqlStr = buildBatchSql(getBatchListForStatement(statement));
        if (sqlStr==null) {
            sqlStr = "untracked batch statement";
        }
        Response response = statementExecResponse(statement, sqlStr);
        if(trackingData) {
            response.set(Response.PARAMETERS, buildBatchParameterSql(getBatchListForStatement(statement)));
        }
        response.start();
    }
    
    before(Statement statement, String sql) : topLevelDynamicSqlExec(statement, sql) {
        if (sql==null) {
            sql = "NULL dynamic sql statement";
        }
        String key = filterDynamicSQL(sql);
        Response response = statementExecResponse(statement, key);
        if (trackingData) {      
            response.set(Response.PARAMETERS, databaseEventFactory.describeParams(sql, key));
        }
        response.start();
    }
    
    before(String SQL, Connection connection) : topLevelPrepareStatement(SQL, connection) {
        preparedStatementResponse(connection, SQL).start();
    }
    
    before(Connection connection) : topLevelCreateStatementExec(connection) {
        statementResponse(connection, DYNAMIC_SQL_DESCRIPTION).start();
    }
    
    // not ideal in that we count each navigate call separately, hence report on it as death by 1000 cuts when
    // ideally we'd count navigate for a single query
    before(ResultSet resultSet) : topLevelTrackedResultSetNavigation(resultSet) {
        Statement statement = resultSet.getStatement(); 
        Object statementKey = getStatementKey(statement);
        Response response = statementExecResponse(statement, NAVIGATE_PREFIX+statementKey);
        if(trackingData) {
            List parameters = getResultSetParams(resultSet);
            if (parameters != null) {
                response.set(Response.PARAMETERS, parameters);
            }
        }
        response.start();
    }
    
    after(DataSource dataSource) returning (Connection connection) : dataSourceConnection(dataSource) {
        Serializable dsKey = dataSourceKey(dataSource);
        putConnectionKey(connection, dsKey);
    }
    
    after(String url) returning (Connection connection) : directConnection(url) { 
        putConnectionKey(connection, url); // IGNORE the properties, if any...
    }

    after(Connection connection, String SQL) returning (PreparedStatement statement) : prepareStatement(SQL, connection) {
        putStatementKey(statement, SQL);
        putStatementConnection(statement, connection);
    }
    
    // clear AFTER processing: allows copying values on exceptional cases without always making a copy...
    after(PreparedStatement statement) returning (ResultSet resultSet): 
            preparedStatementExec(*) && this(statement) && if(trackingData) {
        putResultSetParameters(resultSet, getParameterList(statement));                
    }
    
    after(Connection connection) returning(Statement statement): createStatementExec(connection) {
        ((TrackedStatement)statement).connection = connection;
    }
        
    after(PreparedStatement preparedStatement, int pos, Object val) returning: 
        within(PreparedStatement+) && execution(* PreparedStatement.set*(..)) && args(pos, val, ..) && this(preparedStatement) && if(trackingData) {
        ArrayList params = getParams(preparedStatement);
        params.ensureCapacity(pos + 1);
        for (int i = params.size(); i < pos; i++) {
            params.add(null);
        }
        params.set(pos - 1, val);
    }
    
    
    after(PreparedStatement preparedStatement) : 
        within(PreparedStatement+) && execution(* PreparedStatement.addBatch()) && this(preparedStatement) && if(trackingData) {
        //TODO: push to a stack
        getParams(preparedStatement).clear();
    }
    
    private Response statementExecResponse(Statement statement, Serializable statementKey) {
        return statementResponse(getStatementConnection(statement), statementKey);
    }
    
    private Response preparedStatementResponse(Connection connection, String statementKey) {
        if (statementKey==null) { 
            statementKey="NULL statement";
        }
        return statementResponse(connection, PREPARE_PREFIX+statementKey);        
    }
    
    private Response statementResponse(Connection connection, Serializable statementKey) {        
        Serializable connectionKey = getConnectionKey(connection);

        Response response = createResponse(statementKey, Response.RESOURCE_DATABASE_STATEMENT);
        response.set(Response.RESOURCE_KEY, connectionKey);
        return response;
    }

    private Connection getWrappedConnection(Connection connection) {        
        String possibleNames[] = { "getConnection", "getDelegate" };
        return (Connection)findBestMethod(connection, possibleNames);
    }        

    private static final String[] DATA_SOURCE_OPTIONS = { "getDatabaseName", "getDatabasename", "getUrl", "getURL", "getDataSourceName", "getDescription" };
    private static final String[] C3P0_OPTIONS = { "getJdbcUrl" };
    /** Use common accessors to return meaningful name for the resource accessed by this data source. */
    public static String dataSourceTarget(DataSource ds) {
        // names are listed in descending preference order 
        String options[] = DATA_SOURCE_OPTIONS;
        if (ds.getClass().getName().startsWith("com.mchange.v2.c3p0.")) {
            options = C3P0_OPTIONS;
        }
        String name = (String)findBestMethod(ds, options);
        return (name != null) ? name : "unknown";
    }

    private static Object findBestMethod(Object receiver, String[] possibleNames) {
        Object result = null;
        for (int i=0; result == null && i<possibleNames.length; i++) {
            try {            
                Method method = receiver.getClass().getMethod(possibleNames[i], null);
                result = method.invoke(receiver, null);
            } catch (Exception e) {
                ; // keep trying
            }
        }
        return result;
    }    
    
    public Serializable dataSourceKey(DataSource ds) {
        String dsTarget = dataSourceTarget(ds);
        Object dsName = nameResolver.getName(ds);

        if (dsName == null) {
            return dsTarget;
        }
        return dsName.toString()+" with dataSource "+dsTarget;
    }

    private static String buildBatchSql(List statements) {
        StringBuffer res = new StringBuffer();
        for(int i = 0; i < statements.size(); i++) {
            res.append(filterDynamicSQL((String)statements.get(i)));
            res.append("\n");
        }
        return res.toString();
    }
        
    private static String buildBatchParameterSql(List statements) {
        StringBuffer res = new StringBuffer();
        for(int i = 0; i < statements.size(); i++) {
            res.append((String)statements.get(i));
            res.append("\n");
        }
        return res.toString();
    }
    
    /**
     * to group sensibly and to avoid recording sensitive data, I don't record the where clause (only used for dynamic SQL since parameters aren't included in prepared statements)
     * @return subset of passed SQL up to the where clause
     */
    public static String filterDynamicSQL(String sql) {
        if (sql==null) {
            return "(null) sql statement";
        }
        
        String dynSQL = sql.toLowerCase();
        int pos = dynSQL.indexOf("where");
        int setPos = dynSQL.indexOf("set");
        pos = minPos(pos, setPos);
        int valuePos = dynSQL.indexOf("values");
        pos = minPos(pos, valuePos);
        if (pos > -1) {
            sql = sql.substring(0, pos);
        }
        return sql.trim();
    }
    
    private static int minPos(int pos1, int pos2) {
        if (pos1 == -1) {
            return pos2;
        } else if (pos2 == -1) {
            return pos1;
        } else {
            return Math.min(pos1, pos2);
        }
    }

    private void putConnectionKey(Connection connection, Serializable key) {
        //connections.put(connection, key);
        ((TrackedConnection)connection).key = key;
    }
    
    private Serializable getConnectionKey(Connection connection) {
        Serializable key;
        Connection orig=connection;
        for (int i=0; connection instanceof TrackedConnection && i<5; i++) {
//            /key = (Serializable)connections.get(connection);
            key = ((TrackedConnection)connection).key;
            if (key != null) {
                if (i>0) {
                    //connections.put(orig, key);
                    ((TrackedConnection)orig).key = key;
                }
                return key;
            }
            // can happen with wrappers... it's probably better to track execution of the child instead
            connection = getWrappedConnection(connection);
        }
        return "jbdc://pooled-driver";
    }
    
    private void putStatementConnection(Statement statement, Connection connection) {
//            statements.put(statement, connection);
        ((TrackedStatement)statement).connection = connection;
    }
    
    private void putStatementKey(Statement statement, Serializable key) {
//        statements.put(statement, key);
        ((TrackedStatement)statement).key = key;
    }
    
    private void putResultSetParameters(ResultSet resultSet, List parameters) {
//        resultSetParameters.put(resultSet, parameters);
        ((TrackedResultSet)resultSet).parameters = parameters;
    }
    
    private Connection getStatementConnection(Statement statement) {
//        Connection c = (Connection)statementConnections.get(statement);
        Connection c = ((TrackedStatement)statement).connection;
        if (c==null) {
            // sometimes statements from wrapper drivers don't point to the right wrapper, e.g., Tomcat Data Sources 
            return statement.getConnection(); //fallback
        }
        return c;
    }
    
    private Serializable getStatementKey(Statement statement) {
//        return (Serializable)statements.get(statement);
        Serializable key = ((TrackedStatement)statement).key;
        if (key == null) {
            return "(untracked prepared statement)";
        }
        return key;
    }
    
    private List getResultSetParameters(ResultSet resultSet) {
//        return resultSetParameters.get(resultSet);
        return ((TrackedResultSet)resultSet).parameters;
    }
    
    private static ArrayList getParams(PreparedStatement mps) {
        ArrayList result;
//        synchronized(preparedStatementParameters) {
//            result = (ArrayList)preparedStatementParameters.get(mps);
            result = ((TrackedStatement)mps).parameters;
            if (result == null) {
                result = new ArrayList();            
//                preparedStatementParameters.put(mps, result);
                ((TrackedStatement)mps).parameters=result;
            }
//        }
        return result;
    }
    
    public static List getParameterList(PreparedStatement mps) {        
        return Collections.unmodifiableList(getParams(mps));        
    }
    
    private void clearBatchListForStatement(Statement statement) {
        ((TrackedStatement)statement).batchLists = null;
    }
    
    private static List getBatchListForStatement(Statement statement) {
            List list = ((TrackedStatement)statement).batchLists;
            if (list == null) {
                list = new LinkedList();
                ((TrackedStatement)statement).batchLists = list;
            }
            return list;
    }
    
    private static boolean hasBatchListForStatement(Statement statement) {
        return getBatchListForStatement(statement) != null;
    }
    
    static void setResultSetParams(ResultSet resultSet, List parameters) {
        ((TrackedResultSet)resultSet).parameters = parameters;
    }

    static List getResultSetParams(ResultSet resultSet) {
        return ((TrackedResultSet)resultSet).parameters;
    }
    
    public void setEventFactory(DatabaseEventFactory databaseEventFactory ) {
        this.databaseEventFactory = databaseEventFactory; 
    }

    public DatabaseEventFactory getEventFactory() {
        return databaseEventFactory;
    }
    
    public NameResolver getNameResolver() {
        return nameResolver;
    }

    public void setNameResolver(NameResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

    public String getLayer() {
        return Response.RESOURCE_DATABASE;
    }
}
