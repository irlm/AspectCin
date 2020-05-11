package edu.wustl.doc.facet.feature_timestamp;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import junit.framework.Assert;
import junit.framework.TestSuite;
import edu.wustl.doc.facet.feature_event_struct.SimpleEventTestCase;

public class TestCorbaTimestamp extends SimpleEventTestCase
        implements Upgradeable {
	
	private int sentEventNumber_ = 0;
	private int rcvdEventCount_ = 0;
	private long lastTimestamp_ = 0;

	public TestCorbaTimestamp(String s)
	{
		super(s);
	}

	public boolean handleEvent(Event data)
	{
		// No two messages should have the same timestamp and the
		// timestamps should always be greater than the last.
		// ***Assume no 63 bit roll over for this test.***

		long now = System.currentTimeMillis();

		Assert.assertTrue (data.getHeader().getTimestamp () >= lastTimestamp_);
		Assert.assertTrue (data.getHeader().getTimestamp () <= now);

		// This next one sometimes fails on busy machines.
		// Assert.assertTrue(data.header.timestamp > now - 100);

		lastTimestamp_ = data.getHeader ().getTimestamp ();

		rcvdEventCount_++;
		return (rcvdEventCount_ >= 100);
	}

	public Event getNextEventToSend (Object orb)
	{
		if (sentEventNumber_ < 100) {
			Event data = new Event();
			data.setHeader (new EventHeader());

			sentEventNumber_++;

			return data;

		} else 
			return null;
	}

	//
	// The actual jUnit test.  Create the threads and run them.
	//
	public void testTimestamp ()
	{
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}
	
	static aspect AddTests extends TestSuiteAdder {
		
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestCorbaTimestamp.class);
		}
	}
}
