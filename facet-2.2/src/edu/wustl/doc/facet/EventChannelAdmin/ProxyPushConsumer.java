/*
 * $Id: ProxyPushConsumer.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.EventChannelAdmin;

import edu.wustl.doc.facet.EventComm.*;

public interface ProxyPushConsumer extends PushConsumer {
    public void connect_push_supplier(PushSupplier push_supplier);
}
