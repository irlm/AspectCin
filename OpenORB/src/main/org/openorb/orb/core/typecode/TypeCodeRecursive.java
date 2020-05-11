/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.typecode;

import java.util.Map;

import org.omg.CORBA.CompletionStatus;

/**
 * Typecode interface for recursive and typecodes. These are replaced with
 * the real typecode when the containing typecode is completed.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:48 $
 */
class TypeCodeRecursive
    extends TypeCodeBase
{
    private String m_id;

    /** Creates new RecursiveTypecode */
    TypeCodeRecursive( String id )
    {
        m_id = id;
    }

    boolean _is_recursive()
    {
        return true;
    }

    public boolean _is_compact()
    {
        return false;
    }

    public TypeCodeBase _base_type()
    {
        return this;
    }

    boolean _fix_recursive( Map recursive )
    {
        org.openorb.orb.util.Trace.signalIllegalCondition( null,
              "TypeCodeRecursive._fix_recursive: ???." );
        // never reached
        return false;
    }

    public java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_id;
    }

    // we define the hash and equals operations to be the natural ones.

    public int hashCode()
    {
        return System.identityHashCode( this );
    }

    public boolean equals( Object obj )
    {
        return this == obj;
    }

    public org.omg.CORBA.TCKind kind()
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public boolean equivalent( org.omg.CORBA.TypeCode tc )
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public boolean equal( org.omg.CORBA.TypeCode tc )
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public int member_count()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.TypeCode member_type( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.Any member_label( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public int default_index()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public java.lang.String member_name( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public java.lang.String name()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.TypeCode discriminator_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public short fixed_digits()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public short fixed_scale()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public short member_visibility( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
                org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
    }
}

