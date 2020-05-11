package edu.wustl.doc.facet.feature_event_any;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

aspect EventAnyIntroAspect {

	public abstract void PushConsumer.push (org.omg.CORBA.Any data);
	
}
