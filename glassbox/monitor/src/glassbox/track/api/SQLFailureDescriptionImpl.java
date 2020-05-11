/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.sql.SQLException;

public class SQLFailureDescriptionImpl extends FailureDescriptionImpl implements SQLFailureDescription {

    private int errorCode;
    private String sqlState;
    
    public SQLFailureDescriptionImpl(SQLException sqlException) {
        super(sqlException);
        errorCode = sqlException.getErrorCode();
        sqlState = sqlException.getSQLState();
    }

    public int getSQLErrorCode() {
        return errorCode;
    }

    public String getSQLState() {
        return sqlState;
    }

    public boolean equals(Object oth) {
        if (!super.equals(oth)) {
            return false;
        }
        
        SQLFailureDescriptionImpl other = (SQLFailureDescriptionImpl)oth;
        if (errorCode != other.errorCode) {
            return false;
        }
        
        return eq(sqlState, other.sqlState);
    }
    
    public int hashCode() {
        return super.hashCode() * errorCode ^ 0xa23c4ad7 + (sqlState == null ? 0x172c3d4 : (sqlState.hashCode()<<3)*772);
    }
    
}
