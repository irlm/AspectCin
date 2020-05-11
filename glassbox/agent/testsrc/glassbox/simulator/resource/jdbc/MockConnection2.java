/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MockConnection2 extends MockConnection {
	public PreparedStatement prepareStatement(String sql) throws SQLException {
        return (PreparedStatement) new MockPreparedStatement(sql, this);
	}

    public Statement createStatement() throws SQLException {
        return new MockStatement(this);
    }
}
