/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.persistence.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDAO {

	private static final Log log = LogFactory.getLog(BaseDAO.class);
	
	protected JdbcTemplate jdbcTemplate = null;
	protected Resource resourceTemplate = null;
		
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public Resource getResourceTemplate() {
		return resourceTemplate;
	}

	public void setResourceTemplate(Resource resourceTemplate) {
		this.resourceTemplate = resourceTemplate;
	}
	
	public void shutdown() {
		jdbcTemplate.execute("SHUTDOWN");
	}
	
	public void defragment() {
		jdbcTemplate.execute("CHECKPOINT DEFRAG");
	}
    
}
