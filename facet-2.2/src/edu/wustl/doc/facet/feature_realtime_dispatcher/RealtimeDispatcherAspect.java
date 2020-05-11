package edu.wustl.doc.facet.feature_realtime_dispatcher;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

aspect RealtimeDispatcherAspect {

	Dispatcher around () : call (Dispatcher.new ()) {
		return new RealtimeDispatcher ();
	}

        //
        // Add appropriate accessors
        // 

        public int EventHeader.getPriority ()
        {
                return this.priority;
        }

        public void EventHeader.setPriority (int p)
        {
                this.priority = p;
        }
}
