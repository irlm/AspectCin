package edu.wustl.doc.facet.feature_event_any;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;


aspect EventAnyUpgrader implements Upgradeable, EventAnyFeature {

    /*
    public void (edu.wustl.doc.facet.Upgradeable
                 && !edu.wustl.doc.facet.event_any.CorbaEventAnyFeature
                 && PushConsumerPOA+).push(Any event) {
        this.push();
    }
    */

    /* This is really sad, but I can't get what's above to work right. :( */
    public void edu.wustl.doc.facet.throughput_test.ThroughputTestCase.ThroughputConsumer.push(Any event) {
        this.push();
    }

    pointcut upgradeLocations() :
        this(Upgradeable) &&
        !this(CorbaEventAnyFeature);

    void around (PushConsumer pc) :
        call(void PushConsumer.push()) && target(pc) &&
        upgradeLocations() {

        ORB orb = ORB.init();
        pc.push(orb.create_any());
    }
}
