package edu.wustl.doc.facet.feature_event_struct;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect EventStructIntroAspect {
	
	public void PushConsumer.push (Event event) { }

}
