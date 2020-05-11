package edu.wustl.doc.facet.feature_ttl;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect TtlAspect {

	//
	// Update the time to live for the event.
	// If the TTL is still ok, return true.
	//
	boolean update_ttl (Event event)
	{
		event.getHeader().setTtl (event.getHeader ().getTtl () - 1);
		return event.getHeader().getTtl () >= 0;
	}

	//
        // Update the TTL, and possibly drop the event if it gets too low
        //
	void around (EventCarrier ec) :
		call (void EventChannelImpl.pushEvent (EventCarrier))
		&& args (ec)
	{
		if (update_ttl (ec.getEvent ()))
			proceed (ec);
	}

        //
        // Add appropriate accessors
        //
        public long EventHeader.getTtl ()
        {
                return this.ttl;
        }

        public void EventHeader.setTtl (long l)
        {
                this.ttl = l;
        }

}
