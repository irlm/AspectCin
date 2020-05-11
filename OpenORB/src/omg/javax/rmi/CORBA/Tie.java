/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * This interface is to implement to delegate remote call to a RMI object.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:52 $
 */

public interface Tie extends org.omg.CORBA.portable.InvokeHandler
{
    /**
     * This method returns an object reference for the target object represented by the Tie.
     */
    public org.omg.CORBA.Object thisObject();

    /**
     * This method deactivates the target object represented by Tie.
     */
    public void deactivate();

    /**
     * This method returns the ORB for the Tie.
     */
    public org.omg.CORBA.ORB orb();

    /**
     * This method sets the ORB for the Tie.
     */
    public void orb( org.omg.CORBA.ORB orb );

    /**
     * This method must be implemented by tie classes. It will be called by Util.registerTarget to
     * notify the tie of its registered target implementation object.
     */
    public void setTarget( java.rmi.Remote target );

    /**
     * This method must be implemented by tie classes. It returns the registered target implementation
     * object for the tie.
     */
    public java.rmi.Remote getTarget();
}
