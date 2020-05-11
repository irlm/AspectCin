package edu.wustl.doc.facet.feature_eventbody_octetseq;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

aspect EventBodyOctetSeqAspect {

        //
        // Accessors for the payload
        //

        public byte [] Event.getPayload ()
        {
                return this.payload;
        }

        public void Event.setPayload (byte[] data)
        {
                this.payload = data;
        }
}
