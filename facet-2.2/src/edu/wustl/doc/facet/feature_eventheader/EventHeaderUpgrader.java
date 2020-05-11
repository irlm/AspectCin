/*
 * $Id: EventHeaderUpgrader.java,v 1.2 2003/04/10 22:49:40 ravip Exp $
 */

package edu.wustl.doc.facet.feature_eventheader;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;


aspect EventHeaderUpgrader implements Upgradeable, EventHeaderFeature {

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(EventHeaderFeature);

    after () returning (Event ev) :
        call(Event.new()) &&
        upgradeLocations()
    {
        ev.setHeader (new EventHeader());
    }
}
