//
// Stubbed out class which consumers of the EventChannel should extend
//

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

import org.omg.CORBA.*;

//
// This should be an abstract class, but then AspectJ withholds
// necessary advice
//
public abstract class EventChannelPushConsumer extends PushConsumerBase {

	public void disconnect_push_consumer ()
	{ 
		throw new Error ("This method is essentially abstract; override me"); 
        }
	
	public void push ()
	{ 
		throw new Error ("This method is essentially abstract; override me"); 
        }

	public void push (Event ev)
	{
		throw new Error ("This method is essentially abstract; override me"); 
	}

	public void push (Any an)
	{
		throw new Error ("This method is essentially abstract; override me"); 
	}

	public void push (java.lang.Object o)
	{
		throw new Error ("This method is essentially abstract; override me"); 
	}

	public void push_vec (Event [] events)
        {
                throw new Error ("This method is essentially abstract; override me"); 
        }
}

