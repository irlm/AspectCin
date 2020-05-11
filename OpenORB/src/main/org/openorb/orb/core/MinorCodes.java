/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

/**
 * This lists all the minor codes used throughout the openorb core.
 *
 * @author Chris Wood
 */
public final class MinorCodes
{
    public static final int BASE_VALUE = org.openorb.orb.policy.OPENORB_VPVID.value | 0x000;

    /** Attempt to access incomplete typecode containing recursive */
    public static final int BAD_INV_ORDER_TYPECODE = BASE_VALUE + 1;
    /** Invocation order when using DSI */
    public static final int BAD_INV_ORDER_NOT_STREAMABLE = BASE_VALUE + 2;
    /** Invocation order when using streaming stubs (Delegate) */
    public static final int BAD_INV_ORDER_DELEGATE = BASE_VALUE + 3;
    /** Orb is not initialized */
    public static final int BAD_INV_ORDER_ORB = BASE_VALUE + 4;
    /** Server invocation order */
    public static final int BAD_INV_ORDER_SERVER = BASE_VALUE + 5;

    /** Attempt to extract wrong type from any */
    public static final int BAD_OPERATION_ANY_TYPE = BASE_VALUE + 6;

    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_OBJ_CLASS = BASE_VALUE + 7;
    /** Type mismatch in list streams with fixed type */
    public static final int BAD_PARAM_FIXED_TYPE = BASE_VALUE + 8;
    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_VALUE_CLASS = BASE_VALUE + 9;
    /** Attempt to insert value into any with incorrect typecode */
    public static final int BAD_PARAM_VALUE_TYPE = BASE_VALUE + 10;
    /** Array index out of bounds */
    public static final int BAD_PARAM_ARRAY_INDEX = BASE_VALUE + 11;
    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_ABSTRACT_CLASS = BASE_VALUE + 12;
    /** No primitive typecode of that kind */
    public static final int BAD_PARAM_PRIMITIVE_KIND = BASE_VALUE + 13;

    /** Object class cannot be instantiated or is incorrect type */
    public static final int INV_POLICY_MERGE_FAILED = BASE_VALUE + 14;

    /** Unable to find interface repository */
    public static final int INF_REPOS_FIND = BASE_VALUE + 15;
    /** Unable to find interface in interface repository */
    public static final int INF_REPOS_LOOKUP = BASE_VALUE + 16;
    /** Unable interface from repository is the wrong type */
    public static final int INF_REPOS_TYPE = BASE_VALUE + 17;

    /** Sequence length exceeds limit in typecode */
    public static final int MARSHAL_SEQ_BOUND = BASE_VALUE + 18;
    /** Type mismatch for list stream */
    public static final int MARSHAL_TYPE_MISMATCH = BASE_VALUE + 20;
    /** Bounds mismatch for list stream */
    public static final int MARSHAL_BOUNDS_MISMATCH = BASE_VALUE + 21;
    /** Buffer overread */
    public static final int MARSHAL_BUFFER_OVERREAD = BASE_VALUE + 22;
    /** Buffer underread */
    public static final int MARSHAL_BUFFER_UNDERREAD = BASE_VALUE + 23;
    /** Invalid buffer position or format. */
    public static final int MARSHAL_BUFFER_POS = BASE_VALUE + 24;
    /** Attempt to insert native type into any */
    public static final int MARSHAL_NATIVE = BASE_VALUE + 25;
    /** Problem with union discriminator */
    public static final int MARSHAL_UNION_DISC = BASE_VALUE + 26;
    /** Unable to extract valuebox type from any, missing helper */
    public static final int MARSHAL_VALUEBOX_HELPER = BASE_VALUE + 27;
    /** Unreported exception occurred during marshalling a request.
      * This is reported to interceptors, the client's exception may differ. */
    public static final int MARSHAL_REQUEST_UNKNOWN = BASE_VALUE + 28;
    /** Unreported exception occurred during marshalling or the reply buffer is
      * underread. The exception causing this problem is overwritten by this one. */
    public static final int MARSHAL_REPLY_UNKNOWN_OR_UNDERREAD = BASE_VALUE + 29;

    // Do not instantiate
    private MinorCodes()
    {
    }
}
