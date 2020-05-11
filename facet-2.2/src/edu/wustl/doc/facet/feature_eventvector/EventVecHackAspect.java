package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/*
 * This is something I can't even bear to look at right now but AspectJ 1.1
 * leaves me with no choice whatsoever
 */
public aspect EventVecHackAspect {

	public void PushConsumerOperations.push_vec (Event[] data)
	{
		for (int i = 0; i < data.length; i++) {
			this.push (data[i]);
		}
	}
}
