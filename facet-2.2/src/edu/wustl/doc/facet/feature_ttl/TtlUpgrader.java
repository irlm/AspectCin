package edu.wustl.doc.facet.feature_ttl;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/* Ensures that other unit tests are appropriately updated to
 * have a sufficient ttl, so that their events aren't dropped.
 */

aspect TtlUpgrader {

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(TtlFeature);

    after () returning (EventHeader header) :
        call(EventHeader.new()) &&
        upgradeLocations() {
        header.setTtl (255);
    }
}
