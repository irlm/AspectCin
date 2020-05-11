/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * A SimpleJdbcCall is a multi-threaded, reusable object representing a call to a stored procedure or a
 * stored function.  It provides meta data processing to simplify the code need to access basic
 * stored procedures/functions.  All you need to provide is the name of the procedure/fumnction and a Map
 * containing the parameters when you execute the call.  The names of the supplied parameters will be matched
 * up with in and out parameters declared when the stored procedure was created.
 *
 * The meta data processing is based on the DatabaseMetaData provided by the JDBC driver.  Since we rely 
 * on the JDBC driver this "auto-detection" can only be used for databases that are known to provide accurate
 * meta data.  These currently include Derby, MySQL, Microsoft SQL Server, Oracle and DB2.
 * For any other databases you are required to declare all parameters explicitly.  You can of course declare all
 * parameters explictly even if the database provides the necessary meta data.  In that case your declared parameters
 * will take precedence.  You can also turn off any mete data processing if you want to use parameter names that do not
 * match what is declared during the stored procedure compilation.
 *
 * The actual call is being handled via the standard call method of the JdbcTemplate.
 *
 * Many of the configuration methods return the current instance of the SimpleJdbcCall to provide the ablity
 * to string multiple ones tgether in a "fluid" interface style.
 * 
 * @author trisberg
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see java.sql.DatabaseMetaData
 */
public class SimpleJdbcCall extends AbstractJdbcCall implements SimpleJdbcCallOperations {

	/**
	 * Constructor that takes one parameter with the JDBC DataSource to use when creating the
	 * JdbcTemplate.
	 * @param dataSource the <code>DataSource</code> to use
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Alternative Constructor that takes one parameter with the JdbcTemplate to be used.
	 * @param jdbcTemplate the <code>JdbcTemplate</code> to use
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcCall(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}

	public SimpleJdbcCall withProcedureName(String procedureName) {
		setProcedureName(procedureName);
		setFunction(false);
		return this;
	}

	public SimpleJdbcCall withFunctionName(String functionName) {
		setProcedureName(functionName);
		setFunction(true);
		return this;
	}

	public SimpleJdbcCall withSchemaName(String schemaName) {
		setSchemaName(schemaName);
		return this;
	}

	public SimpleJdbcCall withCatalogName(String catalogName) {
		setCatalogName(catalogName);
		return this;
	}

	public SimpleJdbcCall withReturnValue() {
		setReturnValueRequired(true);
		return this;
	}

	public SimpleJdbcCall declareParameters(SqlParameter... sqlParameters) {
		for (SqlParameter sqlParameter : sqlParameters) {
			if (sqlParameter != null)
				addDeclaredParameter(sqlParameter);
		}
		return this;
	}

	public SimpleJdbcCall useInParameterNames(String... inParameterNames) {
		return this;
	}

	public SimpleJdbcCall useOutParameterNames(String... inParameterNames) {
		return this;
	}

	public SimpleJdbcCall withoutProcedureColumnMetaDataAccess() {
		setAccessCallParameterMetaData(false);
		return this;
	}

	public <T> T executeFunction(Class<T> returnType, Map args) {
		return (T) execute(args).get(getScalarOutParameterName());
	}

	public <T> T executeFunction(Class<T> returnType, MapSqlParameterSource args) {
		return (T) execute(args).get(getScalarOutParameterName());

	}

	public <T> T executeObject(Class<T> returnType, Map args) {
		return (T) execute(args).get(getScalarOutParameterName());

	}

	public <T> T executeObject(Class<T> returnType, MapSqlParameterSource args) {
		return (T) execute(args).get(getScalarOutParameterName());

	}

	public Map<String, Object> execute() {
		return execute(new HashMap<String, Object>());
	}

	public Map<String, Object> execute(Map<String, Object> args) {
		return doExecute(args);
	}

	public Map<String, Object> execute(SqlParameterSource parameterSource) {
		return doExecute(parameterSource);
	}

}
