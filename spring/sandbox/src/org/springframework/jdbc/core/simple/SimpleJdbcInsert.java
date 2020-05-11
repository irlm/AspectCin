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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;

/**
 * @author trisberg
 */
public class SimpleJdbcInsert extends AbstractJdbcInsert implements SimpleJdbcInsertOperations {

	/**
	 * Constructor that takes one parameter with the JDBC DataSource to use when creating the
	 * JdbcTemplate.
	 * @param dataSource the <code>DataSource</code> to use
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcInsert(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Alternative Constructor that takes one parameter with the JdbcTemplate to be used.
	 * @param jdbcTemplate the <code>JdbcTemplate</code> to use
	 * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource
	 */
	public SimpleJdbcInsert(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}

	public SimpleJdbcInsert withTableName(String tableName) {
		setTableName(tableName);
		return this;
	}

	public SimpleJdbcInsert withSchemaName(String schemaName) {
		setSchemaName(schemaName);
		return this;
	}

	public SimpleJdbcInsert withCatalogName(String catalogName) {
		setCatalogName(catalogName);
		return this;
	}

	public SimpleJdbcInsert usingColumns(String... columnNames) {
		setColumnNames(Arrays.asList(columnNames));
		return this;
	}

	public SimpleJdbcInsert usingGeneratedKeyColumns(String... columnNames) {
		setGeneratedKeyNames(columnNames);
		return this;
	}

	public int execute(Map<String, Object> args) {
		return doExecute(args);
	}

	public int execute(SqlParameterSource parameterSource) {
		return doExecute(parameterSource);
	}

	public Number executeAndReturnKey(Map<String, Object> args) {
		return doExecuteAndReturnKey(args);
	}

	public Number executeAndReturnKey(SqlParameterSource parameterSource) {
		return doExecuteAndReturnKey(parameterSource);
	}

	public KeyHolder executeAndReturnKeyHolder(Map<String, Object> args) {
		return doExecuteAndReturnKeyHolder(args);
	}

	public KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource) {
		return doExecuteAndReturnKeyHolder(parameterSource);
	}

	public int[] executeBatch(Map<String, Object>[] batch) {
		return doExecuteBatch(batch);
	}

	public int[] executeBatch(SqlParameterSource[] batch) {
		return doExecuteBatch(batch);
	}
}
