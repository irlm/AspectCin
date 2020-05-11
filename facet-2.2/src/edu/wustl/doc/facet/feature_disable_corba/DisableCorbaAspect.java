package edu.wustl.doc.facet.feature_disable_corba;

import edu.wustl.doc.facet.*;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;	

/*
 * DisableCorbaAspect.java : This aspect removes CORBA throughout FACET
 * so that you get the non-CORBA case with pure Java objects and nothing
 * to do with the ORB
 */

privileged aspect DisableCorbaAspect {

	declare parents : ConsumerAdminBase implements ConsumerAdmin;
	declare parents : SupplierAdminBase implements SupplierAdmin;
	declare parents : PushConsumerBase  implements PushConsumer;
	declare parents : PullSupplierBase  implements PullSupplier;
	declare parents : EventChannelBase  implements EventChannel;
	
	declare parents : ProxyPushSupplierBase implements ProxyPushSupplier;
	declare parents : ProxyPushConsumerBase implements ProxyPushConsumer;
	declare parents : ProxyPullConsumerBase implements ProxyPullConsumer;
	declare parents : ProxyPullSupplierBase implements ProxyPullSupplier;

	ConsumerAdmin around (Object poa, ConsumerAdminBase impl) :
		execution (ConsumerAdmin
                           EventChannelImpl.CorbaGetConsumerAdminRef (Object, ConsumerAdminBase))
		&& args (poa, impl)
	{
		return (ConsumerAdmin) impl; 
	}

	SupplierAdmin around (Object poa, SupplierAdminBase impl) :
		execution (SupplierAdmin
                           EventChannelImpl.CorbaGetSupplierAdminRef (Object, SupplierAdminBase))
		&& args (poa, impl)
	{
		return (SupplierAdmin) impl;
	}

	ProxyPushSupplier around (Object poa, ProxyPushSupplierBase impl) :
		execution (ProxyPushSupplier
                           ConsumerAdminImpl.CorbaGetProxyPushSupplierRef (Object, ProxyPushSupplierBase))
		&& args (poa, impl)
        {
		return (ProxyPushSupplier) impl;
	}

	ProxyPullSupplier around (Object poa, ProxyPullSupplierBase impl) throws Throwable :
		execution (ProxyPullSupplier
                           ConsumerAdminImpl.CorbaGetProxyPullSupplierRef (Object, ProxyPullSupplierBase))
		&& args (poa, impl)
	{
		return (ProxyPullSupplier) impl;
	}
		

	ProxyPushConsumer around (Object poa, ProxyPushConsumerBase impl) :
		execution (ProxyPushConsumer
                           SupplierAdminImpl.CorbaGetProxyPushConsumerRef (Object, ProxyPushConsumerBase))
		&& args (poa, impl)
	{
	        return (ProxyPushConsumer) impl;
	}

	ProxyPullConsumer around (Object poa, ProxyPullConsumerBase impl) throws Throwable :
		execution (ProxyPullConsumer
                           SupplierAdminImpl.CorbaGetProxyPullConsumerRef (Object, ProxyPullConsumerBase))
		&& args (poa, impl)
	{
		return (ProxyPullConsumer) impl;
	}
}

