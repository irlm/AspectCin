/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

import org.openorb.compiler.parser.IdlType;

/**
 * Cette classe represente l'objet IDL Attribute
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlAttribute extends IdlObject implements org.openorb.compiler.idl.reflect.idlAttribute
{
    /**
     * Flag qui indique si l'attribut est en lecture seule
     */
    private boolean _readonly;

    /**
     * Cree un objet IDL Attribute
     */
    public IdlAttribute( IdlObject father )
    {
        super( IdlType.e_attribute, father );
    }

    /**
     * Retourne le type de l'attribut
     *
     * @return le type
     */
    public IdlObject type()
    {
        return ( IdlObject ) _list.elementAt( 0 );
    }

    /**
     * Fixe le type de l'attribut
     *
     * @param tp le type
     */
    public void type ( IdlObject tp )
    {
        _list.removeAllElements();
        _list.addElement( tp );
    }

    /**
     * Retourne le flag readonly de l'attribut
     *
     * @return le flag
     */
    public boolean readOnly()
    {
        return _readonly;
    }

    /**
     * Fixe le flag readonly de l'attribut
     *
     * @param rd le flag
     */
    public void readOnly ( boolean rd )
    {
        _readonly = rd;
    }

    /**
    * Change prefix to my self but also to all contained objects
    */
    public void changePrefix( String prefix )
    {
        if ( _prefix_explicit != true )
        {
            _prefix = prefix;
        }
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public boolean isReadOnly()
    {
        return _readonly;
    }

    public org.openorb.compiler.idl.reflect.idlObject attributeType()
    {
        reset();
        return current();
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}
