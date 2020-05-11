/*
 * $Id: ServerKillerImpl.java,v 1.2 2003/07/16 16:50:32 ravip Exp $
 */

package edu.wustl.doc.utils;

/**
 * Implementation of the ServerKiller CORBA interface to provide an
 * easy way to shutdown the ORB.
 *
 * This is a convenience class so that a shutdown method doesn't 
 * have to be added to all of the CORBA interfaces that are used
 * for our testing.
 *
 * @author     Frank Hunleth
 * @version    $Revision: 1.2 $
 */
public class ServerKillerImpl extends ServerKillerPOA {

        private org.omg.CORBA.ORB orb_;

        /**
         * Create a new ServerKillerImpl instance.
         *
         * @param orb The ORB that we will eventually kill.
         */

        public ServerKillerImpl (org.omg.CORBA.ORB orb)
        {
                this.orb_ = orb;
        }

        /**
         * Shutdown the ORB.
         */
        public void shutdown()
        {
                orb_.shutdown (false);
        }
}





