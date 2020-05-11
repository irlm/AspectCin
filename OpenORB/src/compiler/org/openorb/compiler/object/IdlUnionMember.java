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
 * Cette classe represente l'objet IDL Union member
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlUnionMember extends IdlObject implements org.openorb.compiler.idl.reflect.idlUnionMember
{
    /**
     * L'expression du discriminant de ce membre d'union
     */
    private String exp;

    /**
     * Valeur de l'expression ( un entier )
     */
    private long value;

    /**
     * Indique si le type du membre est le meme que le prochaine membre
     */
    private boolean next;

    /**
     * Indique si ce membre est un membre par defaut
     */
    private boolean _default;

    /**
     * Cree un objet IDL Union member
     */
    public IdlUnionMember( IdlObject father )
    {
        super( IdlType.e_union_member, father );
        value = 0;
        next = false;
        _default = false;
    }

    /**
     * Retourne le type du membre
     *
     * @return le type
     */
    public IdlObject type()
    {
        return ( IdlObject ) _list.elementAt( 0 );
    }

    /**
     * Fixe le type du membre
     *
     * @param tp le type
     */
    public void type ( IdlObject tp )
    {
        _list.removeAllElements();
        _list.addElement( tp );
    }

    /**
     * Fixe l'expression du discriminant
     *
     * @param expr l'expression
     */
    public void setExpression( String expr )
    {
        exp = expr;
    }

    /**
     * Retourne l'expression du discriminant
     *
     * @return l'expression
     */
    public String getExpression()
    {
        return exp;
    }

    /**
     * Fixe la valeur du discriminant
     *
     * @param val la valeur
     */
    public void setValue( long val )
    {
        value = val;
    }

    /**
     * Retourne la valeur du discriminant
     *
     * @return la valeur
     */
    public long getValue()
    {
        return value;
    }

    /**
     * Permet d'indiquer que le type de ce membre est le meme
     * que celui du prochain membre
     */
    public void setAsNext()
    {
        next = true;
    }

    /**
     * Positionne _default a TRUE ce qui indique que ce membre est un membre
     * par defaut.
     */
    public void setAsDefault()
    {
        _default = true;
    }

    /**
     * Retourne le flag indiquant si le type du membre est identique
     * au second
     */
    public boolean isAsNext()
    {
        return next;
    }

    /**
     * Fixe le Type du membre de l'union
     */
    public void memberTypeAndNameIs( IdlObject obj, String s )
    {
        addIdlObject( obj );
        name( s );
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public boolean isDefault()
    {
        return _default;
    }

    public String expression()
    {
        return exp;
    }

    public Long value()
    {
        return new Long( value );
    }

    public org.openorb.compiler.idl.reflect.idlObject internal()
    {
        if ( next == true )
        {
            IdlObject obj = _upper;

            obj.reset();

            while ( obj.current() != this )
                obj.next();

            while ( obj.end() != true )
            {
                if ( ( ( IdlUnionMember ) obj.current() ).isAsNext() )
                    obj.next();
                else
                {
                    obj.current().reset();
                    return obj.current().current();
                }
            }
        }

        reset();
        return current();
    }



}

