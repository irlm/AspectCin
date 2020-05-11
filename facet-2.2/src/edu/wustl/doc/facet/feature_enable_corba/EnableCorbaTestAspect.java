package edu.wustl.doc.facet.feature_enable_corba;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventComm.*;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Contains code relevant only to the unit tests so that we can
 * exclude this when we are building a very light library with no unit
 * tests
 */
aspect EnableCorbaTestAspect {

        declare parents : EventChannelTestCaseBase extends EventChannelCorbaTestCase;

        void around (Object orb, boolean flag) :
                execution (void
                           EventChannelTestCaseBase.CorbaShutdownORB (Object, boolean))
                && args (orb, flag)
        {
                ORB Orb = (ORB) orb;
                Orb.shutdown (flag);
        }

        void around (Object orb) :
                execution (void
                           EventChannelTestCaseBase.CorbaRunORB (Object))
                && args (orb)
        {
                ORB Orb = (ORB) orb;
                Orb.run ();
        }

        Object around (Object orb) :
                execution (void
                           EventChannelTestCaseBase.CorbaGetAndActivatePOA (Object))
                && args (orb)
        {
                ORB Orb = (ORB) orb;
                POA Poa = null;
                
                try {
                        Poa = POAHelper.narrow (Orb.resolve_initial_references ("RootPOA"));
                        Poa.the_POAManager ().activate();
                } catch (Throwable e) { e.printStackTrace (); }
                
                return Poa;
        }        

	PushConsumer around (Object poa, PushConsumerBase consumerImpl) throws Throwable :
		execution (PushConsumer
                           EventChannelTestCaseBase.CorbaGetPushConsumerRef (Object, PushConsumerBase))
		&& args (poa, consumerImpl) 
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (consumerImpl);
		return PushConsumerHelper.narrow (obj); 
	}

	PullSupplier around (Object poa, PullSupplierBase supplierImpl) throws Throwable :
		execution (PullSupplier
                           EventChannelTestCaseBase.CorbaGetPullSupplierRef (Object, PullSupplierBase))
		&& args (poa, supplierImpl)
	{
                POA Poa = (POA) poa;
		org.omg.CORBA.Object obj = Poa.servant_to_reference (supplierImpl);
		return PullSupplierHelper.narrow (obj);
	}
        
        
}
