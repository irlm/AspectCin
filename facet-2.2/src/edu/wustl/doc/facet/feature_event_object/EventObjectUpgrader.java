package edu.wustl.doc.facet.feature_event_object;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect EventObjectUpgrader implements Upgradeable, EventObjectFeature {

    /* This is really sad, but I can't get what's above to work right. :( */
    public void edu.wustl.doc.facet.throughput_test.ThroughputTestCase.ThroughputConsumer.push(Object event) {
        this.push();
    }

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(EventObjectFeature);

    void around (PushConsumer pc) :
	    call (void PushConsumer.push()) && target(pc) &&
	    upgradeLocations()
    {
	    pc.push (new Object());
    }
}
