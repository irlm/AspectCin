package edu.wustl.doc.facet.feature_eventheader;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect EventHeaderAspect {

        //
        // Add accessors to the appropriate classes
        //
	public EventHeader Event.getHeader ()
        {
                return this.header;
        }

        public void Event.setHeader (EventHeader h)
        {
                this.header = h;
        }
}
