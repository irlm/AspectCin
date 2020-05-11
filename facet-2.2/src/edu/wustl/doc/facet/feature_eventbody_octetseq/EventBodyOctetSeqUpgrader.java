package edu.wustl.doc.facet.feature_eventbody_octetseq;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

/**
 * Aspect to fix up other unit tests that don't expect octet sequence
 * bodies and therefore do not initialize the octet sequence body in
 * an Event.
 */
aspect EventBodyOctetSeqUpgrader  {

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(CorbaEventBodyOctetSeqFeature);

    after () returning (Event data) :
        call(Event.new()) &&
        upgradeLocations()
    {
        data.payload = new byte[0];
    }
}
