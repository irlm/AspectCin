/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

/*
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */

package org.omg.CORBA;

public abstract class TypeCode
    implements org.omg.CORBA.portable.IDLEntity
{
    public abstract boolean equal( org.omg.CORBA.TypeCode tc );

    public abstract boolean equivalent( org.omg.CORBA.TypeCode tc );

    public abstract org.omg.CORBA.TypeCode get_compact_typecode();

    public abstract org.omg.CORBA.TCKind kind();

    public abstract java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract java.lang.String name()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract int member_count()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract java.lang.String member_name( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds;

    public abstract org.omg.CORBA.TypeCode member_type( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds;

    public abstract org.omg.CORBA.Any member_label( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds;

    public abstract org.omg.CORBA.TypeCode discriminator_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract int default_index()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public abstract org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    public short fixed_digits()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public short fixed_scale()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public short member_visibility( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
