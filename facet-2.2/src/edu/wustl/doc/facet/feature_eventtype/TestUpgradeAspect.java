package edu.wustl.doc.facet.feature_eventtype;

import edu.wustl.doc.facet.*;

/**
 * Aspect to mark all of the tests for this feature as upgradeable.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 * @since 1.0
 */
aspect TestUpgradeAspect {

    declare parents:
        ((edu.wustl.doc.facet.corba_eventtype.Test* ||
          edu.wustl.doc.facet.corba_eventtype.Test*..*) &&
         !TestUpgradeAspect)
        implements Upgradeable, EventTypeFeature;

}
