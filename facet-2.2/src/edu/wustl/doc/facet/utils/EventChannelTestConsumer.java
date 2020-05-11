/*
 * $Id: EventChannelTestConsumer.java,v 1.3 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet.utils;

import edu.wustl.doc.facet.EventChannelAdmin.EventChannel;

/**
 * Base class for all EventChannel test consumers
 *
 * @author      Ravi Pratap
 * @version     $Revision: 1.3 $
 */
public abstract class EventChannelTestConsumer {

	public abstract void createAndConnect (EventChannel ec,
                                               Object poa,
                                               Object orb) throws Throwable;

	public void preRun () throws Throwable { }
}
