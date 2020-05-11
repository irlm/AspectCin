/*
 * $Id: EventChannelTestSupplier.java,v 1.3 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet.utils;

import edu.wustl.doc.facet.EventChannelAdmin.EventChannel;

/**
 * Interface for all EventChannel test suppliers
 *
 * @author      Ravi Pratap
 * @version     $Revision: 1.3 $
 */
public interface EventChannelTestSupplier {

	public void runTest (EventChannel ec, Object orb) throws Throwable;

}
