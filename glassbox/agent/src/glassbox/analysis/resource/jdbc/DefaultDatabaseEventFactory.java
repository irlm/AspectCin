/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.resource.jdbc;

import glassbox.monitor.resource.JdbcMonitor;
import glassbox.monitor.resource.MonitoredPreparedStatement;
import glassbox.track.api.*;

import java.sql.SQLException;

import org.springframework.dao.*;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * Conservative detection strategy: failures from SQL that often
 * indicate valid business conditions are not flagged as failures.
 * 
 * Also detects & packages up database data when flagging events.
 */
public class DefaultDatabaseEventFactory extends DefaultFailureDetectionStrategy implements DatabaseEventFactory {

    private SQLExceptionTranslator errorTranslator = new SQLStateSQLExceptionTranslator();//new SQLErrorCodeSQLExceptionTranslator();    
    
//    public Response createSlow(UsageTrackingInfo elapsed, MonitoredPreparedStatement monitoredStatement, String SQL) {
//        //better: change the topic (e.g., glassbox.track.exceptions.slowPreparedStatement)
//        DefaultResponse request = new DefaultResponse(elapsed, SQL, JdbcMonitor.getParameterList(monitoredStatement));
//        request.setDescription("Slow prepared statement: "+SQL+", parameters = "+request.getParameterString());
//        return request;
//    }
//
//    public Response createSlow(UsageTrackingInfo elapsed, String SQL, String summary) {
//        String description = "Slow dynamic SQL: "+SQL; 
//        String params = describeParams(SQL, summary);
//        return new DefaultResponse(elapsed, SQL, params, description); 
//    }
//    
    public String describeParams(String SQL, String summary) {
        int skipPos = SQL.indexOf(summary);
        if (skipPos != -1) {
            SQL = SQL.substring(0, skipPos)+SQL.substring(skipPos+summary.length());
        }
        return SQL;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.resource.jdbc.DatabaseEventFactory#getFailureDescription(java.lang.Throwable, org.aspectj.lang.JoinPoint.StaticPart, glassbox.track.api.PerfStats)
     */
    public FailureDescription getFailureDescription(Throwable t) {
        if (t instanceof SQLException) {            
            //unpack the root exception, so we don't lose error codes etc. in translation
            SQLException sqlException = (SQLException)t;
            for (;;) {
                Throwable next = causeStrategy.getCause(sqlException);
                if (next instanceof SQLException) {
                    sqlException = (SQLException)next;
                } else {
                    break;
                }
            }
            
            FailureDescriptionImpl description = new SQLFailureDescriptionImpl(sqlException);
            
            DataAccessException dataAccessException = errorTranslator.translate("", "", sqlException);                
                
            if (dataAccessException instanceof ConcurrencyFailureException || dataAccessException instanceof DataIntegrityViolationException) {
                // either of these can indicate a normal condition
                description.severity = NORMAL;
            } else {
                description.severity = FAILURE;
            }
            
            Class excClass;
            if (dataAccessException instanceof UncategorizedDataAccessException) {
                excClass = t.getClass();
            } else {
                excClass = dataAccessException.getClass();
            }
            
            description.summary = trim("a data access problem: "+excClass.getName()+", SQL state ["+sqlException.getSQLState()+
                "], SQL error code ["+sqlException.getErrorCode()+"]: "+message(t));
            
            return description;
        }
        return null;
    }
    
    private static final long serialVersionUID = 2;
}
