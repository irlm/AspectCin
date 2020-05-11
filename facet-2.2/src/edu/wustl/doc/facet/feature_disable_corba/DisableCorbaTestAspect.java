package edu.wustl.doc.facet.feature_disable_corba;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventComm.*;


/**
 * Contains code relevant only to the unit tests so that we can
 * exclude this when we are building a very light library with no unit
 * tests
 */
aspect DisableCorbaTestAspect {

        declare parents : EventChannelTestCaseBase extends EventChannelTestCase;

        PushConsumer around (Object poa, PushConsumerBase consumerImpl) :
		execution (PushConsumer
                           EventChannelTestCaseBase.CorbaGetPushConsumerRef (Object, PushConsumerBase))
		&& args (poa, consumerImpl)
	{
		return (PushConsumer) consumerImpl;
	}
	
	PullSupplier around (Object poa, PullSupplierBase supplierImpl) throws Throwable :
		execution (PullSupplier
                           EventChannelTestCaseBase.CorbaGetPullSupplierRef (Object, PullSupplierBase))
		&& args (poa, supplierImpl)
	{
		return (PullSupplier) supplierImpl;
	}
}
