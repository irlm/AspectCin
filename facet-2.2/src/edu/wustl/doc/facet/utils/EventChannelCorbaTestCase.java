/*
 * $Id: EventChannelCorbaTestCase.java,v 1.2 2003/08/26 19:49:49 ravip Exp $
 */

package edu.wustl.doc.facet.utils;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 * @author     Frank Hunleth
 * @version    $Revision: 1.2 $
 */
public class EventChannelCorbaTestCase extends FacetMTTestCase {

        Barrier shutdown_barrier_ = null;
        
        /**
         * Create a new EventChannelTestCase instance.  Instances are
         * usually created by the jUnit test framework.
         *
         * @param s The name of the test case
         */
        public EventChannelCorbaTestCase (String s)
        {
                super(s);
        }


        /**
         * Simple syncronized counter utility class.  This comes in handy
         * any many tests.
         */
        static public class Counter {
                private int count_ = 0;

                public synchronized int incrCount ()
                {
                        count_ = count_ + 1;
                        return count_;
                }

                public synchronized int decrCount ()
                {
                        count_ = count_ - 1;
                        return count_;
                }

                public synchronized int getCount ()
                {
                        return count_;
                }
        }

        /**
         * The test event channel.
         */
        public class EventChannelThread extends TestCaseRunnable {

                /**
                 * Initialize and run the event channel.
                 */
                public void runTestCase () throws Throwable
                {
                        // Initialize the orb.
                        ORB orb = ORB.init (new String[0], null);

                        POA poa = POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));
                        poa.the_POAManager ().activate ();

                        EventChannelImpl evImpl = new EventChannelImpl (orb, poa);

                        org.omg.CORBA.Object obj =
                                poa.servant_to_reference (evImpl);

                        EventChannelCorbaTestCase.this.addCorbaObjectIor ("EventChannel",
                                                                     orb.object_to_string (obj));
                        
                        ServerKillerImpl skImpl = new ServerKillerImpl (orb);
                        obj = poa.servant_to_reference (skImpl);
                        EventChannelCorbaTestCase.this.addCorbaObjectIor ("EventChannelKiller",
                                                                     orb.object_to_string (obj));
                        
                        EventChannelCorbaTestCase.this.notifyInitializationBarrier("Channel");
                        
                        orb.run ();
                }
        }

        /**
         * The test event consumer.
         */
        protected class ConsumerThread extends TestCaseRunnable {
                private EventChannelTestConsumer consumer_;

                public ConsumerThread (EventChannelTestConsumer consumer)
                {
                        consumer_ = consumer;
                }

                /**
                 * Run the event consumer.
                 */
                public void runTestCase () throws Throwable
                {
                        // Initialize the orb.
                        ORB orb = ORB.init (new String[0], null);

                        POA poa = POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));
                        poa.the_POAManager ().activate ();

                        EventChannelCorbaTestCase.this.waitOnInitializationBarrier ("Channel");

                        org.omg.CORBA.Object obj =
                                orb.string_to_object (EventChannelCorbaTestCase.this.getCorbaObjectIor ("EventChannel"));

                        EventChannel ec = EventChannelHelper.narrow (obj);
                        assertNotNull (ec);

                        consumer_.createAndConnect (ec, poa, orb);

                        EventChannelCorbaTestCase.this.notifyInitializationBarrier ("Consumer");

                        consumer_.preRun ();
                        orb.run ();
                        EventChannelCorbaTestCase.this.shutdown_barrier_.waitOnBarrier();
                        orb.shutdown (true);
                }
        }

        /**
         * The test event supplier.
         */
        protected class SupplierThread extends TestCaseRunnable {
                private EventChannelTestSupplier supplier_;

                public SupplierThread (EventChannelTestSupplier supplier)
                {
                        supplier_ = supplier;
                }

                public void runTestCase () throws Throwable
                {
                        // Initialize the orb.
                        ORB orb = ORB.init (new String[0], null);

                        // Wait until the channels and the consumers have initialized.
                        EventChannelCorbaTestCase.this.waitOnInitializationBarrier ("Channel");
                        EventChannelCorbaTestCase.this.waitOnInitializationBarrier ("Consumer");

                        org.omg.CORBA.Object obj =
                                orb.string_to_object (EventChannelCorbaTestCase.this.getCorbaObjectIor ("EventChannel"));

                        EventChannel ec = EventChannelHelper.narrow (obj);
                        assertNotNull (ec);

                        // Let the test case's supplier send its events.
                        supplier_.runTest (ec, orb);
                        EventChannelCorbaTestCase.this.shutdown_barrier_.waitOnBarrier();

                        // Get the server killer so that we can terminate the event channel
                        // task.
                        obj = orb.string_to_object
                                (EventChannelCorbaTestCase.this.getCorbaObjectIor ("EventChannelKiller"));

                        ServerKiller sk = ServerKillerHelper.narrow (obj);
                        assertNotNull (sk);

                        // Kill the event channel.
                        sk.shutdown ();

                        orb.shutdown (true);
                        orb.destroy ();
                }
        }

        /*
         * Intercept the call to runTestCaseRunnables and initialize
         * the barriers depending on the number of threads.
         */
        protected void runTestCaseRunnables (final TestCaseRunnable[] runnables)
        {
                int numEventChannels = 0;
                int numConsumers = 0;
                int numSuppliers = 0;

                for (int i = 0; i < runnables.length; i++) {
                        if (runnables[i] instanceof EventChannelThread) {
                                numEventChannels++;
                        } else if (runnables[i] instanceof ConsumerThread) {
                                numConsumers++;
                        } else if (runnables[i] instanceof SupplierThread) {
                                numSuppliers++;
                        }
                }

                createInitializationBarrier ("Channel", numEventChannels);
                createInitializationBarrier ("Consumer", numConsumers);

                shutdown_barrier_ = new Barrier (numConsumers + numSuppliers);
                
                super.runTestCaseRunnables (runnables);
        }
}
