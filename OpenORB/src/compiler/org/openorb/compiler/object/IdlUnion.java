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
 * Cette classe represente l'objet IDL Union
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlUnion extends IdlObject implements org.openorb.compiler.idl.reflect.idlUnion
{
    private boolean _forward = false;

    private IdlUnion _def = null;

    /**
     * Repere la position du membre par defaut
     */
    private int default_index = -1;

    /**
     * Cree un objet IDL Union
     */
    public IdlUnion( IdlObject father )
    {
        super( IdlType.e_union, father );
        _is_container = true;
    }

    /**
     * Fixe la position du membre par defaut
     *
     * @param idx la position du membre par defaut
     */
    public void index ( int idx )
    {
        default_index = idx;
    }

    /**
     * Retourne la position du membre par defaut
     *
     * @return la position du membre par defaut
     */
    public int index()
    {
        return default_index;
    }

    /**
     * This method returns an contained object
     */
    public IdlObject searchObject( String name )
    {
        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
                if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name().equals( name ) )
                    return ( IdlObject ) ( _list.elementAt( i ) );
        }

        if ( _list.size() > 0 )
            if ( switchFinalObject( ( IdlObject ) ( _list.elementAt( 0 ) ) ).kind() == IdlType.e_enum )
                return switchFinalObject( ( IdlObject ) ( _list.elementAt( 0 ) ) ).returnObject( name, true );

        return null;
    }

    public boolean isForward()
    {
        return _forward;
    }

    public void isForward( boolean forward )
    {
        _forward = forward;
    }

    public IdlUnion getDefinition()
    {
        return _def;
    }

    public void setDefinition( IdlUnion def )
    {
        _def = def;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public org.openorb.compiler.idl.reflect.idlObject discriminant()
    {
        if ( _forward )
            return ( ( org.openorb.compiler.idl.reflect.idlUnion ) _def ).discriminant();

        reset();

        current().reset();

        return current().current();

    }

    public java.util.Enumeration content()
    {
        java.util.Vector tmp = new java.util.Vector();

        reset();
        next();

        while ( end() != true )
        {
            tmp.addElement( current() );

            next();
        }

        return new org.openorb.compiler.idl.reflect.idlEnumeration( tmp );
    }

    public org.openorb.compiler.idl.reflect.idlUnion description()
    {
        return ( org.openorb.compiler.idl.reflect.idlUnion ) _def;
    }
}

