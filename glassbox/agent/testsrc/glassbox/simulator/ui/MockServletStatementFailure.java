/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 8, 2005
 */
package glassbox.simulator.ui;

import glassbox.simulator.resource.jdbc.MockConnection2;
import glassbox.simulator.resource.jdbc.MockJdbcDriver;
import glassbox.simulator.resource.jdbc.MockPreparedStatement;
import glassbox.test.DelayingRunnable;

import java.io.IOException;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 * @author Ron Bodkin
 */
public class MockServletStatementFailure extends MockDelayingServlet {
    MockJdbcDriver driver;
    {
        // preload mocks... which is slow
        driver = new MockJdbcDriver();
        try {
            driver.connect("jdbc://demo", null).prepareStatement("bazSF");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    DelayingRunnable runnable = new CpuHoggingRunnable(getDelay());

    // 3 connection failures, 2 sharing same sql, but different execution points, 1 with different ex pt and diff sql: should see 3 items in client
    // BUT line numbers may not be present, in which case, we see first and second invocations as the same, and aggregate stats
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        runnable.run();
        
        PreparedStatement ps = null;
    	Connection conn = null;
        try {
            conn = driver.connect("MockConnection2", null);
            ps = ((MockConnection2)conn).prepareStatement("select badness1 from statement_failure1 where obj=?");
            ps.setObject(1, "test");
            ((MockPreparedStatement)ps).setExceptionString("Simulated statement execution failure1");
            ps.execute();
        } catch (SQLException e) { }
        try {
            conn = driver.connect("MockConnection2", null);
            ps = ((MockConnection2)conn).prepareStatement("select badness1 from statement_failure1");
            ((MockPreparedStatement)ps).setExceptionString("Simulated statement execution failure1");
            ps.execute(); // this may be indistinguishable from previous call, if line numbers have been JITed out of existence in stack trace
        } catch (SQLException e) { }
		try {
            conn = driver.connect("MockConnection2", null);
            ps = ((MockConnection2)conn).prepareStatement("select badness2 from statement_failure2 where dbl=? and now=?");
            ps.setDouble(1, Math.random());
            ps.setDate(2, new Date(1999, 12, 31));
            ((MockPreparedStatement)ps).setExceptionString("Simulated statement execution failure2");
            ps.execute();
        } catch (SQLException e) {
        }
    }
    private static final long serialVersionUID = 2;
}