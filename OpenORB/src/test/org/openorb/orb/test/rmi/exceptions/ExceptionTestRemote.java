/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.exceptions;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Remote interface for the exception test case.
 *
 * @author Stefan Reich
 */
public interface ExceptionTestRemote
    extends Remote
{
    /**
     * Throws a COMM_FAILURE exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwCommFailure () throws RemoteException;

    /**
     * Throws an INV_OBJ_REF exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwInvObjRef () throws RemoteException;

    /**
     * Throws a NO_PERMISSION exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwNoPermission () throws RemoteException;

    /**
     * Throws a MARSHAL exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwMarshal () throws RemoteException;

    /**
     * Throws a BAD_PARAM exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwBadParam () throws RemoteException;

    /**
     * Throws a OBJ_NOT_EXISTS exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwObjNotExist () throws RemoteException;

    /**
     * Throws a TRANSACTION_REQUIRED exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwTaRequired () throws RemoteException;

    /**
     * Throws a TRANSACTION_ROLLEDBACK exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwTaRolledBack () throws RemoteException;

    /**
     * Throws an INVALID_TRANSACTION exception.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwInvalidTransaction () throws RemoteException;

    /**
     * Throws a Throwable.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwThrowable () throws RemoteException;

    /**
     * Throws a NullPointerException.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwNPE () throws RemoteException;

    /**
     * Throws a RuntimeException.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwRuntimeException () throws RemoteException;

    /**
     * Throws an Error.
     *
     * @throws RemoteException When an error occurs.
     */
    void throwError () throws RemoteException;
}

