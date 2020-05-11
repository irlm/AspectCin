/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * When used as a parameter type, return type, or data member, the java Class type is mapped to
 * javax.rmi.CORBA.ClassDesc.
 *
 * @author Jerome Daniel
 * @author <a href="mailto:michael@rumpfonline.de">Michael Rumpf &lt;michael@rumpfonline.de&gt;</a>
 */
public class ClassDesc
    implements java.io.Serializable
{
    public String repid;
    public String codebase; // space-separated list of URLs
    static final long serialVersionUID = -3477057297839810709L;
}
