/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
public class MockHostConfiguration extends HostConfiguration {

    protected String HOST = "localhost";
    protected Protocol PROTOCOL = new Protocol("http", new DefaultProtocolSocketFactory(), 443);
    
        public String getHost() {
            return HOST;
        }
        
        public Protocol getProtocol() {
            return PROTOCOL;
        }
}
