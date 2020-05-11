package edu.wustl.doc.facet.feature_eventbody_octetseq;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.feature_event_struct.SimpleEventTestCase;
import edu.wustl.doc.facet.EventComm.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 */
public class TestEventOctetSeq extends SimpleEventTestCase
	implements Upgradeable, EventBodyOctetSeqFeature {
	
	private int sentEventNumber_ = 0;
	private int rcvdEventCount_ = 0;

	/**
	 * Create a new TestEventAny instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestEventOctetSeq (String s) {
		super(s);
	}

	public boolean handleEvent(Event data) {

		int eventNum = data.getPayload ().length;

		Assert.assertEquals(rcvdEventCount_, eventNum);

		for (byte i = 0; i < rcvdEventCount_; i++) {
			Assert.assertEquals (data.getPayload () [i], i);
		}

		rcvdEventCount_++;

		return (rcvdEventCount_ >= 100);
	}

	public Event getNextEventToSend (Object orb)
        {
		if (sentEventNumber_ < 100) {
			Event data = new Event();
			data.setPayload (new byte [sentEventNumber_]);

			for (byte i = 0; i < sentEventNumber_; i++) {
				data.getPayload () [i] = i;
			}

			sentEventNumber_++;

			return data;
		} else {
			return null;
		}
	}

	public void testEventBodyOctetSeq () {

		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());
		
		runTestCaseRunnables(tct);
	}

    	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventOctetSeq.class);
		}
	}
}
