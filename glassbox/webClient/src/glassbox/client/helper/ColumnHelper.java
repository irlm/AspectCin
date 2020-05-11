/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.client.pojo.OperationData;
import glassbox.client.web.session.SessionData;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ColumnHelper extends BaseHelper {

    // Singleton doesn't work in the default configuration - Spring generates subclasses...
    protected Collection columns = null;
    protected static final Map sorters = makeSorters();    
    protected static final Map reverseSorters = makeReverseSorters();
    private static final Log logger = LogFactory.getLog(ColumnHelper.class);
    
    public final static String STATUS        =   "status";
    public final static String ANALYSIS      =   "analysis";
    public final static String SERVER        =   "server"; 
    public final static String OPERATION     =   "operation"; 
    public final static String APPLICATION   =   "application"; 
    public final static String EXECUTIONS    =   "executions";  
    public final static String AVERAGE_TIME  =   "averageTime";  
    
    protected static Map makeSorters() {
        Map sorters = new HashMap(10); // create some spare room to avoid collisions
        OperationDataSorter averageTimeSorter = new AverageTimeSorter(null);
        OperationDataSorter statusSorter = new StatusSorter(averageTimeSorter);
        OperationDataSorter analysisSorter = new AnalysisSorter(statusSorter);
        OperationDataSorter serverSorter = new ServerSorter(statusSorter);
        OperationDataSorter operationShortNameSorter = new OperationShortNameSorter(statusSorter);
        OperationDataSorter applicationShortNameSorter = new ApplicationShortNameSorter(statusSorter);        
        OperationDataSorter executionSorter = new ExecutionSorter(statusSorter);
        
        sorters.put(STATUS, statusSorter);
        sorters.put(ANALYSIS, analysisSorter);
        sorters.put(SERVER, serverSorter);
        sorters.put(OPERATION, operationShortNameSorter);
        sorters.put(APPLICATION, applicationShortNameSorter);
        sorters.put(EXECUTIONS, executionSorter);
        sorters.put(AVERAGE_TIME, averageTimeSorter);
        
        return sorters;
    }

    protected static Map makeReverseSorters() {
        Map reverseSorters = new HashMap(10);
        for (Iterator it = sorters.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            reverseSorters.put(entry.getKey(), new ReverseComparator((Comparator) entry.getValue()));
        }
        return reverseSorters;
    }    
    
    public Collection getColumns() {      
        return columns;
    }

    public void setColumns(Collection columns) {
        this.columns = columns;
    }  

    public static Comparator getSorter(String sortName) {
        return (Comparator)sorters.get(sortName);
    }

    public static Comparator getReverseSorter(String sortName) {
        return (Comparator)reverseSorters.get(sortName);
    }
    
    public void setColumnSorter(HttpSession session, String sortName){
        SessionData sessionData = getSessionData(session);
        Comparator newSorter = getSorter(sortName);
        if (newSorter == null) {
            logger.error("unknown sort order: "+sortName);
            newSorter = getDefaultSorter(); // recover from error
        } else if (sessionData.getColumnSorter().equals(newSorter)){
            newSorter = getReverseSorter(sortName);
        }
        sessionData.setColumnSorter(newSorter);        
    }
    
    public static Comparator getDefaultSorter() {
        return (Comparator)sorters.get(STATUS);
    }
    
    protected abstract static class OperationDataSorter implements Comparator, Serializable {
        public OperationDataSorter(OperationDataSorter defaultSorter) {
            this.defaultSorter = defaultSorter;
        }
        
        public int compare(Object arg0, Object arg1) {
            OperationData data1 = (OperationData)arg0;
            OperationData data2 = (OperationData)arg1;
            int delta = compareOperations(data1, data2);
            if (delta == 0) {
                if (defaultSorter != null) {
                    return defaultSorter.compareOperations(data1, data2);
                }
            }
            return delta;
        }
        
        public abstract int compareOperations(OperationData data1, OperationData data2);
        
        private OperationDataSorter defaultSorter;
    }
        
    public static class StatusSorter extends OperationDataSorter {

        public StatusSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }

        public int compareOperations(OperationData data1, OperationData data2) {
            if(data1.isOk() && (data2.isSlow() || data2.isFailing())    ) {
                return 1;
            } else  if(data1.isSlow() &&  data2.isFailing()) {
                return 1;
            } else if((data1.isSlow() || data1.isFailing()) && data2.isOk()) {
                return -1;
            } else  if(data1.isFailing() && data2.isSlow()) {
                return -1;
            }
            return 0;
        }        
    }
    
    public static class AnalysisSorter extends OperationDataSorter {

        public AnalysisSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        public int compareOperations(OperationData data1, OperationData data2) {
            return data1.getAnalysis().compareTo(data2.getAnalysis());
        }
    }
    
    
    public static class AverageTimeSorter extends OperationDataSorter {

        public AverageTimeSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        /**
         * note: this sorts in reverse order
         */
        public int compareOperations(OperationData data1, OperationData data2) {
            return data2.getAverageExecutionTimeAsDouble().compareTo(data1.getAverageExecutionTimeAsDouble());
        }
    }    
    
    public static class ServerSorter extends OperationDataSorter {

        public ServerSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        public int compareOperations(OperationData data1, OperationData data2) {
            return data1.getAgentName().toLowerCase().compareTo(data2.getAgentName().toLowerCase());
        }     
    }
    
    
    public static class OperationShortNameSorter extends OperationDataSorter {

        public OperationShortNameSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        public int compareOperations(OperationData data1, OperationData data2) {
            return data1.getOperationShortName().toLowerCase().compareTo(data2.getOperationShortName().toLowerCase());
        }
        
    }
    
    public static class ApplicationShortNameSorter extends OperationDataSorter {

        public ApplicationShortNameSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        public int compareOperations(OperationData data1, OperationData data2) {
            return data1.getApplicationName().toLowerCase().compareTo(data2.getApplicationName().toLowerCase());
        }
        
    }


    public static class ExecutionSorter extends OperationDataSorter {

        public ExecutionSorter(OperationDataSorter defaultSorter) {
            super(defaultSorter);
        }
        
        /**
         * note: this sorts in reverse order
         */
        public int compareOperations(OperationData data1, OperationData data2) {
            return data2.getExecutions().compareTo(data1.getExecutions());
        }        
    }
    
}