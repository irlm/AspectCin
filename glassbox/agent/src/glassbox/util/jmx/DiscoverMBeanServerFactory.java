/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.jmx;

import java.util.ArrayList;

import javax.management.*;

public class DiscoverMBeanServerFactory {

    private String preferred = "";
    private boolean newOnly = false;
    
    public MBeanServer getServer() {
        if (!newOnly) {
            MBeanServer existing = findExisting();
            if (existing != null) {
                return existing;
            }
        }
        
        try {
            return MBeanServerFactory.newMBeanServer(getNewName());
        } catch (JMRuntimeException incompatibleJmxImplException) {
            // limitation: we can't delegate to the container's JMX implementation
            // although we might fix this by eager loading or by plugging in a special delegating loader
            // this bites us on JBoss for Java 1.4..
            String builderKey = "javax.management.builder.initial";
            String originalBuilder = System.getProperty(builderKey);
            try {
                // use mx4j for now
                System.setProperty(builderKey, "mx4j.server.MX4JMBeanServerBuilder");
                return MBeanServerFactory.newMBeanServer(getNewName());
            } finally {
                System.setProperty(builderKey, originalBuilder);
            }
        }
    }
    
    private MBeanServer findExisting() {
        MBeanServer server = null;
        if (!noPreferred()) {
            server = findServer(preferred);
        }
        if (server == null) {
            server = findServer(null);
        }
        return server;
    }
    
    private MBeanServer findServer(String id) {
        ArrayList jmxServerList=MBeanServerFactory.findMBeanServer(id);
        if (jmxServerList.size() > 0) {
            return (MBeanServer)jmxServerList.get(0);
        }
        return null; // none found
    }
    
    private boolean noPreferred() {
        return "".equals(preferred) || preferred == null;
    }
    
    private String getNewName() {
        return noPreferred() ? "glassbox" : preferred;
    }

    /**
     * @return the newOnly
     */
    public boolean isNewOnly() {
        return newOnly;
    }

    /**
     * @param newOnly the newOnly to set
     */
    public void setNewOnly(boolean newOnly) {
        this.newOnly = newOnly;
    }

    /**
     * @return the preferred
     */
    public String getPreferred() {
        return preferred;
    }

    /**
     * @param preferred the preferred to set
     */
    public void setPreferred(String preferred) {
        this.preferred = preferred;
    }
}
;