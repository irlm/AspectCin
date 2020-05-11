package edu.wustl.doc.facet.feature_eventbody_string;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

/**
 * Aspect to fix up other unit tests that don't expect string payload
 * bodies and therefore do not initialize the string payload in
 * an Event.
 */
aspect EventBodyStringUpgrader  {

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(EventBodyStringFeature);

    after () returning (Event data) :
        call(Event.new()) && upgradeLocations()
    {
        data.payload = "";
    }
}
