/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.Semaphore;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Condition;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;
import glassbox.agent.control.api.GlassboxService;
import glassbox.client.pojo.ConnectionData;
import glassbox.config.GlassboxInitializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalAgentClient extends AbstractAgentClientProvider implements AgentClientProvider {

    private static final Log log = LogFactory.getLog(LocalAgentClient.class);
    
    static CountDownLatch initializeLatch = new CountDownLatch(1);

    private static AgentConnectionException agentConnectionException;
    private static GlassboxService conn;
    
    public LocalAgentClient(ConnectionData connectionData) {
        super(connectionData);
    }

    protected void close() {
        serviceBean = null;
    }

    protected void connect() throws AgentConnectionException {
        if (conn == null) {
            initialize();
        }
        if (conn == null) {
            throw new AgentConnectionException("Can't initialize Glassbox");
        }
        serviceBean = conn;
    }

    public static void initialize() throws AgentConnectionException {
    	try {
            try {
                Object res = GlassboxInitializer.start(false);
                if (res!=null && !(res instanceof GlassboxService)) {
                    log.error("Invalid definition probably due to classloader issue from improper installation");
                    log.error("Class "+res.getClass()+" is not an instance of "+GlassboxService.class);
                    log.error("Loaders are "+res.getClass().getClassLoader()+", "+GlassboxService.class.getClassLoader());
                    agentConnectionException = new AgentConnectionException("Can't initialize glassbox: bad types");
    				throw agentConnectionException;
                } else {
                    conn = (GlassboxService)res;
                }
            } catch (Exception e) {
                log.debug("Failure to initialize Glassbox", e);
                throw new AgentConnectionException("Can't initialize Glassbox", e);
            } catch (NoClassDefFoundError err) {
                throw new AgentConnectionException("Can't initialize Glassbox", err);
            }    		
    	} finally {
    		initializeLatch.countDown();
    	}
    }        

    public static void shutdown() {
        GlassboxInitializer.stop();
    }
    
    public static GlassboxService getInitializedService() throws InterruptedException, AgentConnectionException {
        initializeLatch.await(15, TimeUnit.SECONDS);
        
        if (conn != null) 
            return conn;
        
        if (agentConnectionException != null)
            throw agentConnectionException;
        
        
        throw new AgentConnectionException("Ran out of time initializing.");
    }

}
