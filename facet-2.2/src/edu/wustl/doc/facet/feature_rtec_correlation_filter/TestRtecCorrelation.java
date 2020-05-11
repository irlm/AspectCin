package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.feature_eventvector.SimpleEventVecTestCase;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

import java.util.Vector;
/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestRtecCorrelation extends SimpleEventVecTestCase
	implements Upgradeable, RtecCorrelationFilterFeature {
	
	private int sentEventNumber_ = 0;
	private int rcvdEventCount_ = 0;

        private int dependencyLength_= 4; 
	/**
	 * Create a new TestRtecCorrelation instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestRtecCorrelation(String s) {
		super(s);
	}

	public boolean handleEvent (Event data) {
		Assert.fail ("handleEvent should never be called!");
		return true;
	}

	public boolean handleEvents (Event[] data) {

                 Assert.assertEquals (3, data.length);

                 //
                 // Heavy Data Structure (Vector) is used to avoid
                 // nested loops to check the event set.
                 //
                 Integer two = new Integer (2);
                 Integer three = new Integer (3);
                 Integer five = new Integer (5);
                 
                 Vector eventNum = new Vector(1);
                 eventNum.addElement (two); 
                 eventNum.addElement (three); 
                 eventNum.addElement (five); 

                  for (int i = 0; i != 3; i++) {
                          Integer event_type = new Integer (data [i].getHeader ().getType ());
                          if (!eventNum.contains (event_type) ) {
                                  Assert.fail ("Unexpected Events Delivered to me!!!");
                          }
                  }
                  
                  rcvdEventCount_++;

                  return (rcvdEventCount_ >= 4);
	}
        
	public Event getNextEventToSend (Object orb) {
		if (sentEventNumber_ < 42) {
			Event data = new Event();
			data.setHeader (new EventHeader());

			switch (sentEventNumber_ % 10) {
			case 0:
				data.getHeader ().setType (2);
				break;
			case 1:
				data.getHeader ().setType (3);
				break;
			case 2:
				data.getHeader ().setType (3);
				break;
			case 3:
				data.getHeader ().setType (2);
				break;
			case 4:
				data.getHeader ().setType (2);
				break;
			case 5:
				data.getHeader ().setType (4);
				break;
			case 6:
				data.getHeader ().setType (3);
				break;
			case 7:
				data.getHeader ().setType (5);
				break;
			case 8:
				data.getHeader ().setType (3);
				break;
			case 9:
				data.getHeader ().setType (2);
				break;
			}

			sentEventNumber_++;

			return data;
		} else {
			return null;
		}
	}

	public void connectPushConsumer(ProxyPushSupplier pps, PushConsumer pc)
	{
 		ConsumerQOS qos = new ConsumerQOS();
		qos.dependencies = new Dependency[dependencyLength_];

		qos.dependencies[0] = new Dependency();
		qos.dependencies[0].setHeader (new EventHeader());
                //
                // In Rtec QOS structure specification, when an
                // operator is inserted, then the number of children
                // following it should be set in header.source field.
                //
		qos.dependencies[0].getHeader ().setSource (3);
		qos.dependencies[0].setFilterOp (FilterOpTypes.CORRELATE_AND);

		qos.dependencies[1] = new Dependency();
		qos.dependencies[1].setHeader (new EventHeader());
		qos.dependencies[1].getHeader ().setType (2);
		qos.dependencies[1].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

 		qos.dependencies[2] = new Dependency();
 		qos.dependencies[2].setHeader(new EventHeader());
 		qos.dependencies[2].getHeader ().setType (3);
 		qos.dependencies[2].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

 		qos.dependencies[3] = new Dependency();
 		qos.dependencies[3].setHeader(new EventHeader());
 		qos.dependencies[3].getHeader ().setType (5);
 		qos.dependencies[3].setFilterOp (FilterOpTypes.CORRELATE_MATCH);


		pps.connect_push_consumer (pc, qos);
	}

	public void testRtecCorrelation () {

		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestRtecCorrelation.class);
		}
	}
}
