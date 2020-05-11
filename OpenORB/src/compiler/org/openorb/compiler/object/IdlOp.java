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
 * Cette classe represente l'objet IDL Op
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlOp extends IdlObject implements org.openorb.compiler.idl.reflect.idlOperation
{
    /**
     * Flag qui indique si l'attribut est en lecture seule
     */
    private boolean _oneway;

    /**
     * Cree un objet IDL Op
     */
    public IdlOp( IdlObject father )
    {
        super( IdlType.e_operation, father );
        _is_container = true;
    }

    /**
     * Retourne le flag oneway de l'operation
     *
     * @return le flag
     */
    public boolean oneway()
    {
        return _oneway;
    }

    /**
     * Fixe le flag oneway de l'operation
     *
     * @param rd le flag
     */
    public void oneway ( boolean one )
    {
        _oneway = one;
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

    public boolean isOneway()
    {
        return _oneway;
    }

    public org.openorb.compiler.idl.reflect.idlObject returnType()
            {
                reset();
                return current();
            }

            public org.openorb.compiler.idl.reflect.idlParameter [] parameters()
            {
                java.util.Vector tmp = new java.util.Vector();

                reset();
                next();

                while ( !end() )
                {
                    if ( current().kind() == IdlType.e_param )
                        tmp.addElement( current() );
                    else
                        break;

                    next();
                }

                org.openorb.compiler.idl.reflect.idlParameter [] params = new org.openorb.compiler.idl.reflect.idlParameter[ tmp.size() ];

                for ( int i = 0; i < tmp.size(); i++ )
                {
                    params[ i ] = ( org.openorb.compiler.idl.reflect.idlParameter ) tmp.elementAt( i );
                }

                return params;
            }

            public org.openorb.compiler.idl.reflect.idlException [] exceptions()
            {
                reset();
                next();

                while ( end() != true )
                {
                    if ( current().kind() == IdlType.e_raises )
                        break;

                    next();
                }

                if ( end() )
                    return new org.openorb.compiler.idl.reflect.idlException[ 0 ];

                IdlRaises raises = ( IdlRaises ) current();

                raises.reset();

                org.openorb.compiler.idl.reflect.idlException [] except = new org.openorb.compiler.idl.reflect.idlException[ raises.length() ];

                int i = 0;

                while ( raises.end() != true )
                {
                    except[ i++ ] = ( org.openorb.compiler.idl.reflect.idlException ) raises.current();

                    raises.next();
                }

                return except;
            }

            public String [] contexts()
            {
                reset();
                next();

                while ( end() != true )
                {
                    if ( current().kind() == IdlType.e_context )
                        break;

                    next();
                }

                if ( end() )
                    return new String[ 0 ];

                IdlContext ctx = ( IdlContext ) current();

                java.util.Vector values = ctx.getValues();

                String [] ctxs = new String[ values.size() ];

                for ( int i = 0; i < values.size(); i++ )
                {
                    ctxs[ i ] = ( String ) values.elementAt( i );
                }

                return ctxs;
            }

            public java.util.Enumeration content()
            {
                return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
            }
        }
