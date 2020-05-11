package edu.wustl.doc.facet.feature_event_pull;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

/**
 * Implementation of the ProxyPullConsumer interface.
 */
public class ProxyPullConsumerImpl extends ProxyPullConsumerBase {

	private EventChannelImpl eventChannel_;
	private int pollingInstance_ = 0;
	private Thread pollerThread_;

	private class PullPoller implements Runnable {
		private PullSupplier pullSupplier_;
		private int myInstance_;

		PullPoller (int instance, PullSupplier ps)
		{
			pullSupplier_ = ps;
			myInstance_ = instance;
		}

		public void run()
		{
			try {
				while (myInstance_ == ProxyPullConsumerImpl.this.pollingInstance_) {
					Event e = pullSupplier_.pull();
					EventCarrier ec = new EventCarrier(e);

					ProxyPullConsumerImpl.this.eventChannel_.pushEvent(ec);
				}

			} catch (org.omg.CORBA.SystemException se) {
				// Ignore this for now.  It probably means that the client
				// has ungracefully disconnected or timed out.
				// If "pulling" becomes popular, this will need to be
				// bulletproofed.
			} catch (Throwable t) {
				System.err.println("Unexpected exception: " + t);
				t.printStackTrace();
			}
		}
	}

	/**
	 *  Constructor for the ProxyPushSupplierImpl object.
	 */
	protected ProxyPullConsumerImpl (EventChannelImpl ec)
	{
		eventChannel_ = ec;
	}

	/**
	 * Connect a user's pull consumer to the event channel.
	 *
	 * @param  pull_supplier  Reference to a PushSupplier
	 */
	public synchronized void connect_pull_supplier (PullSupplier pull_supplier) 
	{
		if (pollerThread_ == null) {
			PullPoller poller = new PullPoller(pollingInstance_, pull_supplier);
			pollerThread_ = new Thread(poller);
			pollerThread_.start();
		} 
	}
		

	/**
	 * Disconnect from the event channel.
	 */
	public synchronized void disconnect_pull_consumer()
	{
		// Signal the polling thread that it should stop polling.
		pollingInstance_++;

		pollerThread_ = null;
	}
}
