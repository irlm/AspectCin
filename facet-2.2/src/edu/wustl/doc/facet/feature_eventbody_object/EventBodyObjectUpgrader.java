package edu.wustl.doc.facet.feature_eventbody_object;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Aspect to fix up other unit tests that don't expect any bodies and
 * therefore do not initialize the payload in an Event.
 *
 */
aspect EventBodyObjectUpgrader  {

	pointcut upgradeLocations() :
		this (Upgradeable) &&
		!this (EventBodyObjectFeature);

	after () returning (Event data) :
		call (Event.new()) &&
		upgradeLocations()
        {
                data.payload = new Object ();
	}
}
