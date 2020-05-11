package glassbox.client.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public aspect ErrorHandling {

    private static final Log log = LogFactory.getLog(ErrorHandling.class);
    
    after() throwing (Throwable t) : execution(public * Controller+.*(..)) && within(glassbox.client..*) {
        log.error("Failure when executing controller request", t);
    }
    
}
