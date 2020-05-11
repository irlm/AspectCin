package edu.wustl.doc.facet.feature_eventbody_any;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;
import org.omg.CORBA.*;

aspect EventBodyAnyAspect {

	//
        // Accessors for the payload
        //

        public Any Event.getPayload ()
        {
                return this.payload;
        }

        public void Event.setPayload (Any data)
        {
                this.payload = data;
        }
}
