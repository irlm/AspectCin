/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by all objects that represent a primitive type
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlPrimitive extends idlObject
{
    public static final int VOID = 0;

    public static final int BOOLEAN = 1;

    public static final int FLOAT = 2;

    public static final int DOUBLE = 3;

    public static final int LONGDOUBLE = 4;

    public static final int SHORT = 5;

    public static final int USHORT = 6;

    public static final int LONG = 7;

    public static final int ULONG = 8;

    public static final int LONGLONG = 9;

    public static final int ULONGLONG = 10;

    public static final int CHAR = 11;

    public static final int WCHAR = 12;

    public static final int OCTET = 13;

    public static final int OBJECT = 14;

    public static final int ANY = 15;

    public static final int TYPECODE = 16;

    public static final int VALUEBASE = 17;

    /**
     * Return the primitive type
     */
    public int primitive();
}
