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

package org.springframework.orm.jdo;

import java.sql.SQLException;
import java.util.Collection;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * SPI strategy that encapsulates certain functionality that standard JDO 1.0 does
 * not offer despite being relevant in the context of O/R mapping, such as access to
 * the underlying JDBC Connection and explicit flushing of changes to the database.
 * Also defines various further hooks that even go beyond standard JDO 2.0.
 *
 * <p>To be implemented for specific JDO providers such as JPOX, Kodo, Lido,
 * Versant Open Access. Almost every O/R-based JDO provider offers proprietary
 * means to access the underlying JDBC Connection and to explicitly flush changes;
 * hence, this would be the minimum functionality level that should be supported.
 *
 * <p>JDO 2.0 defines standard ways for most of the functionality covered here.
 * Hence, Spring's DefaultJdoDialect uses the corresponding JDO 2.0 methods
 * by default, to be overridden in a vendor-specific fashion if necessary.
 * Vendor-specific subclasses of DefaultJdoDialect are still required for special
 * transaction semantics and more sophisticated exception translation (if needed).
 *
 * <p>In general, it is recommended to derive from DefaultJdoDialect instead of
 * implementing this interface directly. This allows for inheriting common
 * behavior (present and future) from DefaultJdoDialect, only overriding
 * specific hooks to plug in concrete vendor-specific behavior.
 *
 * @author Juergen Hoeller
 * @since 02.11.2003
 * @see JdoTransactionManager#setJdoDialect
 * @see JdoAccessor#setJdoDialect
 * @see DefaultJdoDialect
 */
public interface JdoDialect {

	//-------------------------------------------------------------------------
	// Hooks for transaction management (used by JdoTransactionManager)
	//-------------------------------------------------------------------------

	/**
	 * Begin the given JDO transaction, applying the semantics specified by the
	 * given Spring transaction definition (in particular, an isolation level
	 * and a timeout). Invoked by JdoTransactionManager on transaction begin.
	 * <p>An implementation can configure the JDO Transaction object and then
	 * invoke <code>begin</code>, or invoke a special begin method that takes,
	 * for example, an isolation level.
	 * <p>An implementation can also apply read-only flag and isolation level to the
	 * underlying JDBC Connection before beginning the transaction. In that case,
	 * a transaction data object can be returned that holds the previous isolation
	 * level (and possibly other data), to be reset in <code>cleanupTransaction</code>.
	 * <p>Implementations can also use the Spring transaction name, as exposed by the
	 * passed-in TransactionDefinition, to optimize for specific data access use cases
	 * (effectively using the current transaction name as use case identifier).
	 * @param transaction the JDO transaction to begin
	 * @param definition the Spring transaction definition that defines semantics
	 * @return an arbitrary object that holds transaction data, if any
	 * (to be passed into cleanupTransaction)
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @throws TransactionException in case of invalid arguments
	 * @see #cleanupTransaction
	 * @see javax.jdo.Transaction#begin
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#prepareConnectionForTransaction
	 */
	Object beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException;

	/**
	 * Clean up the transaction via the given transaction data.
	 * Invoked by JdoTransactionManager on transaction cleanup.
	 * <p>An implementation can, for example, reset read-only flag and
	 * isolation level of the underlying JDBC Connection. Furthermore,
	 * an exposed data access use case can be reset here.
	 * @param transactionData arbitrary object that holds transaction data, if any
	 * (as returned by beginTransaction)
	 * @see #beginTransaction
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#resetConnectionAfterTransaction
	 */
	void cleanupTransaction(Object transactionData);

	/**
	 * Retrieve the JDBC Connection that the given JDO PersistenceManager uses underneath,
	 * if accessing a relational database. This method will just get invoked if actually
	 * needing access to the underlying JDBC Connection, usually within an active JDO
	 * transaction (for example, by JdoTransactionManager). The returned handle will
	 * be passed into the <code>releaseJdbcConnection</code> method when not needed anymore.
	 * <p>This strategy is necessary as JDO 1.0 does not provide a standard way to retrieve
	 * the underlying JDBC Connection (due to the fact that a JDO provider might not work
	 * with a relational database at all).
	 * <p>Implementations are encouraged to return an unwrapped Connection object, i.e.
	 * the Connection as they got it from the connection pool. This makes it easier for
	 * application code to get at the underlying native JDBC Connection, like an
	 * OracleConnection, which is sometimes necessary for LOB handling etc. We assume
	 * that calling code knows how to properly handle the returned Connection object.
	 * <p>In a simple case where the returned Connection will be auto-closed with the
	 * PersistenceManager or can be released via the Connection object itself, an
	 * implementation can return a SimpleConnectionHandle that just contains the
	 * Connection. If some other object is needed in <code>releaseJdbcConnection</code>,
	 * an implementation should use a special handle that references that other object.
	 * @param pm the current JDO PersistenceManager
	 * @param readOnly whether the Connection is only needed for read-only purposes
	 * @return a handle for the JDBC Connection, to be passed into
	 * <code>releaseJdbcConnection</code>, or <code>null</code>
	 * if no JDBC Connection can be retrieved
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @see #releaseJdbcConnection
	 * @see org.springframework.jdbc.datasource.ConnectionHandle#getConnection
	 * @see org.springframework.jdbc.datasource.SimpleConnectionHandle
	 * @see JdoTransactionManager#setDataSource
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
	 */
	ConnectionHandle getJdbcConnection(PersistenceManager pm, boolean readOnly)
			throws JDOException, SQLException;

	/**
	 * Release the given JDBC Connection, which has originally been retrieved
	 * via <code>getJdbcConnection</code>. This should be invoked in any case,
	 * to allow for proper release of the retrieved Connection handle.
	 * <p>An implementation might simply do nothing, if the Connection returned
	 * by <code>getJdbcConnection</code> will be implicitly closed when the JDO
	 * transaction completes or when the PersistenceManager is closed.
	 * @param conHandle the JDBC Connection handle to release
	 * @param pm the current JDO PersistenceManager
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @see #getJdbcConnection
	 */
	void releaseJdbcConnection(ConnectionHandle conHandle, PersistenceManager pm)
			throws JDOException, SQLException;


	//-------------------------------------------------------------------------
	// Hooks for special data access operations (used by JdoTemplate)
	//-------------------------------------------------------------------------

	/**
	 * Detach a copy of the given persistent instance from the current JDO transaction,
	 * for use outside a JDO transaction (for example, as web form object).
	 * @param pm the current JDO PersistenceManager
	 * @param entity the persistent instance to detach
	 * @throws JDOException in case of errors
	 * @see javax.jdo.PersistenceManager#detachCopy(Object)
	 */
	Object detachCopy(PersistenceManager pm, Object entity) throws JDOException;

	/**
	 * Detach copies of the given persistent instances from the current JDO transaction,
	 * for use outside a JDO transaction (for example, as web form objects).
	 * @param pm the current JDO PersistenceManager
	 * @param entities the persistent instances to detach
	 * @throws JDOException in case of errors
	 * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
	 */
	Collection detachCopyAll(PersistenceManager pm, Collection entities) throws JDOException;

	/**
	 * Reattach the given detached instance (for example, a web form object) with
	 * the current JDO transaction, merging its changes into the current persistence
	 * instance that represents the corresponding entity.
	 * @param pm the current JDO PersistenceManager
	 * @param detachedEntity the detached instance to attach
	 * @return the corresponding persistent instance
	 * @throws JDOException in case of errors
	 * @see javax.jdo.PersistenceManager#makePersistent(Object)
	 */
	Object attachCopy(PersistenceManager pm, Object detachedEntity) throws JDOException;

	/**
	 * Reattach the given detached instances (for example, web form objects) with
	 * the current JDO transaction, merging their changes into the current persistence
	 * instances that represent the corresponding entities.
	 * @param pm the current JDO PersistenceManager
	 * @param detachedEntities the detached instances to reattach
	 * @return the corresponding persistent instances
	 * @throws JDOException in case of errors
	 * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
	 */
	Collection attachCopyAll(PersistenceManager pm, Collection detachedEntities) throws JDOException;

	/**
	 * Flush the given PersistenceManager, i.e. flush all changes (that have been
	 * applied to persistent objects) to the underlying database. This method will
	 * just get invoked when eager flushing is actually necessary, for example when
	 * JDBC access code needs to see changes within the same transaction.
	 * @param pm the current JDO PersistenceManager
	 * @throws JDOException in case of errors
	 * @see JdoAccessor#setFlushEager
	 */
	void flush(PersistenceManager pm) throws JDOException;

	/**
	 * Create a new Query object for the given named query.
	 * @param pm the current JDO PersistenceManager
	 * @param entityClass a persistent class
	 * @param queryName the name of the query
	 * @return the Query object
	 * @throws JDOException in case of errors
	 * @see javax.jdo.PersistenceManager#newNamedQuery(Class, String)
	 */
	Query newNamedQuery(PersistenceManager pm, Class entityClass, String queryName) throws JDOException;

	/**
	 * Apply the given timeout to the given JDO query object.
	 * <p>Invoked by JdoTemplate with the remaining time of a specified
	 * transaction timeout, if any.
	 * @param query the JDO query object to apply the timeout to
	 * @param timeout the timeout value to apply
	 * @throws JDOException if thrown by JDO methods
	 * @see JdoTemplate#prepareQuery
	 */
	void applyQueryTimeout(Query query, int timeout) throws JDOException;


	//-----------------------------------------------------------------------------------
	// Hook for exception translation (used by JdoTransactionManager and JdoTemplate)
	//-----------------------------------------------------------------------------------

	/**
	 * Translate the given JDOException to a corresponding exception from Spring's
	 * generic DataAccessException hierarchy. An implementation should apply
	 * PersistenceManagerFactoryUtils' standard exception translation if can't do
	 * anything more specific.
	 * <p>Of particular importance is the correct translation to
	 * DataIntegrityViolationException, for example on constraint violation.
	 * Unfortunately, standard JDO does not allow for portable detection of this.
	 * <p>Can use a SQLExceptionTranslator for translating underlying SQLExceptions
	 * in a database-specific fashion.
	 * @param ex the JDOException thrown
	 * @return the corresponding DataAccessException (must not be <code>null</code>)
	 * @see JdoAccessor#convertJdoAccessException
	 * @see JdoTransactionManager#convertJdoAccessException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 * @see org.springframework.dao.DataIntegrityViolationException
	 * @see org.springframework.jdbc.support.SQLExceptionTranslator
	 */
	DataAccessException translateException(JDOException ex);

}
