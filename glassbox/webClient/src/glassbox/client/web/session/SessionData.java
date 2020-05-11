/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.web.session;

import glassbox.client.helper.ColumnHelper;

import java.io.Serializable;
import java.util.*;

public class SessionData implements Serializable {
    
    public final static String CLIENT_SESSION_KEY = "GLASSBOX_CLIENT_SESSION_KEY";
    private static final long serialVersionUID = 1L;
    
    public SessionData() {}
    
    protected Map connections = new TreeMap(); // use a sorted map: sort in alphabetical order by name!    
    protected List selectedColumns = new ArrayList();
    protected Comparator columnSorter = ColumnHelper.getDefaultSorter();
    
    public Comparator getColumnSorter() {
        return columnSorter;
    }
    public void setColumnSorter(Comparator columnSorter) {
        this.columnSorter = columnSorter;
    }
    public Map getConnections() {
        return connections;
    }
    public void setConnections(Map connections) {
        this.connections = connections;
    }
    public List getSelectedColumns() {
        return selectedColumns;
    }
    public void setSelectedColumns(List selectedColumns) {
        this.selectedColumns = selectedColumns;
    }
    
}
