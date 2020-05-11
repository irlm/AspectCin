/*
 * Copyright (c) 2005 Glassbox Corporation. All Rights Reserved. See license terms for limitations and restrictions on
 * use.
 * 
 * Created on Apr 8, 2005
 */
package glassbox.simulator.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;

/**
 * 
 * @author Ron Bodkin
 */
public class MockServletConnectionFailure extends MockDelayingServlet {

    private boolean fail = false;

    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {


        MockJdbcDriver driver = new MockJdbcDriver();

        fail = !fail;

        if (fail) {
            driver.setSqlException(new SQLException("failed connection", "08001"));

            try {
                driver.connect("failing_URL01", null); // different url and exec point
            } catch (SQLException e) {
            }
            try {
                driver.connect("failing_URL02", null); // add two connection failures from different points to generate
                                                        // two problems in detail pane of client
            } catch (SQLException e) {
            }
            try {
                driver.connect("failing_URL02", null);
            } catch (SQLException e) {
            }
        } else {
            driver.setSqlException(null); // this should make calls succeed
            try {
                driver.connect("succeeding_URL", null); // different url and exec point
            } catch (SQLException e) {
            }
        }
    }

    private static final long serialVersionUID = 1L;
}