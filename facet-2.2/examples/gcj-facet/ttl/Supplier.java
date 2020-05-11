import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

/*
 * Placeholder class which implements the Java interface
 */
public class Supplier implements PushSupplier {

	ProxyPushConsumerBase ppc_;
	
	public Supplier (ProxyPushConsumerBase ppc)
	{
		this.ppc_ = ppc;
	}

	/*
	 * Delegate all the work to native C++ methods
	 */
	public native void sendEvents ();
	public native void disconnect_push_supplier ();
}
