package edu.wustl.doc.facet.feature_eventbody_any;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import org.omg.CORBA.ORB;

/**
 * Aspect to fix up other unit tests that don't expect any bodies and
 * therefore do not initialize the payload in an Event.
 *
 */
aspect EventBodyAnyUpgrader  {

	pointcut upgradeLocations() :
		this (Upgradeable) && !this (EventBodyAnyFeature);

	after () returning (Event data) :
		call (Event.new())
		&& upgradeLocations()
        {
		// Get the singleton ORB.
		ORB orb = org.omg.CORBA.ORB.init ();

		// Create an empty any.
		data.payload = orb.create_any ();
	}
}
