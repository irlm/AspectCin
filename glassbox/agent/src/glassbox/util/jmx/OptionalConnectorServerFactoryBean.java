/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.jmx;

import glassbox.config.OptionalBean;

import java.io.IOException;

import javax.management.JMException;

import org.springframework.jmx.support.ConnectorServerFactoryBean;

public class OptionalConnectorServerFactoryBean extends ConnectorServerFactoryBean implements OptionalBean {

    public void onError(Exception e) {
        logWarn("Unable to set up remote JMX connector. This is often caused by having another server trying to use the same ports. Root cause:", e);
    }
    
    // boilerplate code to provide a hook for advising
    public void afterPropertiesSet() throws JMException, IOException {
        super.afterPropertiesSet();
    }

    // boilerplate code to provide a hook for advising
    public void destroy() throws IOException {
        super.destroy();
    }

}
