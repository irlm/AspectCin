/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.client.persistence.jdbc.ConfigurationDAO;
import glassbox.client.persistence.jdbc.ConnectionDAO;
import glassbox.client.pojo.ConnectionData;
import glassbox.common.BaseTestCase;

import java.util.Iterator;
import java.util.List;

public class ConnectionPersistenceTest extends BaseTestCase {

    public void setUp() throws Exception {
        super.setUp();
        ConfigurationDAO configurationDAO = (ConfigurationDAO) getContext().getBean("configurationDAO");
        assertNotNull(configurationDAO);
        configurationDAO.configure();
    }

    public void doTearDown() throws Exception {
    }

    public ConnectionPersistenceTest(String arg0) {
        super(arg0);
    }

    public void testConfig() {

        System.out.println("Testing Creating Connections");
        assertNotNull(getContext());

        ConnectionDAO connectionDAO = (ConnectionDAO) getContext().getBean("connectionDAO");
        assertNotNull(connectionDAO);

    }

    public void testCRUD() {

        assertNotNull(getContext());

        ConnectionDAO connectionDAO = (ConnectionDAO) getContext().getBean("connectionDAO");
        assertNotNull(connectionDAO);

        List connections = connectionDAO.getAll();
        int orig = connections.size();

        ConnectionData data = new ConnectionData();
        data.setName("Test Connection");
        data.setHostName("localhost");
        data.setPort("0");
        data.setProtocol("local");
        data.setUrl("local:glassbox");

        connections = connectionDAO.getAll();
        connectionDAO.add(data);

        connections = connectionDAO.getAll();

        assertTrue(connections.size() > 0);

        ConnectionData connectionData = (ConnectionData) connections.get(0);
        assertNotNull(connectionData);

        Iterator connectionsIt = connections.iterator();

        while (connectionsIt.hasNext()) {
            ConnectionData connectionItData = (ConnectionData) connectionsIt.next();
            assertNotNull(connectionItData);
            assertNotNull(connectionItData.getId());
            assertNotNull(connectionItData.getName());
            assertNotNull(connectionItData.getUrl());
        }

        connectionDAO.delete(data);

        assertEquals(orig, connectionDAO.getAll().size());
    }

}
