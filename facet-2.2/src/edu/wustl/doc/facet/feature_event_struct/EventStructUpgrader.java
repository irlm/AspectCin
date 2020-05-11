package edu.wustl.doc.facet.feature_event_struct;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect EventStructUpgrader implements Upgradeable, EventStructFeature {

    pointcut upgradeLocations() :
        this (Upgradeable) &&
        !this (EventStructFeature);

    void around (PushConsumer pc) :
        call (void PushConsumer.push ()) && target (pc) &&
        upgradeLocations ()
    {
            pc.push (new Event());
    }
}
