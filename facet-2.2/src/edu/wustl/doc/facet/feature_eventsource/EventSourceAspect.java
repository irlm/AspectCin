package edu.wustl.doc.facet.feature_eventsource;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect EventSourceAspect {

        //
        // Add appropriate accessors
        //

        public int EventHeader.getSource ()
        {
                return this.source_id;
        }

        public void EventHeader.setSource (int src)
        {
                this.source_id = src;
        }
}
