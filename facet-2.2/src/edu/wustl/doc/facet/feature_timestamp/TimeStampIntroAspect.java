package edu.wustl.doc.facet.feature_timestamp;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect TimeStampIntroAspect {

	public long EventHeader.timestamp;
	
}
