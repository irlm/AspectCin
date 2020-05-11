/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;

public class MockServletSlowConnection extends MockDelayingServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	MockJdbcDriver driver = new MockJdbcDriver();
        driver.setDelay(getDelay());
        try {
            driver.connect("simulated_db", null);
        } catch (SQLException e) {
            throw new ServletException("bad JDBC", e);
        }
	}
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private static final long serialVersionUID = 1;
}
