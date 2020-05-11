package org.omg.CORBA.portable;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public interface InvokeHandler {

	OutputStream _invoke(String method, InputStream input, ResponseHandler handler) throws org.omg.CORBA.SystemException;
}


