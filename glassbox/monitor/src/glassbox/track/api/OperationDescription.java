/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.config.extension.api.PluginTracking.PluginHolder;

import java.io.Serializable;

public interface OperationDescription extends CallDescription, PluginHolder {

    /**
     * 
     * @return value of the type of operation: typically a class name like <code>javax.servlet.HttpServlet<code> or
     * <code>org.apache.struts.action.Action</code> 
     */
    String getOperationType();

    /**
     * 
     * @return descriptive name for the operation, e.g., the name of the servlet or action.
     */
    String getOperationName();

    /**
     * 
     * @return short name for the operation, e.g., the name of the servlet or action.
     */
    String getShortName();

    /**
     * 
     * @return name for the application context, e.g., the name of the servlet context
     */
    String getContextName();

    int callType();

    String getSummary();

    Serializable getCallKey();

    Serializable getResourceKey();

    /**
     * @return the parent
     */
    OperationDescription getParent();

    /**
     * @param parent the parent to set
     */
    void setParent(OperationDescription parent);

}