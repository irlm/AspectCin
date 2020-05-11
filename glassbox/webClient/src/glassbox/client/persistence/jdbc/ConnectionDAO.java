/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.persistence.jdbc;

import glassbox.client.pojo.ConnectionData;

import java.sql.*;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

public class ConnectionDAO extends BaseDAO {

	public List getAll() {
        AllConnectionMappingQuery query = new AllConnectionMappingQuery(jdbcTemplate.getDataSource()); 
        return query.execute();        
    }
	
	public Iterator iterator() {
		return getAll().iterator();
	}
	
	public ConnectionData get(Integer id) {
        ConnectionMappingQuery query = new ConnectionMappingQuery(jdbcTemplate.getDataSource()); 
        Object[] parms = new Object[1];
        parms[0] = id;
        List dataList = query.execute(parms);
        if (dataList.size() > 0)
            return (ConnectionData) dataList.get(0);
        else
            return null;
    }
		
	public void put(ConnectionData data) {
		UpdateConnection update = new UpdateConnection(jdbcTemplate.getDataSource());
		update.run(data);
	}
	
	public void add(ConnectionData data) {
		InsertConnection insert = new InsertConnection(jdbcTemplate.getDataSource());
		insert.run(data);		
	}
	
	public void delete(ConnectionData data) {
		DeleteConnection delete = new DeleteConnection(jdbcTemplate.getDataSource());
		delete.run(data);
	}
	
	private class AllConnectionMappingQuery extends MappingSqlQuery {

	    public AllConnectionMappingQuery(DataSource ds) {
	        super(ds, "SELECT id, name, url, hostname, port, protocol FROM CONNECTION");
	        compile();
	    }

	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
	        ConnectionData config = new ConnectionData();
	        config.setId((Integer) rs.getObject("id"));
	        config.setName((String) rs.getObject("name"));	        
	        config.setUrl((String) rs.getObject("url"));
	        config.setHostName((String) rs.getObject("hostname"));
	        config.setPort((String) rs.getObject("port"));
	        config.setProtocol((String) rs.getObject("protocol"));
	        config.setViewed(true);
	        //Add more!!
	        return config;
	    } 
	  }
	
	
	private class ConnectionMappingQuery extends MappingSqlQuery {

	    public ConnectionMappingQuery(DataSource ds) {
	        super(ds, "SELECT id, name, url, hostname, port, protocol FROM CONNECTION WHERE id = ?");
	        super.declareParameter(new SqlParameter("id", Types.INTEGER));
	        compile();
	    }

	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
	        ConnectionData config = new ConnectionData();
	        config.setId((Integer) rs.getObject("id"));	        
	        config.setName((String) rs.getObject("name"));	        
	        config.setUrl((String) rs.getObject("url"));
	        config.setHostName((String) rs.getObject("hostname"));
	        config.setPort((String) rs.getObject("port"));
	        config.setProtocol((String) rs.getObject("protocol"));	        
	        return config;
	    } 
	  }
	
	private class UpdateConnection extends SqlUpdate {

	    public UpdateConnection(DataSource ds) {
	        setDataSource(ds);
	        setSql("update CONNECTION set name = ?, url = ?, hostname = ?, port = ?, protocol = ? where id = ?");
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.NUMERIC));
	        compile();
	    }
	    
	    public int run(ConnectionData data) {
	        Object[] params =
	            new Object[] {
	                data.getName(),
	                data.getUrl(),
	                data.getHostName(),
	                data.getPort(),
	                data.getProtocol(),
	                data.getId()};
	        return update(params);
	    }
	}
	
	private class InsertConnection extends SqlUpdate {

	    
	    public InsertConnection(DataSource ds) {
	        setDataSource(ds);
	        setSql("insert into CONNECTION(name, url, hostName, port, protocol) values (?, ?, ?, ?, ?)");
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        declareParameter(new SqlParameter(Types.VARCHAR));	        
	        compile();
	    }
	    
	    public int run(ConnectionData data) {
	        Object[] params =
	            new Object[] {
	                data.getName(),
	                data.getUrl(),
	                data.getHostName(),
	                data.getPort(),
	                data.getProtocol()};
	        return update(params);
	    }	  
	}
	
	private class DeleteConnection extends SqlUpdate {

	    public DeleteConnection(DataSource ds) {
	        setDataSource(ds);
	        setSql("delete from CONNECTION where name = ?");
	        declareParameter(new SqlParameter(Types.VARCHAR));
	        compile();
	    }
	    
	    public int run(ConnectionData data) {
	        Object[] params =
	            new Object[] {
	                data.getName()
                };
	        return update(params);
	    }
	}
}
