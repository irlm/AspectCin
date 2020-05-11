package edu.wustl.doc.facet.feature_event_struct;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

public aspect EventStructAspect {

	//
	// Add fields and methods to the EventCarrier to be able to
	// pass around the basic Event structure.
	//
	private Event EventCarrier.event_;

	public EventCarrier.new (Event event)
	{
		event_ = event;
	}

	public Event EventCarrier.getEvent ()
	{
		return this.event_;
	}
	
	//
        // Override the push (Event) method to do the right thing
        //
	public void ProxyPushConsumerImpl.push (Event event)
	{
		EventCarrier ec = new EventCarrier (event);
		this.getEventChannel ().pushEvent (ec);
	}

        void around (ProxyPushSupplierImpl pps, EventCarrier ec) :
                execution (void ProxyPushSupplierImpl.push (EventCarrier))
                && target (pps)
                && args (ec)
        {
                pps.getPushConsumer ().push (ec.event_);
        }

	//
        // Add push() method to PushConsumer to * make code
        // compilable. This is necessary because IDL does not allow
        // overloaded methods
	//
	public void PushConsumer.push ()
	{
		throw new Error ("PushConsumer.push () not available in this configuration!");
	}
}
