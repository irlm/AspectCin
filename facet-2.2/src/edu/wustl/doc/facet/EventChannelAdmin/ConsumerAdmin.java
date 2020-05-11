/*
 * $Id: ConsumerAdmin.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.EventChannelAdmin;

import edu.wustl.doc.facet.EventComm.*;

/**
 * The ConsumerAdmin interface.
 */
public interface ConsumerAdmin {

    public ProxyPushSupplier obtain_push_supplier();
}
