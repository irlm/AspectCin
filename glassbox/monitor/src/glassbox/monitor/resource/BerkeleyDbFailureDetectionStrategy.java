package glassbox.monitor.resource;

import glassbox.track.api.*;
import glassbox.monitor.resource.BerkeleyDbMonitor.IDatabaseException;

public class BerkeleyDbFailureDetectionStrategy extends DefaultFailureDetectionStrategy {

    public int getSeverity(Throwable t) {
        if (t instanceof IDatabaseException) {
            IDatabaseException dbe = (IDatabaseException)t;
            if (dbe.getErrno() <0) {
                // <0 is a warning per www.oracle.com/technology/products/berkeley-db/faq/db_faq.html+Berkeley+DB+error+number 
                return WARNING;
            }
        }
        return super.getSeverity(t);
    }
    
    public FailureDescription getFailureDescription(Throwable t) {
        FailureDescriptionImpl description = (FailureDescriptionImpl)super.getFailureDescription(t);
        if (t instanceof IDatabaseException) {            
            //unpack the root exception, so we don't lose error codes etc. in translation
            IDatabaseException dbe = (IDatabaseException)t;
            for (;;) {
                Throwable next = causeStrategy.getCause((Throwable)dbe);
                if (next instanceof IDatabaseException) {
                    dbe = (IDatabaseException)next;
                } else {
                    break;
                }
            }
            
            description.summary = trim("a data access problem: "+t.getClass().getName()+", error code ["+dbe.getErrno()+"]: "+message(t).trim());
            
            return description;
        }
        return null;
    }
}

