/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package test;

import java.io.IOException;

import javax.servlet.*;

public class DummyMonitoredItem extends GenericServlet {

    public void monitored() {}
    
    public void nested() {
        new DummyMonitoredItem().monitored();
    }

    public void init() {
        // test ordering        
    }
    
    public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
    }

    
    
}
