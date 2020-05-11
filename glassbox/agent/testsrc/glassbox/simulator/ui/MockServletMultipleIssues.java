/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 8, 2005
 */
package glassbox.simulator.ui;

import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import glassbox.simulator.MultithreadedRunner;
import glassbox.simulator.resource.jdbc.MockJdbcDriver;


/**
 * 
 * @author Doug Barnum
 */
public class MockServletMultipleIssues extends MockDelayingServlet {
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

    Random random = new Random();
	MockServletThreadContention[] lockers = new MockServletThreadContention[3];
	Thread[] threads;
    MultithreadedRunner runner = new MultithreadedRunner();
	double maxdel;
	
	public MockServletMultipleIssues(double maxdel) {
		this.maxdel = maxdel;
        
        threads = new Thread[lockers.length];
        for (int i=0; i<lockers.length; i++) {            
            lockers[i] = new MockServletThreadContention(0L);
        }
	}
	
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {

        try {
            driver.setDelay(getDelay());
            Connection conn = driver.connect("jdbc://demo", null);
            PreparedStatement ps = conn.prepareStatement("select * from big_wad");
            ps.execute();
        } catch (SQLException e) {
            throw new ServletException("bad JDBC", e);
        }
    	
    	
    	for (int i=0; i<lockers.length; i++) {
        	final MockServletThreadContention locker = lockers[i];
        	locker.setDelay(genDelay(maxdel));
        	threads[i] = new Thread(new Runnable() {
        		public void run() {
        			try {
        				locker.forceDoGet();
        			} catch (Throwable t) {};
        		}
        	});
        }
        try {
        	runner.run(threads);
        } catch (InterruptedException ie) {};
    }
    private static final long serialVersionUID = 1L;
    
    private long genDelay(double maxDelay) {
        return (long)((0.9 + 0.2 * random.nextDouble())*maxDelay * TimeConversion.NANOSECONDS_PER_SECOND);
    }
    
   
}