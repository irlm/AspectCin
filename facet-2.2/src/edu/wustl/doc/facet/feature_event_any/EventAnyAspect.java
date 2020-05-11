package edu.wustl.doc.facet.feature_event_any;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

privileged aspect EventAnyAspect {

	/* Add fields and methods to the EventCarrier to be able to
	 * pass CORBA anys
	 */
	private org.omg.CORBA.Any EventCarrier.event_;
    
	public EventCarrier.new (org.omg.CORBA.Any event)
	{
		event_ = event;
	}
		
	/* Add push method to the ProxyPushConsumer */
	public void ProxyPushConsumerImpl.push (org.omg.CORBA.Any event)
	{
		EventCarrier ec = new EventCarrier(event);
		this.eventChannel_.pushEvent(ec);
	}

	/* Add event parameter in push dispatch method */
	pointcut getsEventCarrier(EventCarrier ec) :
		cflow (execution(void ProxyPushSupplierImpl.push (EventCarrier)) && args (ec));

	void around (PushConsumer pc, EventCarrier ec) :
		call (void PushConsumer.push ()) && target (pc) && getsEventCarrier(ec)
	{
		pc.push (ec.event_);
	}

	/* Add push() method to PushConsumer to
	 * make code compilable. **BLEMISH**
	 */
	public void PushConsumer.push()
	{
		System.out.println("WARNING: PushConsumer.push() not available in this configuration!");
	}
}
