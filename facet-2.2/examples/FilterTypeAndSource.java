//
// Sample program to demonstrate use of the FACET EventChannel
//
// This example demonstrates Source and Type filtering
// and makes use of an object payload
//
// Please enable feature_eventtype_filter, feature_source_filter and
// feature_eventbody_object to compile this example.
//

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

public class FilterTypeAndSource {

    public static void main (String [] args) {

	EventChannelImpl eci = new EventChannelImpl (null, null);
	ConsumerAdmin ca = eci.for_consumers ();
	SupplierAdmin sa = eci.for_suppliers ();

	ProxyPushSupplier pps = ca.obtain_push_supplier ();
	PushConsumer consumer = new Consumer ();


	//
	// We set ourselves up so that we only receive events of a
	// particular type (in this case any type) and source
	//
        
	ConsumerQOS qos = new ConsumerQOS ();
        
	Dependency [] deps = new Dependency [1];

	deps [0] = new Dependency ();
	deps [0].setHeader (new EventHeader ());
	deps [0].getHeader ().setType (FilterOpTypes.TYPE_ANY);
        deps [0].getHeader ().setSource (2);

        qos.setDependencies (deps);
	
	pps.connect_push_consumer (consumer, qos);

	ProxyPushConsumer ppc = sa.obtain_push_consumer ();
	Supplier supplier = new Supplier (ppc);

	supplier.sendEvents ();

    }
}

//
// A sample consumer. In this example, the registration with the
// event channel is being done by the code in main ()
// 
class Consumer extends EventChannelPushConsumer {

	public void push ()
	{
		System.out.println ("In this configuration, I am not supposed to be called !");
	}

        //
        // This method is called when an event is delivered to the
        // consumer
        //
	public void push (Event data)
	{
		System.out.println ("Event received of type: " + data.getHeader ().getType ()
                                    + " and source : " + data.getHeader ().getSource ());

                System.out.println ("Event payload :" + data.getPayload ());
	}
}

//
// The supplier who pushes a couple of events
// 
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
			data.header = new EventHeader ();

                        data.getHeader ().setType (i);
                        data.getHeader ().setSource (i);

                        //
                        // We use an object payload so we can
                        // practically send anything across
                        //
                        data.setPayload (this);
			
			ppc_.push (data);
		}
	}
	
	public void disconnect_push_supplier ()
	{

	}
}
