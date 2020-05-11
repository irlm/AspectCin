package edu.wustl.doc.facet.feature_eventbody_octetseq;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

aspect EventBodyOctetSeqIntroAspect {

	public byte [] Event.payload;
	
}
