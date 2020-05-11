/*
 * $Id: EventChannelTraceAspect.java,v 1.5 2003/06/19 17:39:18 ravip Exp $
 */

package edu.wustl.doc.facet.feature_tracing;

import edu.wustl.doc.facet.*;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.aspectj.lang.Signature;

/**
 * Enabling this aspect weaves in tracing code throughout the event
 * channel.
 *
 * The tracing is done using the Log4J logger.  A good pattern
 * for printing out the trace messages is "%-4r [%t]%x%m%n"
 */
aspect EventChannelTraceAspect {

        /**
         * Get the logging category
         */
        protected static Logger log = Logger.getLogger ("edu.wustl.doc.facet");

        /**
         * Application classes.
         */
        pointcut myClasses() :
                within (edu.wustl.doc.facet..*) &&
                !within (edu.wustl.doc.facet.EventChannelAdmin.*) &&
                !within (edu.wustl.doc.facet.EventComm.*) &&
                !within (*Test*) &&
                !within (edu.wustl.doc.facet.Upgradeable) &&
                !within (EventChannelTraceAspect);

        /**
         * The constructors in those classes.
         */
        pointcut myConstructors (): myClasses() && execution(new (..));
	
        /**
         * The methods of those classes.
         */
        pointcut myMethods (): myClasses () && execution (* *(..));

        protected void enterMethod (Signature signature)
        {
                /* Update the indentation for tracing in this thread. */
                NDC.push (" ");
                
                /* Log the message */
                log.debug("Entering " + signature);
        }
        
        protected void exitMethod (Signature signature)
        {
                log.debug("Exiting " + signature);
                NDC.pop ();
        }
        
        /**
         * Prints trace messages before and after executing constructors.
         */
        before (): myConstructors ()
         {
                 enterMethod (thisJoinPointStaticPart.getSignature ());
         }
        
        after(): myConstructors ()
        {
                exitMethod (thisJoinPointStaticPart.getSignature ());
        }
        
        /**
         * Prints trace messages before and after executing methods.
         */
        before (): myMethods ()
        {
                enterMethod (thisJoinPointStaticPart.getSignature());
        }

        after(): myMethods ()
        {
                exitMethod (thisJoinPointStaticPart.getSignature());
        }
}
