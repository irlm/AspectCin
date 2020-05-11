//
// Sample program to demonstrate use of the FACET EventChannel
//
// This example demonstrates Event correlation. Please enable the
// feature_correlation_filter to compile this example.
//

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

public class Correlation {

    public static void main (String [] args) {

	EventChannelImpl eci = new EventChannelImpl (null, null);
	ConsumerAdmin ca = eci.for_consumers ();
	SupplierAdmin sa = eci.for_suppliers ();

	ProxyPushSupplier pps = ca.obtain_push_supplier ();
	PushConsumer consumer = new Consumer ();


	//
	// We set ourselves up so that we only receive events
	// of type 2 and 3.
	//
	ConsumerQOS qos = new ConsumerQOS ();

        Dependency[] deps = new Dependency [3];

        deps [0] = new Dependency ();
        deps [0].setHeader (new EventHeader ());
        deps [0].setFilterOp (FilterOpTypes.CORRELATE_AND);

        deps [1] = new Dependency ();
        deps [1].setHeader (new EventHeader ());
        deps [1].getHeader ().setType (2);
        deps [1].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

        deps [2] = new Dependency ();
        deps [2].setHeader (new EventHeader ());
        deps [2].getHeader ().setType (3);
        deps [2].setFilterOp (FilterOpTypes.CORRELATE_MATCH);

        qos.setDependencies (deps);
	
	pps.connect_push_consumer (consumer, qos);

	ProxyPushConsumer ppc = sa.obtain_push_consumer ();
	Supplier supplier = new Supplier (ppc);

	supplier.sendEvents ();

    }
}

class Consumer extends EventChannelPushConsumer {

	public void push ()
	{
		System.out.println ("In this configuration, I am not supposed to be called !");
	}
	
	public void push_vec (Event [] data)
	{
		System.out.println ("Got data of length : " + data.length);

		System.out.println ("The types are : " + data [0].getHeader ().getType () +
				    " and " + data [1].getHeader ().getType ());
	}
}

class Supplier implements PushSupplier {

	ProxyPushConsumer ppc_;
	
	public Supplier (ProxyPushConsumer ppc)
	{
		this.ppc_ = ppc;
	}

	public void sendEvents ()
	{
		for (int i = 0; i < 10; ++i) {
			Event data = new Event ();
			data.setHeader (new EventHeader ());

			if (i % 2 == 0)
				data.getHeader ().setType (2);
			else
				data.getHeader ().setType (3);

			ppc_.push (data);
		}
	}
	
	public void disconnect_push_supplier ()
	{

	}
}
