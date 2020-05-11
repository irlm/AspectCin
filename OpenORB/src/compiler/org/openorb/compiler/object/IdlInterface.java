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
 * Cette classe represente l'objet IDL Interface
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlInterface extends IdlObject implements org.openorb.compiler.idl.reflect.idlInterface
{
    /**
     * Flag qui indique si une interface est abstraite
     */
    private boolean _abstract;

    /**
     * Flag qui indique si une interface locale
     */
    private boolean _local;

    /**
    * Flag qui indique si l'interface est forwardee
    */
    private boolean _forward;

    /**
    * Flag qui indique si l'interface est deja definie
    */
    private boolean _defined;

    /**
     * Liste des interfaces dont cette interface herite
     */
    private java.util.Vector _inheritance;

    /**
     * Reference vers le veritable objet si on est un forward
     */
    private IdlInterface _real;

    /**
    * Cree un objet IDL Interface
    */
    public IdlInterface( IdlObject father )
    {
        super( IdlType.e_interface, father );
        _forward = false;
        _inheritance = new java.util.Vector();
        _abstract = false;
        _local = false;
        _has_inheritance = true;
        _is_container = true;
    }

    /**
     * Indique que l'interface est forwardee
     */
    public void forward()
    {
        _forward = true;
        _type = IdlType.e_forward_interface;
    }

    /**
     * Retourne le flag qui indique si l'interface est forwardee
     *
     * @return le flag
     */
    public boolean isForward()
    {
        return _forward;
    }

    /**
     * Fixe le fait que l'interface est abstract
     */
    public void abstract_interface( boolean value )
    {
        _abstract = value;
    }

    /**
     * Retourne le flag qui indique si l'interfac est abstract
     */
    public boolean abstract_interface()
    {
        return _abstract;
    }

    /**
     * Fixe le fait que l'interface est locale
     */
    public void local_interface( boolean value )
    {
        _local = value;
    }

    /**
     * Retourne le flag qui indique si l'interface est locale
     */
    public boolean local_interface()
    {
        return _local;
    }


    /**
     * Indique que l'interface est definie
     */
    public void defined( IdlInterface itf )
    {
        _defined = true;
        itf.setInterface( this );
    }

    /**
     * Retourne le flag qui indique si l'interface est definie
     *
     * @return le flag
     */
    public boolean isDefined()
    {
        return _defined;
    }

    /**
     * Ajoute un ancetre a la liste d'heritage
     *
     * @param obj l'objet ancetre
     */
    public void addInheritance( IdlObject obj )
    {
        _inheritance.addElement( ( Object ) obj );
    }

    /**
     * Recupere la liste des ancetres
     *
     * @return la liste des ancetres
     */
    public java.util.Vector getInheritance()
    {
        return _inheritance;
    }

    /**
     * Retourne la veritable interface ( non forward )
     *
     * @return l'interface
     */
    public IdlInterface getInterface()
    {
        return _real;
    }

    /**
     * Fixe la veritable interface ( non forward )
     *
     * @param itf la veritable interface
     */
    public void setInterface( IdlInterface itf )
    {
        _real = itf;
    }

    /**
     * This method returns an inherited object
     */
    public IdlObject returnInheritedObject( String name )
   {
       IdlObject obj = null;

       java.util.Vector inheritance = ( ( IdlInterface ) this ).getInheritance();

       for ( int i = 0; i < inheritance.size(); i++ )
       {
           if ( ( ( IdlObject ) inheritance.elementAt( i ) ).kind() == IdlType.e_forward_interface )
               obj = ( ( IdlInterface ) inheritance.elementAt( i ) ).getInterface().returnObject( name, true );
           else
               obj = ( ( IdlObject ) inheritance.elementAt( i ) ).returnObject( name, true );

           if ( obj != null )
               return obj;
       }

       return null;
   }

   /**
	* This method returns an contained object
	*/
   public IdlObject searchObject( String name )
   {
       if ( ( ( IdlInterface ) this ).getInterface() != null )
       {
           return ( ( IdlInterface ) this ).getInterface().searchObject( name );
       }

       for ( int i = 0; i < _list.size(); i++ )
       {
           if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
           {
               if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).isSame( name ) )
                   return sameAs( name );
           }
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

       public boolean isLocal()
       {
           return _local;
       }

       public org.openorb.compiler.idl.reflect.idlObject description()
       {
           return getInterface();
       }

       public org.openorb.compiler.idl.reflect.idlInterface [] inheritance()
       {
           org.openorb.compiler.idl.reflect.idlInterface [] tmp = new org.openorb.compiler.idl.reflect.idlInterface[ _inheritance.size() ];

           for ( int i = 0; i < _inheritance.size(); i++ )
               tmp[ i ] = ( org.openorb.compiler.idl.reflect.idlInterface ) _inheritance.elementAt( i );

           return tmp;
       }
   }

