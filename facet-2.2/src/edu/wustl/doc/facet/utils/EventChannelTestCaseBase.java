package edu.wustl.doc.facet.utils;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;

/**
 * Base class for the test case classes. This class
 * derives from the appropriate TestCase class based
 * on whether we are in CORBA or non-CORBA mode.
 *
 * The inheritance hierarchy is modified by the *Corba aspects.
 *
 * @author Ravi Pratap
 * @version $Revision: 1.4 $
 */
public abstract class EventChannelTestCaseBase {

        public EventChannelTestCaseBase (String s)
        {
                super (s);
        }

        public PushConsumer CorbaGetPushConsumerRef (Object poa,
                                                     PushConsumerBase consumerImpl)
                throws Throwable
	{
		// Empty - filled in my aspects
		return null;
	}

	public PullSupplier CorbaGetPullSupplierRef (Object poa,
                                                     PullSupplierBase supplierImpl)
                throws Throwable
	{
		// Empty - filled in by aspects
		return null;
	}

        public void CorbaRunORB (Object orb)
        {
                // Empty - filled by aspects 
        }

        public void CorbaShutdownORB (Object orb, boolean flag)
	{
                // Empty - filled by aspects
	}

        public Object CorbaGetAndActivatePOA (Object orb)
        {
                // Empty - filled by aspects
                return null;
        }
}
