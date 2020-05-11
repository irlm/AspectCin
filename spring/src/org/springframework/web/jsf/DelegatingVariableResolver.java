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

package org.springframework.web.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

/**
 * JSF <code>VariableResolver</code> that first delegates to the original
 * resolver of the underlying JSF implementation, then to the Spring root
 * <code>WebApplicationContext</code>.
 *
 * <p>Configure this resolver in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;application>
 *   ...
 *   &lt;variable-resolver>org.springframework.web.jsf.DelegatingVariableResolver&lt;/variable-resolver>
 * &lt;/application></pre>
 *
 * All your JSF expressions can then implicitly refer to the names of
 * Spring-managed service layer beans, for example in property values of
 * JSF-managed beans:
 *
 * <pre>
 * &lt;managed-bean>
 *   &lt;managed-bean-name>myJsfManagedBean&lt;/managed-bean-name>
 *   &lt;managed-bean-class>example.MyJsfManagedBean&lt;/managed-bean-class>
 *   &lt;managed-bean-scope>session&lt;/managed-bean-scope>
 *   &lt;managed-property>
 *     &lt;property-name>mySpringManagedBusinessObject&lt;/property-name>
 *     &lt;value>#{mySpringManagedBusinessObject}&lt;/value>
 *   &lt;/managed-property>
 * &lt;/managed-bean></pre>
 *
 * with "mySpringManagedBusinessObject" defined as Spring bean in
 * applicationContext.xml:
 *
 * <pre>
 * &lt;bean id="mySpringManagedBusinessObject" class="example.MySpringManagedBusinessObject">
 *   ...
 * &lt;/bean></pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see WebApplicationContextVariableResolver
 * @see FacesContextUtils#getRequiredWebApplicationContext
 */
public class DelegatingVariableResolver extends VariableResolver {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	protected final VariableResolver originalVariableResolver;


	/**
	 * Create a new DelegatingVariableResolver, using the given original VariableResolver.
	 * <p>A JSF implementation will automatically pass its original resolver into the
	 * constructor of a configured resolver, provided that there is a corresponding
	 * constructor argument.
	 * @param originalVariableResolver the original VariableResolver
	 */
	public DelegatingVariableResolver(VariableResolver originalVariableResolver) {
		Assert.notNull(originalVariableResolver, "Original JSF VariableResolver must not be null");
		this.originalVariableResolver = originalVariableResolver;
	}

	/**
	 * Return the original JSF VariableResolver that this resolver delegates to.
	 * Used to resolve standard JSF-managed beans.
	 */
	protected final VariableResolver getOriginalVariableResolver() {
		return this.originalVariableResolver;
	}


	/**
	 * Delegate to the original VariableResolver first, then try to
	 * resolve the variable as Spring bean in the root WebApplicationContext.
	 */
	public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException {
		// Ask original JSF variable resolver.
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve variable '" + name + "' in via original VariableResolver");
		}
		Object originalResult = getOriginalVariableResolver().resolveVariable(facesContext, name);
		if (originalResult != null) {
			return originalResult;
		}

		// Ask Spring root application context.
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve variable '" + name + "' in root WebApplicationContext");
		}
		BeanFactory bf = getBeanFactory(facesContext);
		if (bf.containsBean(name)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully resolved variable '" + name + "' in root WebApplicationContext");
			}
			return bf.getBean(name);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not resolve variable '" + name + "'");
		}
		return null;
	}

	/**
	 * Retrieve the Spring BeanFactory to delegate bean name resolution to.
	 * <p>The default implementation delegates to <code>getWebApplicationContext</code>.
	 * Can be overridden to provide an arbitrary BeanFactory reference to resolve
	 * against; usually, this will be a full Spring ApplicationContext.
	 * @param facesContext the current JSF context
	 * @return the Spring BeanFactory (never <code>null</code>)
	 * @see #getWebApplicationContext
	 */
	protected BeanFactory getBeanFactory(FacesContext facesContext) {
		return getWebApplicationContext(facesContext);
	}

	/**
	 * Retrieve the web application context to delegate bean name resolution to.
	 * <p>The default implementation delegates to FacesContextUtils.
	 * @param facesContext the current JSF context
	 * @return the Spring web application context (never <code>null</code>)
	 * @see FacesContextUtils#getRequiredWebApplicationContext
	 */
	protected WebApplicationContext getWebApplicationContext(FacesContext facesContext) {
		return FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}

}
