package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect EventVecIntroAspect {

	public void PushConsumer.push_vec (Event [] data)
        {
                for (int i = 0; i < data.length; i++) {
			this.push (data[i]);
		}
        }
}
