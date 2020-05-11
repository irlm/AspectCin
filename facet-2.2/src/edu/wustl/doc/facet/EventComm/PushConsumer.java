/*
 * $Id: PushConsumer.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */
package edu.wustl.doc.facet.EventComm;

public interface PushConsumer {
    public void push();
    public void disconnect_push_consumer();
}
