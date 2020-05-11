/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.persistence.jdbc;

import glassbox.client.pojo.ConfigurationData;
import glassbox.web.ContextLoaderServlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

public class ConfigurationDAO extends BaseDAO {
		
	private static final Log log = LogFactory.getLog(ConfigurationDAO.class);
		
	protected String databaseCheck = null;

	
	public boolean configure() {
        if (!hasConfiguration()) {
            return false;
        }
        
        try {
    		if(!getConfiguration()) {
    			log.debug("--> Setting db configuration...  ");
    			return setConfiguration();
    		}
            return true;
        } catch (Throwable t) {
            System.setProperty("glassbox.config.ds", ContextLoaderServlet.NO_DATABASE_URL);
            //XXX - temporarily allow this
            log.debug("Can't connect to database", t);
            return false;
        }            
	}

    public boolean hasConfiguration() {
        return !ContextLoaderServlet.NO_DATABASE_URL.equals(System.getProperty("glassbox.config.ds", ContextLoaderServlet.NO_DATABASE_URL));
    }

	public boolean getConfiguration() {
		boolean found = false;
		try {
			DatabaseMetaData source = jdbcTemplate.getDataSource().getConnection().getMetaData();			
			ResultSet rs = source.getTables(null, null, "%", null);
			log.debug("Database Table: " + rs + " CHECK: " + databaseCheck);
			while (rs.next()) {				
				String s = rs.getString(3);
				log.debug("Database Table: " + s);
				if (s.equalsIgnoreCase(databaseCheck)) {
					found = true;
				}
			}
			log.debug("DATABASE INITIALIZED?? : " + found);			
		} catch (SQLException e) {			
			log.error("Can't read Glassbox configuration data", e);
		}
		return found;
	}
	
	public boolean setConfiguration() {
		try { 
			BufferedReader buff = null;
			if((buff = new BufferedReader(new InputStreamReader(resourceTemplate.getInputStream()))) != null) { 
				String fileString = "";
				String line = null;
				while((line = buff.readLine()) != null)
					fileString += line;			
				jdbcTemplate.execute(fileString); 
			}
            return true;
		} catch(Exception e) {
			log.error("Can't save Glassbox configuration data", e);
            return false;
		}		
	}
	
	public void insert() {
		Random rand = new Random();
		jdbcTemplate.execute("insert into CONFIGURATION(name) values ('" + rand.nextInt()+"')");
	}
	
	public String getDatabaseCheck() {
		return databaseCheck;
	}

	public void setDatabaseCheck(String databaseCheck) {
		this.databaseCheck = databaseCheck;
	}	
	
	public List getAll() {
        AllConfigurationMappingQuery query = new AllConfigurationMappingQuery(jdbcTemplate.getDataSource()); 
        return query.execute();        
    }
	
	public ConfigurationData get(Integer id) {
        ConfigurationMappingQuery query = new ConfigurationMappingQuery(jdbcTemplate.getDataSource()); 
        Object[] parms = new Object[1];
        parms[0] = id;
        List dataList = query.execute(parms);
        if (dataList.size() > 0)
            return (ConfigurationData) dataList.get(0);
        else
            return null;
    }
	
	
	public void put(ConfigurationData data) {
		UpdateConfiguration update = new UpdateConfiguration(jdbcTemplate.getDataSource());
		update.run(data.getId(), data.getLastUsed());
	}
	
	public void add(ConfigurationData data) {
		InsertConfiguration insert = new InsertConfiguration(jdbcTemplate.getDataSource());
		insert.run(data);
	}
	
	public void delete(ConfigurationData data) {
		DeleteConfiguration delete = new DeleteConfiguration(jdbcTemplate.getDataSource());
		delete.run(data);
	}
	
	private class AllConfigurationMappingQuery extends MappingSqlQuery {

	    public AllConfigurationMappingQuery(DataSource ds) {
	        super(ds, "SELECT id FROM CONFIGURATION");
	        compile();
	    }

	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
	        ConfigurationData config = new ConfigurationData();
	        config.setId((Integer) rs.getObject("id"));	        
	        //Add more!!
	        return config;
	    } 
	  }
	
	
	private class ConfigurationMappingQuery extends MappingSqlQuery {

	    public ConfigurationMappingQuery(DataSource ds) {
	        super(ds, "SELECT id, FROM CONFIGURATION WHERE id = ?");
	        super.declareParameter(new SqlParameter("id", Types.INTEGER));
	        compile();
	    }

	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
	        ConfigurationData config = new ConfigurationData();
	        config.setId((Integer) rs.getObject("id"));	        
	        //Add more!!
	        return config;
	    } 
	  }
	
	private class UpdateConfiguration extends SqlUpdate {

	    public UpdateConfiguration(DataSource ds) {
	        setDataSource(ds);
	        setSql("update CONFIGURATION set lastUsed = ? where id = ?");
	        declareParameter(new SqlParameter(Types.TIMESTAMP));
	        declareParameter(new SqlParameter(Types.NUMERIC));
	        compile();
	    }
	    
	    public int run(Integer id, Timestamp date) {
	        Object[] params =
	            new Object[] {
	                date,
	                id};
	        return update(params);
	    }
	}
	
	private class InsertConfiguration extends SqlUpdate {

	    public InsertConfiguration(DataSource ds) {
	        setDataSource(ds);
	        setSql("insert into CONFIGURATION values(?)");
	        declareParameter(new SqlParameter(Types.TIMESTAMP));
	        compile();
	    }
	    
	    public int run(ConfigurationData data) {
	        Object[] params =
	            new Object[] {
	                data.getLastUsed()};
	        return update(params);
	    }
	}
	
	private class DeleteConfiguration extends SqlUpdate {

	    public DeleteConfiguration(DataSource ds) {
	        setDataSource(ds);
	        setSql("delete from CONFIGURATION where id = ?");
	        declareParameter(new SqlParameter(Types.NUMERIC));
	        compile();
	    }
	    
	    public int run(ConfigurationData data) {
	        Object[] params =
	            new Object[] {
	                data.getId()};
	        return update(params);
	    }
	}	
}
