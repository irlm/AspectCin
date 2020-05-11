package glassbox.track;

import glassbox.track.api.DefaultFailureDetectionStrategy;

import java.io.IOException;
import java.rmi.RemoteException;


public class ServletFailureDetectionStrategy extends DefaultFailureDetectionStrategy {

    public int getSeverity(Throwable t) {
        if (t instanceof IOException || t instanceof NumberFormatException) {
            // servlet IO exceptions can be normal, e.g., caused by clients that break the request
            // likewise Tomcat generates spurious NumberFormatExceptions when parsing options on init...
            return WARNING;
        } else if ("weblogic.servlet.jsp.AddToMapException".equals(t.getClass().getName())) {
            // Our first "expected" exception. Weblogic 9.x throws these as a normal part of running
            // This should be refactored so normal exceptions are described by component not globally!            
            return NORMAL;
        }

        return super.getSeverity(t);
    }

}
