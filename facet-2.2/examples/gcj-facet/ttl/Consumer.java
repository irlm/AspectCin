import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

/*
 * We need this class only to inherit from FACET's classes
 * which are Java classes
 */
public class Consumer extends EventChannelPushConsumer {

	/*
	 * Push method is implemented natively of course
	 */
	public native void push (Event data);
	
}
