package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

privileged aspect RtecCorrelationFilterAspect {

        //
        // This domination advice is necessary since we all wrap around the
        // same join point
        //
        declare precedence: RtecCorrelationFilterAspect,
                (edu.wustl.doc.facet.feature_event_struct.EventStructAspect ||
                 edu.wustl.doc.facet.feature_consumer_dispatch.ConsumerDispatchAspect ||
                 edu.wustl.doc.facet.feature_source_filter.SourceFilterAspect);
        
 	//
	// Accessors on Dependency
	//
	public int Dependency.getFilterOp ()
	{
		return this.filter_op;
	}

        public void Dependency.setFilterOp (int i)
        {
                this.filter_op = i;
        }
	
	private FilterBuilderRegistry builderRegistry_ = new FilterBuilderRegistry();

	public FilterRunner ProxyPushSupplierImpl.filterRunner_;
	public EventCarrierList ProxyPushSupplierImpl.savedEvents_ = new EventCarrierList();

	after (ProxyPushSupplierImpl pps, ConsumerQOS qos) returning () :
		execution (void ProxyPushSupplierImpl.connect_push_consumer (PushConsumer, ConsumerQOS))
		&& args (PushConsumer, qos)
		&& target (pps)
	{

		// Build up filter internal structure.
		FilterTreeBuilder ftb = new FilterTreeBuilder (builderRegistry_, qos.getDependencies ());

		try {

                   
                        FilterNode root = ftb.buildTree();

			//
			// Make sure that we went through all of the dependencies or
			// there is something wrong with the tree structure.
			//
			if (!ftb.checkDone()) 
				throw new EventChannelException ();
		
			// Create the new filter runner.
			pps.filterRunner_ = new FilterRunner (root);

		} catch (EventChannelException e) {
			// Need to catch exception since it is checked and AspectJ
			// doesn't generate code that has the right signature.
			e.printStackTrace();
			pps.filterRunner_ = null;
		}
	}


	void around (ProxyPushSupplierImpl pps, EventCarrier eventCarrier) :
            execution (void ProxyPushSupplierImpl.push (EventCarrier))
            && target (pps) && args (eventCarrier)
        {
                if (pps.filterRunner_ == null) {
                        proceed (pps, eventCarrier);
                        return;
                }

		if (pps.filterRunner_.pushEvent (eventCarrier, pps.savedEvents_)) {

			Event[] events = pps.savedEvents_.createEventArray ();
			if (events.length == 1) {
                                pps.getPushConsumer().push (events[0]);
			} else {
				pps.getPushConsumer().push_vec (events);
			}

			// Clear the list for next time.
			pps.savedEvents_.clear();
		}
	}
}
