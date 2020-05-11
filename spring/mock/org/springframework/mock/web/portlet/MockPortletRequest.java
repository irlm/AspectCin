/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.mock.web.portlet;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Mock implementation of the {@link javax.portlet.PortletRequest} interface.
 *
 * @author John A. Lewis
 * @author Juergen Hoeller
 * @since 2.0
 */
public class MockPortletRequest implements PortletRequest {

	private boolean active = true;

	private final PortalContext portalContext;

	private final PortletContext portletContext;

	private PortletSession session = null;

	private WindowState windowState = WindowState.NORMAL;

	private PortletMode portletMode = PortletMode.VIEW;

	private PortletPreferences portletPreferences = new MockPortletPreferences();

	private final Map properties = CollectionFactory.createLinkedMapIfPossible(16);

	private final Hashtable attributes = new Hashtable();

	private final Map parameters = CollectionFactory.createLinkedMapIfPossible(16);

	private String authType = null;

	private String contextPath = "";

	private String remoteUser = null;

	private Principal userPrincipal = null;

	private final Set userRoles = new HashSet();

	private boolean secure = false;

	private boolean requestedSessionIdValid = true;

	private final Vector responseContentTypes = new Vector();

	private final Vector locales = new Vector();

	private String scheme = "http";

	private String serverName = "localhost";

	private int serverPort = 80;


	/**
	 * Create a new MockPortletRequest with a default {@link MockPortalContext}
	 * and a default {@link MockPortletContext}.
	 * @see MockPortalContext
	 * @see MockPortletContext
	 */
	public MockPortletRequest() {
		this(null, null);
	}

	/**
	 * Create a new MockPortletRequest with a default {@link MockPortalContext}.
	 * @param portletContext the PortletContext that the request runs in
	 * @see MockPortalContext
	 */
	public MockPortletRequest(PortletContext portletContext) {
		this(null, portletContext);
	}

	/**
	 * Create a new MockPortletRequest.
	 * @param portalContext the PortalContext that the request runs in
	 * @param portletContext the PortletContext that the request runs in
	 */
	public MockPortletRequest(PortalContext portalContext, PortletContext portletContext) {
		this.portalContext = (portalContext != null ? portalContext : new MockPortalContext());
		this.portletContext = (portletContext != null ? portletContext : new MockPortletContext());
		this.responseContentTypes.add("text/html");
		this.locales.add(Locale.ENGLISH);
	}


	//---------------------------------------------------------------------
	// Lifecycle methods
	//---------------------------------------------------------------------

	/**
	 * Return whether this request is still active (that is, not completed yet).
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Mark this request as completed.
	 */
	public void close() {
		this.active = false;
	}

	/**
	 * Check whether this request is still active (that is, not completed yet),
	 * throwing an IllegalStateException if not active anymore.
	 */
	protected void checkActive() throws IllegalStateException {
		if (!this.active) {
			throw new IllegalStateException("Request is not active anymore");
		}
	}


	//---------------------------------------------------------------------
	// PortletRequest methods
	//---------------------------------------------------------------------

	public boolean isWindowStateAllowed(WindowState windowState) {
		return CollectionUtils.contains(this.portalContext.getSupportedWindowStates(), windowState);
	}

	public boolean isPortletModeAllowed(PortletMode portletMode) {
		return CollectionUtils.contains(this.portalContext.getSupportedPortletModes(), portletMode);
	}

	public void setPortletMode(PortletMode portletMode) {
		this.portletMode = portletMode;
	}

	public PortletMode getPortletMode() {
		return this.portletMode;
	}

	public void setWindowState(WindowState windowState) {
		this.windowState = windowState;
	}

	public WindowState getWindowState() {
		return this.windowState;
	}

	public void setPreferences(PortletPreferences preferences) {
		this.portletPreferences = preferences;
	}

	public PortletPreferences getPreferences() {
		return this.portletPreferences;
	}

	public void setSession(PortletSession session) {
		this.session = session;
		if (session instanceof MockPortletSession) {
			MockPortletSession mockSession = ((MockPortletSession) session);
			mockSession.access();
		}
	}

	public PortletSession getPortletSession() {
		return getPortletSession(true);
	}

	public PortletSession getPortletSession(boolean create) {
		checkActive();
		// Reset session if invalidated.
		if (this.session instanceof MockPortletSession && ((MockPortletSession) this.session).isInvalid()) {
			this.session = null;
		}
		// Create new session if necessary.
		if (this.session == null && create) {
			this.session = new MockPortletSession(this.portletContext);
		}
		return this.session;
	}

	/**
	 * Set a single value for the specified property.
	 * <p>If there are already one or more values registered for the given
	 * property key, they will be replaced.
	 */
	public void setProperty(String key, String value) {
		Assert.notNull(key, "Property key must not be null");
		List list = new LinkedList();
		list.add(value);
		this.properties.put(key, list);
	}

	/**
	 * Add a single value for the specified property.
	 * <p>If there are already one or more values registered for the given
	 * property key, the given value will be added to the end of the list.
	 */
	public void addProperty(String key, String value) {
		Assert.notNull(key, "Property key must not be null");
		List oldList = (List) this.properties.get(key);
		if (oldList != null) {
			oldList.add(value);
		}
		else {
			List list = new LinkedList();
			list.add(value);
			this.properties.put(key, list);
		}
	}

	public String getProperty(String key) {
		Assert.notNull(key, "Property key must not be null");
		List list = (List) this.properties.get(key);
		return (list != null && list.size() > 0 ? (String) list.get(0) : null);
	}

	public Enumeration getProperties(String key) {
		Assert.notNull(key, "property key must not be null");
		return Collections.enumeration((List) this.properties.get(key));
	}

	public Enumeration getPropertyNames() {
		return Collections.enumeration(this.properties.keySet());
	}

	public PortalContext getPortalContext() {
		return this.portalContext;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthType() {
		return this.authType;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return this.contextPath;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getRemoteUser() {
		return this.remoteUser;
	}

	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	public Principal getUserPrincipal() {
		return this.userPrincipal;
	}

	public void addUserRole(String role) {
		this.userRoles.add(role);
	}

	public boolean isUserInRole(String role) {
		return this.userRoles.contains(role);
	}

	public Object getAttribute(String name) {
		checkActive();
		return this.attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		checkActive();
		return this.attributes.keys();
	}

	public void setParameters(Map parameters) {
		Assert.notNull(parameters, "Parameters Map must not be null");
		this.parameters.clear();
		for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Assert.isTrue(entry.getKey() instanceof String, "Key must be of type String");
			Assert.isTrue(entry.getValue() instanceof String[], "Value must be of type String[]");
			this.parameters.put(entry.getKey(), entry.getValue());
		}
	}

	public void setParameter(String key, String value) {
		Assert.notNull(key, "Parameter key must be null");
		Assert.notNull(value, "Parameter value must not be null");
		this.parameters.put(key, new String[] {value});
	}

	public void setParameter(String key, String[] values) {
		Assert.notNull(key, "Parameter key must be null");
		Assert.notNull(values, "Parameter values must not be null");
		this.parameters.put(key, values);
	}

	public void addParameter(String name, String value) {
		addParameter(name, new String[] {value});
	}

	public void addParameter(String name, String[] values) {
		String[] oldArr = (String[]) this.parameters.get(name);
		if (oldArr != null) {
			String[] newArr = new String[oldArr.length + values.length];
			System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
			System.arraycopy(values, 0, newArr, oldArr.length, values.length);
			this.parameters.put(name, newArr);
		}
		else {
			this.parameters.put(name, values);
		}
	}

	public String getParameter(String name) {
		String[] arr = (String[]) this.parameters.get(name);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	public Enumeration getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	public String[] getParameterValues(String name) {
		return (String[]) this.parameters.get(name);
	}

	public Map getParameterMap() {
		return Collections.unmodifiableMap(this.parameters);
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isSecure() {
		return this.secure;
	}

	public void setAttribute(String name, Object value) {
		checkActive();
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	public void removeAttribute(String name) {
		checkActive();
		this.attributes.remove(name);
	}

	public String getRequestedSessionId() {
		PortletSession session = this.getPortletSession();
		return (session != null ? session.getId() : null);
	}

	public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
		this.requestedSessionIdValid = requestedSessionIdValid;
	}

	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionIdValid;
	}

	public void addResponseContentType(String responseContentType) {
		this.responseContentTypes.add(responseContentType);
	}

	public void addPreferredResponseContentType(String responseContentType) {
		this.responseContentTypes.add(0, responseContentType);
	}

	public String getResponseContentType() {
		return (String) this.responseContentTypes.get(0);
	}

	public Enumeration getResponseContentTypes() {
		return this.responseContentTypes.elements();
	}

	public void addLocale(Locale locale) {
		this.locales.add(locale);
	}

	public void addPreferredLocale(Locale locale) {
		this.locales.add(0, locale);
	}

	public Locale getLocale() {
		return (Locale) this.locales.get(0);
	}

	public Enumeration getLocales() {
		return this.locales.elements();
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getScheme() {
		return scheme;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}

}
