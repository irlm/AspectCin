/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.jdbc;

import java.sql.*;
import java.util.*;

import org.jmock.core.CoreMock;
import org.jmock.core.DynamicMock;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationMocker;
import org.jmock.core.matcher.MethodNameMatcher;
import org.jmock.core.stub.ReturnStub;

public class MockConnection implements Connection {

    private Object stubProxy(Class clazz) {
        DynamicMock mock = new CoreMock(clazz, clazz.getName());
        mock.setDefaultStub(new ReturnStub(null));
        InvocationMocker invocationMocker = new InvocationMocker();
        invocationMocker.addMatcher(new MethodNameMatcher("execute"));
        invocationMocker.setStub(new ReturnStub(Boolean.TRUE));
        mock.addInvokable(invocationMocker);
        invocationMocker = new InvocationMocker();
        invocationMocker.addMatcher(new MethodNameMatcher("getConnection"));
        invocationMocker.setStub(new ReturnStub(this));
        mock.addInvokable(invocationMocker);
        Object o = mock.proxy();
        return o;
    }

    public StringBuffer describeTo(StringBuffer buffer) {

        return null;
    }

    public void verify() {

    }

    public boolean hasDescription() {

        return false;
    }

    public void invoked(Invocation invocation) {

    }

    public boolean matches(Invocation invocation) {

        return false;
    }

    public Statement createStatement() throws SQLException {
        return (Statement) stubProxy(Statement.class);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return (PreparedStatement) stubProxy(PreparedStatement.class);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return (CallableStatement) stubProxy(PreparedStatement.class);
    }

    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
    }

    public boolean getAutoCommit() throws SQLException {
        return false;
    }

    public void commit() throws SQLException {
    }

    public void rollback() throws SQLException {
    }

    public void close() throws SQLException {
    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    public boolean isReadOnly() throws SQLException {

        return false;
    }

    public void setCatalog(String catalog) throws SQLException {

    }

    public String getCatalog() throws SQLException {

        return null;
    }

    public void setTransactionIsolation(int level) throws SQLException {

    }

    public int getTransactionIsolation() throws SQLException {

        return 0;
    }

    public SQLWarning getWarnings() throws SQLException {

        return null;
    }

    public void clearWarnings() throws SQLException {

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {

        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {

        return null;
    }

    public Map getTypeMap() throws SQLException {

        return null;
    }

    public void setTypeMap(Map arg0) throws SQLException {

    }

    public void setHoldability(int holdability) throws SQLException {

    }

    public int getHoldability() throws SQLException {

        return 0;
    }

    public Savepoint setSavepoint() throws SQLException {

        return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {

        return null;
    }

    public void rollback(Savepoint savepoint) throws SQLException {

    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return (Statement) stubProxy(Statement.class);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return (PreparedStatement) stubProxy(PreparedStatement.class);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return (CallableStatement) stubProxy(CallableStatement.class);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return (PreparedStatement) stubProxy(PreparedStatement.class);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return (PreparedStatement) stubProxy(PreparedStatement.class);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return (PreparedStatement) stubProxy(PreparedStatement.class);
    }

    // implement Java 1.5 and Java 1.6 JDBC methods for forward compatibility...   
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {

        return null;
    }

    public Blob createBlob() throws SQLException {

        return null;
    }

    public Clob createClob() throws SQLException {

        return null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {

        return null;
    }

    public Properties getClientInfo() throws SQLException {

        return null;
    }

    public String getClientInfo(String name) throws SQLException {

        return null;
    }

    public boolean isValid(int timeout) throws SQLException {

        return false;
    }

    public void setClientInfo(Properties properties) {

    }

    public void setClientInfo(String name, String value) {

    }

    public boolean isWrapperFor(Class arg0) throws SQLException {

        return false;
    }

    public Object unwrap(Class arg0) throws SQLException {

        return null;
    }

    // these 4 types from JDBC 4 would require types in the JDBC 4 jar, and with it Java 5 bytecode format
//    public NClob createNClob() throws SQLException {
//        return null;
//    }
//
//    public BaseQuery createQueryObject(Class arg0, Connection arg1) throws SQLException {
//        return null;
//    }
//
//    public BaseQuery createQueryObject(Class arg0) throws SQLException {
//        return null;
//    }
//
//    public SQLXML createSQLXML() throws SQLException {
//        return null;
//    }

}
