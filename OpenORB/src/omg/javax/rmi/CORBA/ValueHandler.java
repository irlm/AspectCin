/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * This interface provides a way to all serialization and deserialization of objects.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:53 $
 */

public abstract interface ValueHandler
{
    /**
     * This method can be used to write GIOP data, including RMI remote objects and
     * serialized data objects, to an underlying portable OutputStream.
     */
    public void writeValue( org.omg.CORBA.portable.OutputStream out,
                            java.io.Serializable value );

    /**
     * This method can be used to read GIOP data, including RMI remote objects and
     * serialized data objects, from an underlaying portable InputStream.
     */
    public java.io.Serializable readValue( org.omg.CORBA.portable.InputStream in,
                                           int offset,
                                           java.lang.Class clz,
                                           String repositoryID,
                                           org.omg.SendingContext.RunTime sender );

    /**
     * This method returns the RMI-style repository ID string fro clz.
     */
    public String getRMIRepositoryID( java.lang.Class clz );

    /**
     * This method returns true if the value is custom marshaled and therefore requires a
     * chunked encoding, and false otherwise.
     */
    public boolean isCustomMarshaled( java.lang.Class clz );

    /**
     * This method returns the ValueHandler object's SendingContext::RunTime object reference,
     * which is used to construct the SendingContextRuntTime service context.
     */
    public org.omg.SendingContext.RunTime getRunTimeCodeBase();

    /**
     * This method returns the serialization replacement for the value object.
     */
    public java.io.Serializable writeReplace( java.io.Serializable value );

}
