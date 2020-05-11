/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * Each IDL object builds by the org.openorb IDL parser implements this interface.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlObject
{
    /**
     * Return the object name
     */
    public String idlName();

    /**
     * Return the object type
     */
    public int idlType();

    /**
     * Return the final type ( if IDENTIFIER then returns the associated idl object )
     */
    public int idlFinalType();

    /**
     * Return the concrete type ( if IDENTIFIER or TYPEDEF then returns the associated idl object )
     */
    public int idlConcreteType();

    /**
     * Return the defined in object
     */
    public idlObject idlDefinedIn();

    /**
     * Return the IDL ID
     */
    public String idlID();

    /**
     * Return this idlObject content
     */
    public java.util.Enumeration content();

    /**
     * Return this idlObject content with a filter
     */
    public java.util.Enumeration filter( int type );

    /**
     * Return TRUE if this object contains a object type
     */
    public boolean containsObject( int type );

    /**
     * Return TRUE if this object is defined into a included file
     */
    public boolean included();
}
