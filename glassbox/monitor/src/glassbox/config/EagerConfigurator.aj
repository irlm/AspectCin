/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import glassbox.monitor.MonitoredType;

import java.rmi.Remote;
import java.sql.Connection;

/**
 * This aspect is packaged with the monitor but is NOT deployed by default.
 * It is designed for use outside of Web containers, where starting a Web app is not a good option.
 */
public aspect EagerConfigurator {
    declare parents: org.apache.struts.action.Action+ implements MonitoredType;
    declare parents: org.springframework.web.servlet.mvc.Controller+ implements MonitoredType;
    declare parents: javax.servlet.Servlet+ implements MonitoredType;
    declare parents: Connection+ implements MonitoredType;

    declare parents: Remote+ implements MonitoredType;
}
