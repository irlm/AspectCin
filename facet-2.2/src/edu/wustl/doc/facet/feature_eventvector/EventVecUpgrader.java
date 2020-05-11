package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/*
 * Upgrade code that was written without knowledge of Event vectors.
 */
public aspect EventVecUpgrader {

	public void PushConsumerBase.push_vec (Event[] data)
	{
		for (int i = 0; i < data.length; i++) {
			this.push (data[i]);
		}
	}
}
