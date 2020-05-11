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


/**
 * 
 * @author Ron Bodkin
 */
public class MockServletSlowDatabaseOverall extends MockDelayingServlet {
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
            driver.setDelay(getDelay() / 3);
            Connection conn1 = driver.connect("jdbc://demo1", null);
            PreparedStatement ps1 = conn1.prepareStatement("select * from big_wad_1");
            ps1.execute();
            Connection conn2 = driver.connect("jdbc://demo2", null);
            PreparedStatement ps2 = conn2.prepareStatement("select * from big_wad_2");
            ps2.execute();
            Connection conn3 = driver.connect("jdbc://demo3", null);
            PreparedStatement ps3 = conn3.prepareStatement("select * from big_wad_3");
            ps3.execute();
        } catch (SQLException e) {
            throw new ServletException("bad JDBC", e);
        }
    }
    private static final long serialVersionUID = 1L;
}
