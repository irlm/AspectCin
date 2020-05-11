package edu.wustl.doc.facet.feature_event_object;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

privileged aspect EventObjectAspect {
	/*
	 * Introductions from CORBA IDL.
	 */
	public abstract void PushConsumer.push (Object data);

	/* Add fields and methods to the EventCarrier to be able to
	 * pass Java Objects.
	 */
	private Object EventCarrier.event_;

	public EventCarrier.new (Object event)
	{
		event_ = event;
	}
		
	/* Add push method to the ProxyPushConsumer */
	public void ProxyPushConsumerImpl.push(Object event)
	{
		EventCarrier ec = new EventCarrier(event);
		this.eventChannel_.pushEvent(ec);
	}

	/* Add event parameter in push dispatch method */
	pointcut getsEventCarrier(EventCarrier ec) :
		cflow (execution (void ProxyPushSupplierImpl.push (EventCarrier))
		       && args(ec));
	
	void around (PushConsumer pc, EventCarrier ec) :
		call(void PushConsumer.push ())
		&& target (pc)
		&& getsEventCarrier (ec)
	{
		pc.push(ec.event_);
	}

	/* Add push() method to PushConsumer to
	 * make code compilable. **BLEMISH**
	 */
	public void PushConsumer.push()
	{	
		System.out.println("WARNING: PushConsumer.push() not available in this configuration!");
	}
}
