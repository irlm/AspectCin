/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.ui;

import glassbox.simulator.resource.jdbc.MockJdbcDriver;
import glassbox.simulator.resource.jdbc.MockStatement;
import glassbox.test.DelayingRunnable;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Handy, dandy multifunction controller delegate, would be called by a Spring multi action controller...
 * This now requires load-time weaving: efficiency trumped ease of testing...
 * 
 * @author Ron Bodkin
 *
 */
public class MockMultiActionDelegate {
    protected DelayingRunnable delayer = new DelayingRunnable();
    protected MockJdbcDriver driver;
    
    public MockMultiActionDelegate() {
        // preload mocks... which is slow
        driver = new MockJdbcDriver();
        try {
            driver.connect("jdbc://demo", null).prepareStatement("baz");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public ModelAndView doDynamicQuery(HttpServletRequest request, HttpServletResponse response, Object command) throws Exception {
        Connection conn = driver.connect("jdbc://demo2", null);
        MockStatement stmt = (MockStatement)conn.createStatement();
        stmt.setDelay(delayer.getDelay());
        stmt.execute("select * from user where ssn='012-34-5678' and id=\"3\"");
        return null;
    }

    public ModelAndView doNestedFailure(HttpServletRequest request, HttpServletResponse response, Object command) throws Exception {
        throw new RuntimeException("runtimeExceptionFoo");
    }

    public ModelAndView doHandledFailure(HttpServletRequest request, HttpServletResponse response, Object command) throws Exception {
        try {
            callHelper();
            return null;
        } catch (Throwable t) {
            // gone
            return null;
        }
    }
    
    private void callHelper() {
        throw new RuntimeException("do you notice me?");
    }

    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }
    
    private static final long serialVersionUID = 1L;
    
}
