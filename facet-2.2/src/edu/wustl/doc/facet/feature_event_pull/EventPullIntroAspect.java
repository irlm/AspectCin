package edu.wustl.doc.facet.feature_event_pull;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect EventPullIntroAspect {

	public abstract ProxyPullSupplier ConsumerAdmin.obtain_pull_supplier ();
	public abstract ProxyPullConsumer SupplierAdmin.obtain_pull_consumer ();

	//
	// The pull operation blocks until the event data is available or
	// an exception is raised. It returns the event data to the
	// consumer.  If the event communication has already been
	// disconnected, the EventChannelException exception is raised.
	//	
	public abstract Event PullSupplier.pull ();

	//
	// The try_pull operation does not block: if the event data is
	// available, it returns the event data and sets the has_event
	// parameter to true; if the event is not available, it sets the
	// has_event parameter to false and the event data is returned as
	// long with an undefined value.  If the event communication has
	// already been disconnected, the EventChannelException exception is
	// raised.
	//
	public Event PullSupplier.try_pull (BooleanHolder has_event)
        {
                return null;
        } 

	//
	// The disconnect_pull_supplier operation terminates the event
	// communication; it releases resources used at the supplier to
	// support the event communication.  The PullSupplier object
	// reference is disposed.
	//
	public abstract void PullSupplier.disconnect_pull_supplier ();

	public abstract void PullConsumer.disconnect_pull_consumer ();

	public abstract void ProxyPullConsumer.connect_pull_supplier (PullSupplier pull_supplier);
	public abstract void ProxyPullSupplier.connect_pull_consumer (PullConsumer pull_consumer);
}
