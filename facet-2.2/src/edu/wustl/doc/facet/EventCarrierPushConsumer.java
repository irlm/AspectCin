/*
 * $Id: EventCarrierPushConsumer.java,v 1.3 2003/07/11 23:02:08 ravip Exp $
 */

package edu.wustl.doc.facet;

/**
 * Internal interface for entities within the event channel
 * that can pass EventCarrier objects.
 *
 * @author Frank Hunleth
 * @version $Revision: 1.3 $
 */
public interface EventCarrierPushConsumer {
        
        /**
         * Event channel internal method used to push an event to the
         * consumers. (Public so that features can access this method.)
         *
         * @param eventCarrier      The event wrapped in an EventCarrier
         */
        void push (EventCarrier eventCarrier);
}
