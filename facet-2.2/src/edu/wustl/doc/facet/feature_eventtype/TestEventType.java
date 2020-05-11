package edu.wustl.doc.facet.feature_eventtype;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.feature_event_struct.SimpleEventTestCase;
import edu.wustl.doc.facet.EventComm.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 */
public class TestEventType extends SimpleEventTestCase
        implements Upgradeable, EventTypeFeature {
	
	private int sentEventNumber_ = 0;
	private int rcvdEventCount_ = 0;

	/**
	 * Create a new TestEventType instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestEventType(String s) {
		super(s);
	}

	public boolean handleEvent(Event data)
        {
		int eventNum = data.getHeader().getType();

		Assert.assertEquals(rcvdEventCount_, eventNum);

		rcvdEventCount_++;

		return (rcvdEventCount_ >= 100);
	}

	public Event getNextEventToSend (Object orb)
        {
		if (sentEventNumber_ < 100) {
			Event data = new Event();
			data.setHeader (new EventHeader());

			data.getHeader().setType (sentEventNumber_);
			sentEventNumber_++;

			return data;
		} else {
			return null;
		}
	}

	public void testEventType ()
        {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}


	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventType.class);
		}
	}
}
