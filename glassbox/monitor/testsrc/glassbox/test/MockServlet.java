/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 8, 2005
 */
package glassbox.test;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * @author Ron Bodkin
 */
public class MockServlet extends HttpServlet {
    
    protected transient Runnable runnable = null;
    
    public transient MockHttpServletRequest REQUEST = makeRequest();
    public transient HttpServletResponse RESPONSE = new MockHttpServletResponse();
    
    public MockServlet() {}
    
    public MockServlet(Runnable runnable) {
        setRunnable(runnable);
    }
    
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public Runnable getRunnable() {
        return runnable;
    }
    
    public void forceDoGet() throws ServletException, IOException {
        REQUEST.setMethod("GET");
        service(REQUEST, RESPONSE);
    }
    
    public void forceDoPost() throws ServletException, IOException {
        REQUEST.setMethod("POST");
        service(REQUEST, RESPONSE);
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        run();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public String getServletName() {
		return getClass().getName();
	}

    public void run() {
        if (runnable != null) {
            runnable.run();
        }
    }

    protected MockHttpServletRequest makeRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Random rand = new Random();
        String uri = "/Simul/"+getClass().getName()+rand.nextInt(20);
        if (rand.nextInt(3)>1) {
            String value=""+rand.nextInt(15);
            request.addParameter("arg", value);
        }
        request.setRequestURI(uri);
        return request;
    }
    private static final long serialVersionUID = 1;
}

