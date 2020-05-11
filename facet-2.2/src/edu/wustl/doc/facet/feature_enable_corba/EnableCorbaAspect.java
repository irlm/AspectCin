//
// Enables CORBA :
// Sets up the *Base classes to point to the POA classes and other CORBA related magic
//

package edu.wustl.doc.facet.feature_enable_corba;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
	
public aspect EnableCorbaAspect {

	declare parents : EventChannelBase  extends EventChannelPOA;
	declare parents : PushConsumerBase  extends PushConsumerPOA;
	declare parents : PullSupplierBase  extends PullSupplierPOA;
	declare parents : SupplierAdminBase extends SupplierAdminPOA;
	declare parents : ConsumerAdminBase extends ConsumerAdminPOA;

	declare parents : ProxyPushConsumerBase extends ProxyPushConsumerPOA;
	declare parents : ProxyPushSupplierBase extends ProxyPushSupplierPOA;
	declare parents : ProxyPullConsumerBase extends ProxyPullConsumerPOA;
	declare parents : ProxyPullSupplierBase extends ProxyPullSupplierPOA;

	ConsumerAdmin around (Object poa, ConsumerAdminBase impl) throws Throwable :
		execution (ConsumerAdmin
                           EventChannelImpl.CorbaGetConsumerAdminRef (Object, ConsumerAdminBase))
		&& args (poa, impl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		return ConsumerAdminHelper.narrow (obj);
	}

	SupplierAdmin around (Object poa, SupplierAdminBase impl) throws Throwable :
		execution (SupplierAdmin
                           EventChannelImpl.CorbaGetSupplierAdminRef (Object, SupplierAdminBase))
		&& args (poa, impl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		return SupplierAdminHelper.narrow (obj);
	}

	ProxyPushSupplier around (Object poa, ProxyPushSupplierBase impl) throws Throwable :
		execution (ProxyPushSupplier
                           ConsumerAdminImpl.CorbaGetProxyPushSupplierRef (Object, ProxyPushSupplierBase))
		&& args (poa, impl)
        {
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		ProxyPushSupplier pps = ProxyPushSupplierHelper.narrow (obj);
		return pps;
	}

	ProxyPullSupplier around (Object poa, ProxyPullSupplierBase impl) throws Throwable :
		execution (ProxyPullSupplier
                           ConsumerAdminImpl.CorbaGetProxyPullSupplierRef (Object, ProxyPullSupplierBase))
		&& args (poa, impl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		ProxyPullSupplier pps = ProxyPullSupplierHelper.narrow (obj);
		return pps;
	}
			
	ProxyPushConsumer around (Object poa, ProxyPushConsumerBase impl) throws Throwable :
		execution (ProxyPushConsumer
                           SupplierAdminImpl.CorbaGetProxyPushConsumerRef (Object, ProxyPushConsumerBase))
		&& args (poa, impl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		return ProxyPushConsumerHelper.narrow (obj);
	}

	ProxyPullConsumer around (Object poa, ProxyPullConsumerBase impl) throws Throwable :
		execution (ProxyPullConsumer
                           SupplierAdminImpl.CorbaGetProxyPullConsumerRef (Object, ProxyPullConsumerBase))
		&& args (poa, impl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (impl);
		return ProxyPullConsumerHelper.narrow (obj);
	}

}
