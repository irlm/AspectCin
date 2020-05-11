/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.jdbc;

import glassbox.test.DelayingRunnable;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class MockPreparedStatement extends MockStatement implements PreparedStatement {
    public void addBatch() throws SQLException {
    }

    public ResultSet executeQuery() throws SQLException {
        return null;
    }

    public int executeUpdate() throws SQLException {
        return 0;
    }

    public MockPreparedStatement(String sql, Connection conn) {
        super(conn);
    }

    public void clearParameters() throws SQLException {

    }

    public boolean execute() throws SQLException {

        if (ex != null)
            throw new SQLException(ex, mockSqlState, mockSqlErrorCode);
        delayer.run();
        return false;
    }

    public ResultSetMetaData getMetaData() throws SQLException {

        return null;
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {

        return null;
    }

    public void setArray(int i, Array x) throws SQLException {

    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    public void setBlob(int i, Blob x) throws SQLException {

    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {

    }

    public void setByte(int parameterIndex, byte x) throws SQLException {

    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {

    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

    }

    public void setClob(int i, Clob x) throws SQLException {

    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

    }

    public void setDate(int parameterIndex, Date x) throws SQLException {

    }

    public void setDouble(int parameterIndex, double x) throws SQLException {

    }

    public void setFloat(int parameterIndex, float x) throws SQLException {

    }

    public void setInt(int parameterIndex, int x) throws SQLException {

    }

    public void setLong(int parameterIndex, long x) throws SQLException {

    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {

    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {

    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

    }

    public void setObject(int parameterIndex, Object x) throws SQLException {

    }

    public void setRef(int i, Ref x) throws SQLException {

    }

    public void setShort(int parameterIndex, short x) throws SQLException {

    }

    public void setString(int parameterIndex, String x) throws SQLException {

    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

    }

    public void setTime(int parameterIndex, Time x) throws SQLException {

    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    public void setURL(int parameterIndex, URL x) throws SQLException {

    }

    // implement Java 1.5 and Java 1.6 JDBC methods for forward compatibility...
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    public void setNString(int parameterIndex, String value) throws SQLException {

    }

    // these 3 methods from JDBC 4 would require types in the JDBC 4 jar, and with it Java 5 bytecode format
//    public void setNClob(int parameterIndex, NClob value) throws SQLException {
//    }
//
//    public void setRowId(int parameterIndex, RowId x) throws SQLException {
//    }
//
//    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
//    }

}
