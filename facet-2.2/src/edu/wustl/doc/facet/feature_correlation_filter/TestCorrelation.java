/*
 * $Id: TestCorrelation.java,v 1.7 2003/08/21 14:28:29 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.feature_eventvector.SimpleEventVecTestCase;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestCorrelation extends SimpleEventVecTestCase
	implements Upgradeable, CorrelationFilterFeature {
	
	private int sentEventNumber_ = 0;
	private int rcvdEventCount_ = 0;

	/**
	 * Create a new TestCorrelation instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestCorrelation(String s) {
		super(s);
	}

	public boolean handleEvent(Event data) {
		Assert.fail ("handleEvent should never be called!");
		return true;
	}

	public boolean handleEvents(Event[] data) {
		Assert.assertEquals(2, data.length);

		int eventNum1 = data[0].getHeader().getType();
		int eventNum2 = data[1].getHeader().getType();

		if (eventNum1 > eventNum2) {
			Assert.assertEquals(2, eventNum2);
			Assert.assertEquals(3, eventNum1);
		} else {
			Assert.assertEquals(3, eventNum2);
			Assert.assertEquals(2, eventNum1);
		}

		rcvdEventCount_++;

		return (rcvdEventCount_ >= 10);
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
		qos.dependencies = new Dependency[3];

		qos.dependencies[0] = new Dependency();
		qos.dependencies[0].setHeader (new EventHeader());
		qos.dependencies[0].setFilterOp (FilterOpTypes.CORRELATE_AND);

		qos.dependencies[1] = new Dependency();
		qos.dependencies[1].setHeader (new EventHeader());
		qos.dependencies[1].getHeader ().setType (2);
		qos.dependencies[1].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

		qos.dependencies[2] = new Dependency();
		qos.dependencies[2].setHeader(new EventHeader());
		qos.dependencies[2].getHeader ().setType (3);
		qos.dependencies[2].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

		pps.connect_push_consumer (pc, qos);
	}

	public void testEventCorrelation () {

		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestCorrelation.class);
		}
	}
}
