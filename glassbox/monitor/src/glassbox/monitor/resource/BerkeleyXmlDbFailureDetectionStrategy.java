package glassbox.monitor.resource;

import glassbox.track.api.*;

import com.sleepycat.dbxml.XmlException;

public class BerkeleyXmlDbFailureDetectionStrategy extends DefaultFailureDetectionStrategy {

    public int getSeverity(Throwable t) {
        if (t instanceof XmlException) {
            XmlException xe = (XmlException)t;
            switch (xe.getErrorCode()) {
                case XmlException.DOCUMENT_NOT_FOUND:
                case XmlException.INVALID_VALUE:
                case XmlException.UNIQUE_ERROR:
                    return WARNING;
            }
        }
        return super.getSeverity(t);
    }
    
    public FailureDescription getFailureDescription(Throwable t) {
        FailureDescriptionImpl description = (FailureDescriptionImpl)super.getFailureDescription(t);
        if (t instanceof XmlException) {            
            //unpack the root exception, so we don't lose error codes etc. in translation
            XmlException xe = (XmlException)t;
            for (;;) {
                Throwable next = causeStrategy.getCause(xe);
                if (next instanceof XmlException) {
                    xe = (XmlException)next;
                } else {
                    break;
                }
            }
            
            String extra="";
            if (xe.getErrorCode() == XmlException.DATABASE_ERROR) {
                extra="], database error code = "+xe.getDbError();
            }
            
            description.summary = trim("a data access problem: "+t.getClass().getName()+", exception code ["+xe.getErrorCode()+extra+"]: "+message(t).trim());
            
            return description;
        }
        return null;
    }
}

