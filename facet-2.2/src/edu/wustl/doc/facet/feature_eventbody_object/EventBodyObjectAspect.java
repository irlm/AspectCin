package edu.wustl.doc.facet.feature_eventbody_object;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

privileged aspect EventBodyObjectAspect {

	public Object Event.payload;

        //
        // Accessors for this field
        //
        public Object Event.getPayload ()
        {
                return this.payload;
        }

        public void Event.setPayload (Object data)
        {
                this.payload = data;
        }

}
