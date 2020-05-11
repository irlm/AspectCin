/*
 * $Id: ProxyPushSupplier.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.EventChannelAdmin;

import edu.wustl.doc.facet.EventComm.*;

public interface ProxyPushSupplier extends PushSupplier {
    public void connect_push_consumer(PushConsumer push_consumer);
}
