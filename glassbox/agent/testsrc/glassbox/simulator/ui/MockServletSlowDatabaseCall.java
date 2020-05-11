/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 8, 2005
 */
package glassbox.simulator.ui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;
import glassbox.simulator.resource.jdbc.MockPreparedStatement;


/**
 * 
 * @author Ron Bodkin
 */
public class MockServletSlowDatabaseCall extends MockDelayingServlet {
    MockJdbcDriver driver;
    {
        // preload mocks... which is slow
        driver = new MockJdbcDriver();
        try {
            driver.connect("jdbc://demo", null).prepareStatement("baz");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        try {
            Connection conn = driver.connect("jdbc://demo2", null);
            MockPreparedStatement ps = (MockPreparedStatement)conn.prepareStatement("select * from accounts where id=? and region=?");
            ps.setDelay(getDelay());
            ps.setInt(1, (int)(Math.random()*(double)0xffffff));
            ps.setString(2, Math.random()>.3 ? "CA" : "TX");
            ps.execute();
            
            ps = (MockPreparedStatement)conn.prepareStatement("update accounts set val=? where id=? and region=?");
            ps.setDelay(getDelay());
            ps.setDouble(1, Math.random()*10000.);
            ps.setInt(2, (int)(Math.random()*(double)0xffffff));
            ps.setString(3, Math.random()>.3 ? "CA" : "TX");
            ps.execute();
        } catch (SQLException e) {
            throw new ServletException("bad JDBC", e);
        }
    }
    private static final long serialVersionUID = 1L;
}