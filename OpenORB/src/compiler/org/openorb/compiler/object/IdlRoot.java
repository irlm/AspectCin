/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

import java.util.Vector;

import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.parser.IdlParser;
import org.openorb.compiler.parser.IdlType;

/**
 * Cette classe represente la representation generique d'une donnee
 * de l'IDL
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:39 $
 */
public class IdlRoot extends IdlObject
{
    /**
     * The ID prefix
     */
    public String _mainPrefix = "IDL";

    // ------------
    // CONSTRUCTEUR
    // ------------
    /**
    * Cree un objet IDL root
    */
    public IdlRoot( CompilerProperties cp, IdlParser parser )
    {
        super( IdlType.e_root, null, parser );
        name( cp.getM_packageName() );
        _is_container = true;
    }

    /**
     * Return the prefix depth
     */
    public int getPrefixDepth()
    {
        return 0;
    }

    /**
     * Return the components of the ID
     */
    public String [] getIdComponents()
    {
        return new String[ 0 ];
    }

    /**
     * Retourne l'ID d'un objet
     */
    public String getId()
    {
        if ( _id != null )
            return _id;

        _id = new String( _mainPrefix + ":" );

        return _id;
    }

    // ----------------
    // NAME TO SEQUENCE
    // ----------------
    /**
    * Returns a sequence of identifiers given a supplied name .
    * The supplied name (in the form value.value.value) is
    * reutnred as a vector of Strings corresponding to the
    * the value elements of the name.
    *
    * @return <code>Vector</code> of matching identifiers.
    * @param name a String
    */
    public Vector nameToOtherSequence( String name )
    {
        int index = 0;
        int previous_index = 0;
        Vector seq = new Vector();

        try
        {
            while ( index != -1 )
            {
                index = name.indexOf( '.', previous_index );

                if ( index != -1 )
                {
                    seq.addElement( new String( name.substring( previous_index, index ) ) );
                    previous_index = index + 1;
                }
            }
        }
        catch ( StringIndexOutOfBoundsException ex )
        { }

        seq.addElement( new String( name.substring( previous_index, name.length() ) ) );

        return seq;
    }

    // ------
    // SEARCH
    // ------
    /**
    * Recherche un symbole au moyen du chemin defini sous forme
    * d'une sequence
    *
    * @return l'objet trouve
    * @param la sequence de noms
    */
    public IdlObject search( Vector seq, boolean scoped )
    {
        IdlObject obj = null;
        int deb = 0;

        if ( ( ( String ) ( seq.elementAt( 0 ) ) ).equals( name() ) )
            deb = 1;

        obj = this;

        for ( int i = deb; i < seq.size(); i++ )
        {
            obj = obj.searchObject( ( String ) seq.elementAt( i ) );

            if ( obj == null )
                return null;
        }

        return obj;
    }

    // ----------
    // IS DEFINED
    // ----------
    /**
    * Indique si le symbole dont le nom est passe est deja defini
    *
    * @param name le nom du symbole
    * @param scoped flag qui indique si la recherche se limite a ce niveau
    * @return VRAI si le symbole est deja defini
    */
    public boolean isDefined( String name_, boolean scoped )
    {
        IdlObject obj;
        String name;

        if ( !_case_sensitive )
            name = name_.toUpperCase();
        else
            name = name_;

        // On regarde s'il s'agit d'un nom absolu ::Toto::Titi
        if ( name.charAt( 0 ) == ':' )
            name = name.substring( 2, name.length() );

        Vector liste = nameToSequence( name );

        if ( liste.size() == 1 )
            liste = nameToOtherSequence( name );

        if ( liste.size() > 1 )
        {
            obj = root().search( liste, scoped );

            if ( obj == null )
                return false;
            else
                return true;
        }

        for ( int i = 0; i < _list.size(); i++ )
        {
            obj = ( IdlObject ) _list.elementAt( i );

            if ( obj.name().equals( name ) == true )
                return true;
        }

        return false;
    }

    /**
     * Cette methode retourne l'objet demande en fonction de son nom.
     *
     * @return l'objet demande
     * @param name le nom de l'objet a retourner
     * @param scoped mettre a vrai si l'on restraint la recherche au scope de l'objet
     */
    public IdlObject returnObject( String name_, boolean scoped )
    {
        if ( name_ == null )
        {
            return null;
        }

        String name;
        if ( !_case_sensitive )
        {
            name = name_.toUpperCase();
        }
        else
        {
            name = name_;
        }

        // On regarde s'il s'agit d'un nom absolu ::Toto::Titi
        if ( name.charAt( 0 ) == ':' )
        {
            name = name.substring( 2, name.length() );
        }

        // On decoupe le nom en sequence si celui-ci est un complexe scoped
        Vector liste = nameToSequence( name );
        if ( liste.size() == 1 )
        {
            liste = nameToOtherSequence( name );
        }
        if ( liste.size() > 1 )
        {
            return root().search( liste, scoped );
        }
        for ( int i = 0; i < _list.size(); i++ )
        {
            IdlObject obj = ( IdlObject ) _list.elementAt( i );
            if ( obj.isSame( name ) == true )
            {
                return obj.sameAs( name );
            }
        }
        return null;
    }
}

