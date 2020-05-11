/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.persistence.jdbc;

import glassbox.client.pojo.MailConfigurationData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.object.MappingSqlQuery;

public class MailDAO extends BaseDAO {


	public boolean isMailConfigured() {		
		if(getAll().size() > 0)
		  return true;
		return false;
	}
	
	
	public List getAll() {
        AllMailMappingQuery query = new AllMailMappingQuery(jdbcTemplate.getDataSource()); 
        return query.execute();        
    }
	
	
	private class AllMailMappingQuery extends MappingSqlQuery {

	    public AllMailMappingQuery(DataSource ds) {
	        super(ds, "SELECT id, name, url, hostname, port, protocol FROM mail");
	        compile();
	    }

	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
	        MailConfigurationData config = new MailConfigurationData();
	        config.setId((Integer) rs.getObject("id"));
	        config.setName((String) rs.getObject("name"));	        
	        config.setSmtp((String) rs.getObject("smtp"));
	        config.setUserName((String) rs.getObject("username"));
	        config.setPassword((String) rs.getObject("password"));
	        return config;
	    } 
	  }	
}
