/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import glassbox.analysis.resource.jdbc.DatabaseEventFactory;
import glassbox.monitor.MonitorResponseTestCase;
import glassbox.response.Response;
import glassbox.test.ajmock.VirtualMockAspect;

import java.lang.reflect.Method;
import java.sql.*;

import javax.sql.DataSource;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.Stub;

//TODO: add a JDBC summarization unit test
public class JdbcMonitorTest extends MonitorResponseTestCase {
	private Mock mockConnection;
	private Connection conn;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockDriver;
    private Driver driver;
    public static final String DUMMY_JDBC_URL = "jdbc:://dummyDatabase";
    public void setUp() {
        super.setUp();
        mockConnection = mock(Connection.class);
	}

    protected Constraint startTime(final long time) {
        return new MethodConstraint("getStart", new Long(time));
    }

    protected Constraint endTime(final long time) {
        return new MethodConstraint("getEnd", new Long(time));
    }
    
    protected class MethodConstraint implements Constraint {
        private Constraint resultEq;
        private String methodName;
        private Object value;
        public MethodConstraint(final String methodName, final Object value) {
            resultEq = eq(value);
            this.methodName = methodName;
            this.value = value;
        }
        
        public boolean eval(Object o) {
            Method method = o.getClass().getMethod(methodName, null);
            Object result=method.invoke(o, null);
            //throw new AssertionFailedError("no method named " + name + " is defined in type "+mockedType);
            return resultEq.eval(result);
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append("result:");
            buffer.append(methodName);
            buffer.append("() = ");
            buffer.append(value);
            return buffer;
        }
    }
    
    public void testDirectConection() {
        establishMockConnection();
        
        assertSingleConnection(DUMMY_JDBC_URL);
    }
    
    protected void assertSingleConnection(String url) {
        assertEquals(2, listener.responses.size());
        assertConnection(url);
    }
    
    protected void assertConnection(String url) {
        Response end = getResponse(1);
        assertEquals(time[0], end.getStart());
        assertEquals(time[1], end.getEnd());
        assertProperties(null, url, Response.RESOURCE_DATABASE_CONNECTION, end);
    }
    
    public void testNestedDirectConection() {
        Mock mockDriverManager = virtualMock(DriverManagerConnection.aspectOf());
        final Driver driver = setupDriver();
        mockDriverManager.expects(once()).will(connectToDriver(DUMMY_JDBC_URL));
        
        DriverManager.getConnection(DUMMY_JDBC_URL);
        
        assertSingleConnection(DUMMY_JDBC_URL);
    }

    public Stub connectToDriver(final String url) {
        return new ExecuteStub() { public Object execute() throws Throwable { return driver.connect(url, null); } };        
    }
    
    public Stub delegateCreate(final Connection connection, final Statement statement) {
        return new ExecuteStub() { public Object execute() throws Throwable { connection.createStatement(); return statement; } };        
    }
    
    public Stub delegateExecute(final Statement statement, final String sql) {
        return new ExecuteStub() { public Object execute() throws Throwable { statement.execute(sql); return null; } };        
    }
    
    public void testNestedDirectFailure() {
        SQLException exc = new SQLException("Can't connect");
        
        Mock mockDriverManager = virtualMock(DriverManagerConnection.aspectOf());
        final Driver driver = setupDriver(throwException(exc));
        mockDriverManager.expects(once()).will(connectToDriver(DUMMY_JDBC_URL));
        
        try {
            DriverManager.getConnection(DUMMY_JDBC_URL);
            fail("swallowed exception");
        } catch (SQLException e) {
            assertEquals(exc, e);
        }

        assertSingleConnection(DUMMY_JDBC_URL);
        Response end = getResponse(1);
        assertFailingResponse(exc, end);
    }
    
    public interface UrlDataSource extends DataSource {
        String getUrl();
    }

    public void testNestedConection() {
        final String url = "jdbc://redmond:223/plants";

        establishMockNestedConnection(url);
        
        assertSingleConnection(url);
    }
    
    
    private Connection establishMockNestedConnection(final String url) {
        final Driver driver = setupDriver();
        Mock mockDatasource = mock(UrlDataSource.class);
        mockDatasource.stubs().method("getConnection").will(connectToDriver(url));
        mockDatasource.stubs().method("getUrl").will(returnValue(url));
        DataSource ds = (DataSource)mockDatasource.proxy();
        return ds.getConnection();
    } 
    
    public void testCreateStatement() {
        establishMockConnection();
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        conn.createStatement();
        
        assertConnection(DUMMY_JDBC_URL);
        assertEquals(4, listener.responses.size());
        Response end = getResponse(3);
        assertProperties(JdbcMonitor.DYNAMIC_SQL_DESCRIPTION, DUMMY_JDBC_URL, Response.RESOURCE_DATABASE_STATEMENT, end);
    }
    
    public void testFailingNestedCleanCreateStatement() {
        final String url = "jdbc://sweden:223/baz";
        String sql = "Can't prepare test";
        Connection connection = establishMockNestedConnection(url);
        Mock mockWrapped = mock(Connection.class);
        Connection wrappedConnection = (Connection)mockWrapped.proxy();
        Mock mockStatement = mock(Statement.class);
        Mock mockWrappedStmt = mock(Statement.class);
        Statement wrappedStmt = (Statement)mockWrappedStmt.proxy();
        
        Mock mockFactory = mock(DatabaseEventFactory.class);
        mockFactory.stubs().method("describeParams").will(returnValue("no parameters"));
        DatabaseEventFactory eventFactory = (DatabaseEventFactory)mockFactory.proxy();
        JdbcMonitor.aspectOf().setEventFactory(eventFactory);
        
        mockWrapped.expects(once()).method("createStatement").will(returnValue(wrappedStmt));
        mockConnection.expects(once()).method("createStatement").will(delegateCreate(wrappedConnection, (Statement)mockStatement.proxy()));
        
        String err = "Invalid SQL: "+sql;
        mockWrappedStmt.expects(once()).method("execute").will(throwException(new SQLException(err)));
        mockStatement.expects(once()).method("execute").with(eq(sql)).will(delegateExecute(wrappedStmt, sql));
        // worst case - unknown wrappers
        mockStatement.stubs().method("getConnection").will(returnValue(mock(Connection.class).proxy()));
        mockWrappedStmt.stubs().method("getConnection").will(returnValue(mock(Connection.class).proxy()));
        
        Statement s = connection.createStatement();
        assertSame(mockStatement.proxy(), s);
        
        try {
            s.execute(sql);
            fail("should throw exception");
        } catch (SQLException se) {
            assertEquals(err, se.getMessage());
        }
        
        assertEquals(6, listener.responses.size());
        Response end = getResponse(5);
//        assertProperties(sql, url, Response.RESOURCE_DATABASE_STATEMENT, end);
    }
    
//    
//    public void testSlowCreateStatement() {
//        establishMockConnection();
//        mockConnection.stubs().method("createStatement").will(
//                new DelayStub(TimingTestHelper.GUARANTEED_SLOW, statement) );
//        conn.createStatement();        
//        stats.summarizeOperation();
//        assertEquals(2, stats.getCount());
//        assertEquals(1, stats.getSlowCount());
//        assertEquals(0, stats.getSlowSingleOperationCount());
//        PerfStats connStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null); 
//        assertEquals(1, connStats.getCount());
//        assertEquals(0, connStats.getSlowCount());
//        assertEquals(0, connStats.getSlowSingleOperationCount());
//        PerfStats stmtStats = stats.getPerfStats(StatisticsType.DatabaseStatementIdx, JdbcMonitor.DYNAMIC_SQL_DESCRIPTION); 
//        assertEquals(1, stmtStats.getCount());
//        assertEquals(1, stmtStats.getSlowCount());
//        assertEquals(1, stmtStats.getSlowSingleOperationCount());
//        //TODO test transient summarization better
//        //assertFalse(uofwStats.getFailed());
//    }
//    
//    public void testDeathBy1000Cuts() {
//        establishMockConnection();
//        mockConnection.stubs().method("createStatement").will(
//                new DelayStub(TimingTestHelper.GUARANTEED_SLOW/4, statement) );
//        for (int i=0; i<4; i++) {
//            conn.createStatement();
//        }
//        stats.summarizeOperation();
//        assertEquals(5, stats.getCount());
//        assertEquals(1, stats.getSlowCount());
//        assertEquals(0, stats.getSlowSingleOperationCount());
//        PerfStats stmtStats = stats.getPerfStats(StatisticsType.DatabaseStatementIdx, JdbcMonitor.DYNAMIC_SQL_DESCRIPTION);
//        assertEquals(4, stmtStats.getCount());
//        assertEquals(1, stmtStats.getSlowCount());
//        assertEquals(0, stmtStats.getSlowSingleOperationCount());
//        //TODO test transient summarization better
//        //assertFalse(uofwStats.getFailed());
//    }
//    
//    public void testFailingCreateStatement() {
//        establishMockConnection();
//        mockConnection.stubs().method("createStatement").will(throwException(new SQLException("bad stmt")));
//        try {
//            conn.createStatement();
//            fail("shold have failed");
//        } catch (SQLException e) {
//            // simulates error handling
//        }
//        stats.summarizeOperation();
//        assertEquals(1, stats.getFailingOperationCount());
//        assertEquals(1, stats.getFailureCount());
//        PerfStats connStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null); 
//        assertEquals(0, connStats.getFailureCount());
//    }
//    
//    public void testFailingPrepareStatement() {
//        establishMockConnection();
//        mockConnection.stubs().method("prepareStatement").will(throwException(new SQLException("bad stmt")));
//        try {
//            conn.prepareStatement("select * from foo where bar = ?");
//            fail("shold have failed");
//        } catch (SQLException e) {
//            // simulates error handling
//        }
//        stats.summarizeOperation();
//        assertEquals(1, stats.getFailureCount());
//        PerfStats connStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null); 
//        assertEquals(0, connStats.getFailureCount());
//    }
//    
//    public void testNotHibernateExecuteStatement() {
//        String sql = "foo";
//        establishMockConnection();
//        Mock mockStatement = mock(DummyHibernateStatement.class);
//        mockStatement.expects(once()).method("executeHibernate").with(eq(sql)).will(VoidStub.INSTANCE);
//        mockStatement.stubs().method("getConnection").will(returnValue(conn));
//        DummyHibernateStatement dummyStmt = (DummyHibernateStatement)(mockStatement.proxy());
//        mockConnection.stubs().method("createStatement").will(returnValue(dummyStmt));
//        DummyHibernateStatement stmt = (DummyHibernateStatement)conn.createStatement();
//        stmt.executeHibernate(sql);
//        PerfStats stmtStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseStatement, sql); 
//        stats.summarizeOperation();
//        assertEquals(0, stmtStats.getCount());
//        assertEquals(dummyStmt, stmt);
//    }
//    
//	public void testExecuteStatement() {
//        establishMockConnection();
//        String sql = "select * from mumble where id < 250";
//		mockConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
//		mockStatement.expects(once()).method("execute").will(returnValue(true));
//		Statement statement = conn.createStatement();
//		statement.execute(sql);
//        stats.summarizeOperation();
//		assertEquals(3, stats.getCount());
//        PerfStats stmtStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseStatement, sql.substring(0, sql.indexOf("where"))); 
//        assertEquals(1, stmtStats.getCount());
//	}
//    
//    public void testConnectFailure() {
//        Mock mockDataSource = mock(DataSource.class);
//        DataSource ds = (DataSource)mockDataSource.proxy();
//        SQLException exc = new SQLException("can't connect");
//        mockDataSource.expects(once()).method("getConnection").will(throwException(exc));
//        try {
//            ds.getConnection();
//            fail("no SQL exception");
//        } catch (SQLException se) {
//            assertEquals(exc, se);
//        }
//        
//        stats = (PerfStats)AbstractMonitor.getRegistry().getPerfStats(StatisticsTypeImpl.Database, JdbcMonitor.dataSourceKey(ds));
//        stats.summarizeOperation();
//        PerfStats connStats = (PerfStats)stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null); 
//        assertEquals(1, connStats.getCount());        
//        assertEquals(1, connStats.getFailureCount());
//        Iterator it = connStats.getWorstFailures().iterator();
//        assertTrue(it.hasNext());
//        FailureStats failureInfo = (FailureStats)it.next();
//        ThreadState threadState = (ThreadState) failureInfo.getFailure().getThreadState();
//        TestHelper.assertArrayEquals(exc.getStackTrace(), threadState.getStackTrace());
//        assertEquals(1, failureInfo.getCount()); 
//        assertEquals(1, stats.getCount());
//        assertEquals(1, stats.getFailureCount());
//        assertFalse(it.hasNext());
//    }
//    
//    public void testMultithreaded() {
//        // TODO: make sure our various maps are properly guarded against deadlock / race conditions!
//    }
    
    public Mock establishMockConnection() {
        setupDriver();
        conn = (Connection)mockConnection.proxy();
        mockStatement = mock(Statement.class);
        statement = (Statement)mockStatement.proxy();
        mockStatement.stubs().method("getConnection").will(returnValue(conn));
        driver.connect(DUMMY_JDBC_URL, null);
        return mockConnection;
    }

    protected Driver setupDriver() {
        return setupDriver(returnValue(mockConnection.proxy()));
    }
    
    protected Driver setupDriver(Stub stub) {
        mockDriver = mock(Driver.class);
        driver = (Driver)mockDriver.proxy();
        mockDriver.expects(once()).method("connect").will(stub);
        return driver;
    }
}
interface DummyHibernateStatement extends Statement {
    void executeHibernate(String foo);
};

aspect DriverManagerConnection extends VirtualMockAspect {
    public pointcut mockPoint() : call(* DriverManager.getConnection(..)) && within(JdbcMonitorTest);
    
    public DriverManagerConnection() {
    }
//    before() : call(* Clock.getTime()) { System.err.println(thisJoinPointStaticPart.getSourceLocation()); }
}

