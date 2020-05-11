/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

import java.util.Vector;

import org.openorb.compiler.parser.IdlParser;
import org.openorb.compiler.parser.IdlType;

/**
 * This class represents a generic type of IDL objects. IDL objects are e.g. modules, interfaces, etc.
 * The IdlCompiler is creating an in memory tree of an IDL file where each tree element is of
 * type IdlObject.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.7 $ $Date: 2004/07/28 15:49:55 $
 */
public class IdlObject
    implements java.io.Serializable, org.openorb.compiler.idl.reflect.idlObject
{
    /**
     * Type value, accessible via kind methods. (@see IdlType, kind).
     */
    public int _type;

    /**
     * The parent object.
     */
    public IdlObject _upper;

    /**
     * This flag is used to set whether the name must be adapted ( '_' is removed ).
     */
    public boolean adaptName = true;

    /**
     * CORBA Object prefix.
     */
    public String _prefix = null;

    /**
     * Name of the object.
     */
    public String _name;

    /**
     * ID of the object.
     */
    public String _id = null;

    /**
     * List of child objects.
     */
    public Vector _list;

    /**
     * Current position in the child object list.
     */
    public int _current = 0;

    /**
     * This flag indicates whether the IDL object is defined in an included IDL file.
     */
    public boolean _map = true;

    /**
     * Comment associated with the object.
     */
    public IdlComment _comment = null;

    /**
     * Implique la mise entre #ifndef du symbole
     */
    public boolean _diese = false;

    /**
     * Flag whether a case-sensitive mode for comparison is in effect.
     * When this is true all name comparisons are done in upper case.
     */
    public static boolean _case_sensitive = true;

    /**
     * This field is used by the JavaToIdl compiler to attach any additional
     * information to an IDL object. E.g. exceptions where the IDL name is
     * package::MyExEx and the Java name is just package.MyEx, the _opaque
     * field holds the name package.MyEx in this case.
     * In the getter/setter method case the whole name, including the prefix,
     * is stored in the field.
     * The type is java.lang.Object so that any kind of object can be
     * associated with this IDL object. In the JavaToIdl case the opaque
     * object is always of type String.
     */
    public java.lang.Object _opaque;

    /**
     * Flag qui precise si l'objet est importe
     */
    public boolean _import = true;

    /**
     * Y a t'il un underscore
     */
    public boolean _underscore = false;

    /**
     * Is TypeId used for this object ?
     */
    public boolean _prefix_explicit = false;

    /**
     * Must be set to true if this object can have inheritance.
     */
    public boolean _has_inheritance = false;

    /**
     * Must be set to true if this object is a container.
     */
    public boolean _is_container = false;

    private IdlParser m_parser = null;

    /**
     * Constructor
     *
     * @param type    type of the IDL object.
     * @param father  parent object
     */
    public IdlObject( int type, IdlObject father, IdlParser parser )
    {
        _type = type;
        _upper = father;
        _list = new Vector();
        m_parser = parser;
        if ( m_parser.getM_idlPrefix() != null )
        {
            _prefix = new String( m_parser.getM_idlPrefix() );
        }
        refreshIncluded();
    }

    public IdlObject( int type, IdlObject father )
    {
        this( type, father, father.getParser() );
    }

    public String toString() 
    {
        return getClass() + "{name=[" + idlName() + "], id=[" + idlID() + "]}";
    }

    public IdlParser getParser()
    {
        return m_parser;
    }

    /**
     * Retourne l'objet correspondant au type du switch dans le
     * cas d'une union.
     *
     * @return l'objet du switch
     * @param l'objet union
     */
    public IdlObject switchFinalObject( IdlObject obj )
    {
        IdlObject type;

        int p = obj.pos();

        switch ( obj.kind() )
        {

        case IdlType.e_ident :
            type = switchFinalObject( ( ( IdlIdent ) obj ).internalObject() );
            break;

        case IdlType.e_typedef :

        case IdlType.e_union_member :
            obj.reset();
            type = switchFinalObject( obj.current() );
            break;

        default :
            type = obj;
            break;
        }

        obj.pos( p );
        return type;
    }

    /**
     * Set the name of the IDL object.
     */
    public void name( String s )
    {
        if ( s.length() != 0 )
        {
            if ( ( s.charAt( 0 ) == '_' ) && ( adaptName ) )
            {
                _name = s.substring( 1 );
                _underscore = true;
            }
            else
                _name = new String( s );
        }
        else
            _name = new String( s );

        _id = null;
    }

    /**
     * Return the name of the IDL object.
     */
    public String name()
    {
        if ( !_case_sensitive )
            return new String( _name ).toUpperCase();

        return _name;
    }

    /**
     * Return the parent IDL object.
     */
    public IdlObject upper()
    {
        return _upper;
    }

    /**
     * Return the type of the IDL object.
     */
    public int kind()
    {
        return _type;
    }

    /**
     * Return the type of the final object. If the IDL object
     * is either a typedef or an identifier this method returns
     * the actual type of the IDL object.
     */
    public int final_kind()
    {
        if ( _type == IdlType.e_typedef )
        {
            return ( ( IdlObject ) _list.elementAt( 0 ) ).final_kind();
        }
        else
            if ( _type == IdlType.e_ident )
            {
                return ( ( IdlIdent ) this ).internalObject().final_kind();
            }
            else
                return kind();
    }

    /**
     * Return the final object. If the IDL object
     * is either a typedef or an identifier this method returns
     * the actual IDL object.
     */
    public IdlObject final_object()
    {
        if ( _type == IdlType.e_typedef )
        {
            return ( ( IdlObject ) _list.elementAt( 0 ) ).final_object();
        }
        else
            if ( _type == IdlType.e_ident )
            {
                return ( ( IdlIdent ) this ).internalObject().final_object();
            }
            else
                return this;
    }

    /**
     * Return the IDL object prefix.
     */
    public String getPrefix()
    {
        return _prefix;
    }

    /**
     * Set the id of the IDL object.
     */
    public void setId( String id )
    {
        _id = id;
    }

    /**
     * Return the id of the IDL object.
     */
    public String getId()
    {
        String [] comp = null;
        String str;

        if ( _id != null )
            return _id;

        comp = getIdComponents();

        str = "";

        for ( int i = 0; i < comp.length; i++ )
        {
            str = str + comp[ i ];

            if ( i + 1 < comp.length )
                str = str + "/";
        }

        _id = root()._mainPrefix + ":";

        if ( _prefix != null )
            _id = _id + _prefix + "/";

        _id = _id + str + ":1.0";

        return _id;
    }

    /**
     * Return the prefix depth
     */
    public int getPrefixDepth()
    {
        if ( upper()._prefix == null )
            if ( _prefix != null )
                return 0;

        if ( _prefix != null )
            if ( !upper()._prefix.equals( _prefix ) )
                return 0;

        return upper().getPrefixDepth() + 1;
    }

    /**
     * Return the components of the ID
     */
    public String [] getIdComponents()
    {
        String [] comp = null;
        String [] tmp = null;
        int deepth = 0;

        if ( ( upper().kind() == IdlType.e_typedef ) && ( _name == null ) )
        {
            tmp = upper().upper().getIdComponents();
            deepth = upper().getPrefixDepth();
        }
        else
        {
            tmp = upper().getIdComponents();
            deepth = getPrefixDepth();
        }

        if ( tmp.length < deepth )
            deepth = tmp.length;

        if ( _name != null )
        {
            comp = new String[ deepth + 1 ];

            int j = 0;

            for ( int i = ( tmp.length - deepth ); i < tmp.length; i++ )
                comp[ j++ ] = tmp[ i ];

            comp[ deepth ] = _name;

            return comp;
        }
        else
            return tmp;
    }

    // -----------------------------------
    // GESTION DE LA LISTE DES SOUS-OBJETS
    // -----------------------------------

    /**
    * Retourne la position courante dans la liste des sous-objets
    *
    * @return la position courante
    */
    public int pos()
    {
        return _current;
    }

    /**
    * Fixe la position courante dans la liste des sous-objets
    *
    * @param p la position courante
    */
    public void pos( int p )
    {
        _current = p;
    }

    /**
    * Initialise la position courante de la liste des sous-objets
    */
    public void reset()
    {
        _current = 0;
    }

    /**
    * Retourve Vrai dans le cas ou l'on a atteint la fin de la
    * liste des sous-objets
    *
    * @return VRAI si le parcours est fini
    */
    public boolean end()
    {
        if ( _current >= _list.size() )
        {
            return true;
        }
        return false;
    }

    /**
    * Retourne le nombre de sous-objets
    *
    * @return le nombre de sous objets
    */
    public int length()
    {
        return _list.size();
    }

    /**
    * Retourne l'objet courant de la liste des sous-objets
    *
    * @return l'objet courant
    */
    public IdlObject current()
    {
        if ( _list.size() > 0 && _current < _list.size() )
        {
            return ( IdlObject ) ( _list.elementAt( _current ) );
        }
        return null;
    }

    /**
    * Passe a la position suivante dans la liste des sous-objets
    */
    public void next()
    {
        if ( end() == false )
        {
            _current++;
        }
    }

    /**
    * Indique si l'objet courant est defini dans un fichier inclus
    *
    * @return VRAI si l'objet est inclus
    */
    public boolean included()
    {
        return _map;
    }

    /**
    * Recalcul si l'objet courant est defini dans un fichier inclus
    */
    public void refreshIncluded()
    {
        if ( _map == false )
        {
            return;
        }
        _map = true;
        if ( m_parser.getInclude_level() == 0 )
        {
            _map = false;
        }
    }

    // ----------------
    // NAME TO SEQUENCE
    // ----------------
    /**
    * Returns a sequence of identifiers given a supplied name .
    * The supplied name (in the form value:value:value) is
    * reutnred as a vector of Strings corresponding to the
    * the value elements of the name.
    *
    * @return a <code>Vector</code> of matching identifiers.
    * @param name a String
    */
    public Vector nameToSequence( String name )
    {
        int index = 0;
        int previous_index = 0;
        Vector seq = new Vector();

        try
        {
            while ( index != -1 )
            {
                index = name.indexOf( ':', previous_index );

                if ( index != -1 )
                {
                    seq.addElement( new String( name.substring( previous_index, index ) ) );
                    previous_index = index + 2;
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
        IdlObject obj = this;
        int deb = 0;

        if ( ( ( String ) ( seq.elementAt( 0 ) ) ).equals( name() ) )
            deb = 1;

        for ( int i = deb; i < seq.size(); i++ )
        {
            obj = obj.returnObject( ( String ) ( seq.elementAt( i ) ), scoped );

            if ( obj == null )
                break;
        }

        return obj;
    }

    // --------
    // MY SCOPE
    // --------
    /**
     * Return the scope of an object.
     * @param scope_name
     * @param level
     */
    public IdlObject myScope( String scope_name, int level )
    {
        IdlObject obj = this;
        boolean stop = false;
        boolean result = false;

        while ( stop != true )
        {
            if ( ( obj.kind() == IdlType.e_module ) ||
                    ( obj.kind() == IdlType.e_interface ) )
            {
                if ( obj.name().equals( scope_name ) )
                    return obj;

                result = obj.isDefined( scope_name, true );

                if ( result )
                    return obj;
            }

            obj = obj.upper();

            if ( obj == null )
                stop = true;
        }

        return null;
    }

    /**
    * Retourne VRAI si l'identificateur passe est deja utilise pour
    * un autre symbole.
    *
    * @return retourne VRAI si l'identificateur est deja utilise
    * @param name le nom a verifier
    * @param scoped mettre a vrai si l'on restraint la recherche au scope de l'objet
    */
    public boolean isVisible( String name_, boolean scoped )
    {
        name_ = adaptName( name_ );

        IdlObject obj = returnObject( name_, scoped );

        if ( obj == null )
            return false;

        if ( obj._import == false )
            return false;

        return true;
    }

    /**
    * Cette methode retourne l'objet demande en fonction de son nom.
    *
    * @return l'objet demande
    * @param name le nom de l'objet a retourner
    * @param scoped mettre a vrai si l'on restraint la recherche au scope de l'objet
    */
    public IdlObject returnVisibleObject( String name_, boolean scoped )
    {
        name_ = adaptName( name_ );

        IdlObject obj = returnObject( name_, scoped );

        if ( obj == null )
            return null;

        if ( obj._import == false )
            return null;

        return obj;
    }

    // ----------
    // IS DEFINED
    // ----------
    /**
     * Retourne VRAI si l'identificateur passe est deja utilise pour
     * un autre symbole.
     *
     * @return retourne VRAI si l'identificateur est deja utilise
     * @param name le nom a verifier
     * @param scoped mettre a vrai si l'on restraint la recherche au scope de l'objet
     */
    public boolean isDefined( String name, boolean scoped )
    {
        name = adaptName( name );

        IdlObject obj = returnObject( name, scoped );

        if ( obj == null )
            return false;

        return true;
    }

    // -------------
    // RETURN OBJECT
    // -------------
    /**
     * Cette methode retourne l'objet demande en fonction de son nom.
     *
     * @return l'objet demande
     * @param name le nom de l'objet a retourner
     * @param scoped mettre a vrai si l'on restraint la recherche au scope de l'objet
     */
    public IdlObject returnObject( String name_, boolean scoped )
    {
        IdlObject obj = null;
        String name;
        if ( !_case_sensitive )
        {
            name = name_.toUpperCase();
        }
        else
        {
            name = name_;
        }
        // check whether the name is absolute
        if ( name.charAt( 0 ) == ':' )
        {
            return root().returnObject( name.substring( 2, name.length() ), scoped );
        }
        // split the name into its constituents
        Vector liste = nameToSequence( name );

        if ( liste.size() > 1 )
        {
            obj = myScope( ( String ) ( liste.elementAt( 0 ) ), liste.size() );

            if ( obj == null )
                return root().returnObject( name, scoped );

            return obj.search( liste, scoped );
        }

        if ( _is_container )
        {

            obj = searchObject( name );

            if ( obj != null )
                return obj;
        }

        // Check for inheritance
        if ( _has_inheritance )
        {
            obj = returnInheritedObject( name );

            if ( obj != null )
                return obj;
        }

        if ( scoped == true )
            return obj;

        // Si l'on a rien trouve alors on va consulter le scope de son pere

        if ( obj == null )
            return upper().returnObject( name, scoped );
        else
            return obj;
    }

    // --------------
    // ADD IDL OBJECT
    // --------------
    /**
     * Ajoute un objet supplementaire comme contenu de cet objet
     */
    public void addIdlObject( IdlObject obj )
    {
        _list.addElement( obj );
    }

    /**
     * Insert un element
     */
    public void insertIdlObject( IdlObject obj, int idx )
    {
        _list.insertElementAt( obj, idx );
    }

    /**
     * Return the root IDL object.
     */
    public IdlRoot root()
    {
        IdlObject obj = this;

        while ( obj.upper() != null )
            obj = obj.upper();

        return ( IdlRoot ) obj;
    }

    /**
     * Permet de tester l'inclusion d'un type dans un autre
     *
     * @param type Le type conteneur
     * @return true si l'objet est inclus dans le conteneur specifie
     */
    public boolean into ( int type )
    {
        IdlObject obj = this;

        while ( obj.upper() != null )
        {
            if ( obj.kind() == type )
                return true;

            obj = obj.upper();
        }

        return false;

    }

    /**
     * Permet de tester la possession d'un type dans un autre
     *
     * @param type Le type conteneur
     * @return true si l'objet est inclus dans le conteneur specifie
     */
    public boolean contains( int type )
    {
        int i = 0;

        if ( kind() == type )
            return true;

        while ( i < _list.size() )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).contains( type ) == true )
                return true;

            i++;
        }

        return false;
    }

    /**
     * Return the member type.
     * This method actually returns the first element from the child list.
     * The method name is very confusing as it conflicts with the _type
     * member.
     *
     * @return The object type !? (The first child object).
     */
    public IdlObject type()
    {
        return ( IdlObject ) _list.elementAt( 0 );
    }

    /**
     * Set the type of the object.
     * This method removes all child objects and adds the specified as new child.
     *
     * @param tp The type of the object !? (The first child member).
     */
    public void type ( IdlObject tp )
    {
        _list.removeAllElements();
        _list.addElement( tp );
    }

    /**
     * Attach a default comment to this object.
     * IdlParser.idl_comment is used per default.
     */
    public void attach_comment()
    {
        _comment = m_parser.getIdl_comment();
        m_parser.setIdl_comment( null );
    }

    /**
     * Attach the specified comment to this object.
     */
    public void attach_comment( IdlComment comment )
    {
        _comment = comment;
    }

    /**
     * Return the comment attached to this object.
     */
    public IdlComment getComment()
    {
        return _comment;
    }

    /**
     * Check whether this IDL object has a comment attached.
     */
    public boolean hasComment()
    {
        if ( _comment != null )
            return true;

        return false;
    }

    /**
     * Set the case sensitive mode to the passed value.
     */
    public void sensitive( boolean s )
    {
        _case_sensitive = s;
    }

    /**
     * Permet de fixer le fait de l'utilisation de #ifndef
     */
    public void use_diese( boolean s )
    {
        _diese = s;
    }

    /**
     * Retourne si une section #ifndef est a utiliser
     */
    public boolean use_diese()
    {
        return _diese;
    }

    /**
     * Set the opaque object for this IDL object.
     */
    public void opaque( java.lang.Object op )
    {
        _opaque = op;
    }

    /**
     * Return the opaque object for ths IDL object.
     */
    public java.lang.Object opaque()
    {
        return _opaque;
    }

    /**
     * Apply a prefix
     */
    public void applyPrefix( String prefix )
    {
        if ( _prefix_explicit != true )
        {
            changePrefix( prefix );

            _prefix_explicit = true;
        }
    }

    /**
     * Change prefix to my self but also to all contained objects
     */
    public void changePrefix( String prefix )
    {
        if ( _prefix_explicit != true )
        {
            _prefix = prefix;

            for ( int i = 0; i < _list.size(); i++ )
            {
                ( ( IdlObject ) _list.elementAt( i ) ).changePrefix( prefix );
            }
        }
    }

    /**
     * This method returns an inherited object
     */
    public IdlObject returnInheritedObject( String name )
    {
        return null;
    }

    /**
     * This method returns true if this object is the same as the given name.
     */
    public boolean isSame( String name )
    {
        return adaptName( name() ).equals( adaptName( name ) );
    }

    /**
     * Return the equivalent object for the given name
     */
    public IdlObject sameAs( String name )
    {
        if ( isSame( adaptName( name ) ) )
            return this;

        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
            {
                if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).isSame( adaptName( name ) ) )
                    return ( IdlObject ) ( _list.elementAt( i ) );
            }
        }

        return null;
    }

    /**
     * This method returns an contained object
     */
    public IdlObject searchObject( String name )
    {
        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
            {
                if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).isSame( name ) )
                    return ( IdlObject ) ( _list.elementAt( i ) );
            }
        }

        return null;
    }

    /**
     * If the adaptName flag is set remove a leading underscore.
     *
     * @param name which should be adapted.
     *
     * @return adatped name.
     */
    public String adaptName( String name )
    {
        String s = name;

        if ( ( s.charAt( 0 ) == '_' ) && ( adaptName ) )
        {
            s = s.substring( 1 );
        }

        return s;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------
    public String idlName()
    {
        return _name;
    }

    public String idlID()
    {
        return getId();
    }

    public org.openorb.compiler.idl.reflect.idlObject idlDefinedIn()
    {
        if ( _upper._upper == null )
            return null;

        return _upper;
    }

    public int idlType()
    {
        switch ( _type )
        {

        case IdlType.e_array :
            return org.openorb.compiler.idl.reflect.idlType.ARRAY;

        case IdlType.e_module :
            return org.openorb.compiler.idl.reflect.idlType.MODULE;

        case IdlType.e_enum :
            return org.openorb.compiler.idl.reflect.idlType.ENUM;

        case IdlType.e_string :
            return org.openorb.compiler.idl.reflect.idlType.STRING;

        case IdlType.e_wstring :
            return org.openorb.compiler.idl.reflect.idlType.WSTRING;

        case IdlType.e_struct :
            return org.openorb.compiler.idl.reflect.idlType.STRUCT;

        case IdlType.e_union :
            return org.openorb.compiler.idl.reflect.idlType.UNION;

        case IdlType.e_attribute :
            return org.openorb.compiler.idl.reflect.idlType.ATTRIBUTE;

        case IdlType.e_const :
            return org.openorb.compiler.idl.reflect.idlType.CONST;

        case IdlType.e_exception :
            return org.openorb.compiler.idl.reflect.idlType.EXCEPTION;

        case IdlType.e_factory :
            return org.openorb.compiler.idl.reflect.idlType.FACTORY;

        case IdlType.e_fixed :
            return org.openorb.compiler.idl.reflect.idlType.FIXED;

        case IdlType.e_ident :
            return org.openorb.compiler.idl.reflect.idlType.IDENTIFIER;

        case IdlType.e_interface :
            return org.openorb.compiler.idl.reflect.idlType.INTERFACE;

        case IdlType.e_forward_interface :
            return org.openorb.compiler.idl.reflect.idlType.INTERFACE;

        case IdlType.e_native :
            return org.openorb.compiler.idl.reflect.idlType.NATIVE;

        case IdlType.e_operation :
            return org.openorb.compiler.idl.reflect.idlType.OPERATION;

        case IdlType.e_param :
            return org.openorb.compiler.idl.reflect.idlType.PARAM;

        case IdlType.e_sequence :
            return org.openorb.compiler.idl.reflect.idlType.SEQUENCE;

        case IdlType.e_simple :
            return org.openorb.compiler.idl.reflect.idlType.PRIMITIVE;

        case IdlType.e_typedef :
            return org.openorb.compiler.idl.reflect.idlType.TYPEDEF;

        case IdlType.e_value :
            return org.openorb.compiler.idl.reflect.idlType.VALUE;

        case IdlType.e_value_inheritance :
            return org.openorb.compiler.idl.reflect.idlType.VALUE;

        case IdlType.e_forward_value :
            return org.openorb.compiler.idl.reflect.idlType.VALUE;

        case IdlType.e_value_box :
            return org.openorb.compiler.idl.reflect.idlType.VALUEBOX;

        case IdlType.e_struct_member :
            return org.openorb.compiler.idl.reflect.idlType.STRUCT_MEMBER;

        case IdlType.e_union_member :
            return org.openorb.compiler.idl.reflect.idlType.UNION_MEMBER;

        case IdlType.e_state_member :
            return org.openorb.compiler.idl.reflect.idlType.STATE;

        default :
            return org.openorb.compiler.idl.reflect.idlType.UNKNOWN;
        }
    }


    public java.util.Enumeration content()
    {
        java.util.Vector tmp = new java.util.Vector();

        reset();

        while ( end() != true )
        {
            switch ( current().kind() )
            {

            case IdlType.e_import :

            case IdlType.e_include :
                break;

            default :
                tmp.addElement( current() );
                break;
            }

            next();
        }

        return new org.openorb.compiler.idl.reflect.idlEnumeration( tmp );
    }

    public java.util.Enumeration filter( int type )
    {
        java.util.Vector tmp = new java.util.Vector();

        reset();

        while ( end() != true )
        {
            if ( ( ( ( org.openorb.compiler.idl.reflect.idlObject ) current() ).idlType() ) == type )
                tmp.addElement( current() );

            next();
        }

        return new org.openorb.compiler.idl.reflect.idlEnumeration( tmp );
    }

    public boolean containsObject( int type )
    {
        reset();

        while ( end() != true )
        {
            if ( ( ( org.openorb.compiler.idl.reflect.idlObject ) current() ).idlType() == type )
                return true;

            next();
        }

        return false;
    }

    public int idlFinalType()
    {
        if ( idlType() == org.openorb.compiler.idl.reflect.idlType.IDENTIFIER )
            return ( ( org.openorb.compiler.idl.reflect.idlIdentifier ) this ).original().idlFinalType();

        return idlType();
    }

    public int idlConcreteType()
    {
        switch ( idlType() )
        {

        case org.openorb.compiler.idl.reflect.idlType.IDENTIFIER :
            return ( ( org.openorb.compiler.idl.reflect.idlIdentifier ) this ).original().idlConcreteType();

        case org.openorb.compiler.idl.reflect.idlType.TYPEDEF :
            return ( ( org.openorb.compiler.idl.reflect.idlTypeDef ) this ).original().idlConcreteType();

        default :
            return idlType();
        }
    }
}
