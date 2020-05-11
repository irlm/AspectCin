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
 * This class represents an IDL Value object
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlValue extends IdlObject implements org.openorb.compiler.idl.reflect.idlValue
{
    /**
     * Indicates if the valuetype is forward
     */
    private boolean _forward;

    /**
     * Indicates if the valuetype is custom
     */
    private boolean _custom;

    /**
     * Indicates if the ValueType is abstract
     */
    private boolean _abstract;

    /**
     * Reference to the supported interfaces
     */
    private java.util.Vector _supports;

    /**
     * Reference to the the value type definition
     */
    private IdlValue _value;

    /**
     * Reference to the inherited values
     */
    private java.util.Vector _inheritance;

    /**
     * Creates an IDL Value object
     */
    public IdlValue( IdlObject father )
    {
        super( IdlType.e_value, father );
        _abstract = false;
        _custom = false;
        _forward = false;
        _inheritance = new java.util.Vector();
        _supports = new java.util.Vector();
        _has_inheritance = true;
        _is_container = true;
    }

    /**
     * Set is abstract
     */
    public void abstract_value( boolean value )
    {
        _abstract = value;
    }

    /**
     * Is abstract
     */
    public boolean abstract_value()
    {
        return _abstract;
    }

    /**
     * Set is custom
     */
    public void custom_value( boolean value )
    {
        _custom = value;
    }

    /**
     * Returns the definition of the value
     */
    public IdlValue definedValue()
    {
        return _value;
    }

    /**
     * Set the value definition
     */
    public void definedValue( IdlValue value )
    {
        _value = value;
    }

    /**
     * Add an inherited
     */
    public void addInheritance( IdlValueInheritance value )
    {
        _inheritance.addElement( value );
    }

    /**
     * Return the inherited
     */
    public java.util.Vector getInheritanceList()
    {
        return _inheritance;
    }

    /**
     * Return inherited as IdlValue objects
     */
    public IdlValue [] getInheritance()
    {
        IdlValue [] tab = new IdlValue[ _inheritance.size() ];

        for ( int i = 0; i < _inheritance.size(); i++ )
            tab[ i ] = ( ( IdlValueInheritance ) _inheritance.elementAt( i ) ) .getValue();

        return tab;
    }

    /**
     * Return is forward
     */
    public boolean custom_value()
    {
        return _custom;
    }

    /**
     * Set is forward
     */
    public void forward( boolean value )
    {
        if ( value )
            _type = IdlType.e_forward_value;
        else
            _type = IdlType.e_value;

        _forward = value;
    }

    /**
     * Is forward
     */
    public boolean forward()
    {
        return _forward;
    }

    /**
     * Set the supported interfaces
     */
    public void supports( java.util.Vector list_obj )
    {
        _supports = list_obj;
    }

    /**
     * Return the supported interfaces
     */
    public java.util.Vector supports()
    {
        return _supports;
    }

    /**
     * Return the truncatables list
     */
    public String [] truncatableList()
    {
        String [] ret = null;
        String [] tmp = null;

        /*
          if ( _abstract )
         return new String[0];
        */

        IdlValue [] list = getInheritance();

        if ( list.length != 0 )
        {

            tmp = ( ( IdlValue ) list[ 0 ] ).truncatableList();

            ret = new String[ tmp.length + 1 ];

            ret[ 0 ] = getId();

            for ( int i = 0; i < tmp.length; i++ )
                ret[ i + 1 ] = tmp[ i ];
        }
        else
        {
            ret = new String[ 1 ];
            ret[ 0 ] = getId();
        }


        return ret;
    }

    /**
     * This method returns an inherited object
     */
    public IdlObject returnInheritedObject( String name )
                           {
                               IdlObject obj = null;
                               IdlValue [] inheritance = ( ( IdlValue ) this ).getInheritance();
                               for ( int i = 0; i < inheritance.length; i++ )
                               {
                                   if ( inheritance[ i ].kind() == IdlType.e_forward_value )
                                   {
                                       obj = inheritance[ i ].definedValue().returnObject( name, false );
                                   }
                                   else
                                   {
                                       obj = inheritance[ i ].returnObject( name, false );
                                   }
                                   if ( obj != null )
                                   {
                                       return obj;
                                   }
                               }

                               return null;
                           }

                           /**
                            * This method returns an contained object
                            */
                           public IdlObject searchObject( String name )
                           {
                               if ( definedValue() != null )
                               {
                                   return definedValue().searchObject( name );
                               }

                               for ( int i = 0; i < _list.size(); i++ )
                               {
                                   if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
                                       if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name().equals( name ) )
                                           return ( IdlObject ) ( _list.elementAt( i ) );
                               }

                               return null;
                           }

                           // ------------------------------------------------------------------------------------------
                           // IDL Reflection
                           // ------------------------------------------------------------------------------------------

                           public boolean isAbstract()
                           {
                               return _abstract;
                           }

                           public boolean isCustom()
                           {
                               return _custom;
                           }

                           public boolean isForward()
                           {
                               return _forward;
                           }

                           public org.openorb.compiler.idl.reflect.idlValue description()
                           {
                               return _value;
                           }

                           public boolean isTruncatable()
                           {
                               java.util.Vector inh = getInheritanceList();

                               for ( int i = 0; i < inh.size(); i++ )
                               {
                                   if ( ( ( IdlValueInheritance ) inh.elementAt( i ) ).truncatable_member() )
                                       return true;
                               }

                               return false;
                           }

                           public org.openorb.compiler.idl.reflect.idlValue concrete()
                           {
                               IdlValue [] inh = getInheritance();

                               if ( inh[ 0 ].isAbstract() )
                                   return null;

                               return inh[ 0 ];
                           }

                           public org.openorb.compiler.idl.reflect.idlValue [] inheritance()
                           {
                               org.openorb.compiler.idl.reflect.idlValue [] list = new org.openorb.compiler.idl.reflect.idlValue [ 0 ];

                               int begin = 0;
                               IdlValue [] inh = getInheritance();

                               if ( inh.length == 0 )
                                   return list;

                               if ( inh[ 0 ].isAbstract() )
                               {
                                   list = new org.openorb.compiler.idl.reflect.idlValue[ inh.length ];

                                   begin = 0;
                               }
                               else
                               {
                                   list = new org.openorb.compiler.idl.reflect.idlValue[ inh.length - 1 ];

                                   begin = 1;
                               }

                               int j = 0;

                               for ( int i = begin; i < inh.length; i++ )
                               {
                                   list[ j++ ] = inh[ i ];
                               }

                               return list;
                           }

                           public org.openorb.compiler.idl.reflect.idlInterface [] supported()
                           {
                               org.openorb.compiler.idl.reflect.idlInterface [] tmp = new org.openorb.compiler.idl.reflect.idlInterface[ _supports.size() ];

                               for ( int i = 0; i < _supports.size(); i++ )
                               {
                                   tmp[ i ] = ( org.openorb.compiler.idl.reflect.idlInterface ) _supports.elementAt( i );
                               }

                               return tmp;
                           }
                       }
