/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import glassbox.test.DelayingRunnable;

public class MockJdbcDriver implements Driver {
    protected DelayingRunnable delayer = new DelayingRunnable();
    protected SQLException sqlException = null;
    
    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }

    public void delay() {
        delayer.run();
    }

    public Connection connect(String arg0, Properties arg1) throws SQLException {
        if (sqlException != null) {
            throw sqlException;
        }
        delay();
        return (arg0.endsWith("2") ? new MockConnection2() :  new MockConnection());
    }

    public boolean acceptsURL(String arg0) throws SQLException {
        return false;
    }

    public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {
        return null;
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    public void setSqlException(SQLException sqlException) {
        this.sqlException = sqlException;        
    }

}
