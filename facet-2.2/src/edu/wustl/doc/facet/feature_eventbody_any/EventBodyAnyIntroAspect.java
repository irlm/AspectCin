package edu.wustl.doc.facet.feature_eventbody_any;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

aspect EventBodyAnyIntroAspect {

	public org.omg.CORBA.Any Event.payload;
	
}
