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
 * This class represents the IDL Array object
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlArray extends IdlObject implements org.openorb.compiler.idl.reflect.idlArray
{
    /**
     * Array size
     */
    public int dimension;

    /**
     * Creates an IDL Array object
     */
    public IdlArray( IdlObject father )
    {
        super( IdlType.e_array, father );
    }

    /**
     * Returns the array size
     *
     * @return the size
     */
    public int getDimension()
    {
        return dimension;
    }

    /**
     * Set the array size
     *
     * @param dims the size
     */
    public void setDimension( int dims )
    {
        dimension = dims;
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

    public int [] dimensions()
    {
        java.util.Vector tmp = new java.util.Vector();
        IdlObject obj = this;

        while ( true )
        {
            tmp.addElement( new Integer( ( ( IdlArray ) obj ).getDimension() ) );

            obj.reset();

            if ( obj.current().kind() != IdlType.e_array )
                break;

            obj = obj.current();
        }

        int [] dims = new int[ tmp.size() ];

        for ( int i = 0; i < tmp.size(); i++ )
            dims[ i ] = ( ( Integer ) tmp.elementAt( i ) ).intValue();

        return dims;
    }

    public org.openorb.compiler.idl.reflect.idlObject internal()
    {
        IdlObject obj = this;

        while ( true )
        {
            obj.reset();

            if ( obj.current().kind() != IdlType.e_array )
                break;

            obj = obj.current();
        }

        return obj.current();
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}

