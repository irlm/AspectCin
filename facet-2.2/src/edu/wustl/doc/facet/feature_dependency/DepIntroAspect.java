package edu.wustl.doc.facet.feature_dependency;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect DepIntroAspect {

	public EventHeader Dependency.header;

	public Dependency [] ConsumerQOS.dependencies;
}
