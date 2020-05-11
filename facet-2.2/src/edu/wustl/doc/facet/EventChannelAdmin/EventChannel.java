/*
 * $Id: EventChannel.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.EventChannelAdmin;

/**
 * The EventChannel interface.
 */
public interface EventChannel {
    public void destroy();
    public ConsumerAdmin for_consumers();
    public SupplierAdmin for_suppliers();
}
