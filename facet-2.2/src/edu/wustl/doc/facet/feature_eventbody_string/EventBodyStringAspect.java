package edu.wustl.doc.facet.feature_eventbody_string;

import edu.wustl.doc.facet.EventComm.*;

aspect EventBodyStringAspect {

        //
        // Accessors for the payload
        //
        public String Event.getPayload ()
        {
                return this.payload;
        }

        public void Event.setPayload (String data)
        {
                this.payload = data;
        }
}
