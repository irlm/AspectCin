/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;
import glassbox.simulator.resource.jdbc.MockPreparedStatement;
import glassbox.simulator.resource.jdbc.MockStatement;
import glassbox.test.DelayingRunnable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

/**
 * Handy, dandy multifunction action.
 * 
 * @author Ron Bodkin
 *
 */
public class MockDispatchAction extends DispatchAction {
    protected DelayingRunnable delayer = new DelayingRunnable();
    protected MockJdbcDriver driver;
    
    public MockDispatchAction() {
        // preload mocks... which is slow
        driver = new MockJdbcDriver();
        try {
            driver.connect("jdbc://demo", null).prepareStatement("baz");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public ActionForward doBatch(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
            HttpServletResponse response) throws IOException, ServletException {
        try {
            Connection conn = driver.connect("jdbc://demo2", null);
            MockStatement stmt = (MockStatement)conn.createStatement();
            stmt.clearBatch();
            stmt.addBatch("insert (id, ssn) values (1, '012-34-5678') into user");
            stmt.addBatch("update user set name='Wallace' where ssn='012-34-5678'");
            stmt.setDelay(delayer.getDelay());
            stmt.executeBatch();
            return null;
        } catch (SQLException e) {
            throw new ServletException("bad JDBC", e);
        }
    }
    
    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }
    
    private static final long serialVersionUID = 1L;
    
}
