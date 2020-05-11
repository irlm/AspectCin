package edu.wustl.doc.facet.feature_eventtype;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect EventTypeAspect {

	//
        // Add appropriate accessors
        //

        public int EventHeader.getType ()
        {
                return this.type;
        }

        public void EventHeader.setType (int t)
        {
                this.type = t;
        }
}
