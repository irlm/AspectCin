/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

/**
 * This interface lists all the minor codes used througout the IIOP module.
 *
 * @author Chris Wood
 */
public final class IIOPMinorCodes
{
    public static final int BASE_VALUE = org.openorb.orb.policy.OPENORB_VPVID.value | 0x100;

    /** IIOP version does not support primitive */
    public static final int BAD_OPERATION_IIOP_VERSION = BASE_VALUE + 1;

    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_OBJ_CLASS = BASE_VALUE + 2;
    /** Typecode is not fixed typecode */
    public static final int BAD_PARAM_FIXED_TYPE = BASE_VALUE + 3;
    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_VALUE_CLASS = BASE_VALUE + 4;
    /** Null valued strings cannot be transmitted */
    public static final int BAD_PARAM_NULL_STRING = BASE_VALUE + 5;
    /** Array index out of bounds */
    public static final int BAD_PARAM_ARRAY_INDEX = BASE_VALUE + 6;
    /** Object class cannot be instantiated or is incorrect type */
    public static final int BAD_PARAM_ABSTRACT_CLASS = BASE_VALUE + 7;

    /** Connection to client has been lost before reply can be sent */
    public static final int COMM_FAILURE_CLIENT_DIED = BASE_VALUE + 8;
    /** No route to server */
    public static final int COMM_FAILURE_NO_ROUTE = BASE_VALUE + 9;
    /** No connection to server, server is not listening or connection refused */
    public static final int COMM_FAILURE_NO_CONNECT = BASE_VALUE + 10;
    /** Unable to find host in DNS */
    public static final int COMM_FAILURE_HOST_NOT_FOUND = BASE_VALUE + 11;
    /** IOException occoured during read */
    public static final int COMM_FAILURE_IO_EXCEPTION = BASE_VALUE + 12;
    /** Unexpected end of stream during read */
    public static final int COMM_FAILURE_EOF = BASE_VALUE + 13;
    /** Broken data during read */
    public static final int COMM_FAILURE_BAD_DATA = BASE_VALUE + 14;
    /** Message error. Remote server detected a broken OpenORB */
    public static final int COMM_FAILURE_MSG_ERROR = BASE_VALUE + 15;

    /** Invalid tag for IIOP profile */
    public static final int INV_OBJREF_BAD_TAG = BASE_VALUE + 12;
    /** IIOP profile data is corrupted */
    public static final int INV_OBJREF_BAD_PROFILE = BASE_VALUE + 13;
    /** Component data is corrupted */
    public static final int INV_OBJREF_BAD_COMPONENT = BASE_VALUE + 14;
    /** Component data is corrupted */
    public static final int INV_OBJREF_MISSING_ENCODER = BASE_VALUE + 15;

    /** Problem with marshaling / unmarshalling char data */
    public static final int MARSHAL_CHAR = BASE_VALUE + 16;
    /** Problem with marshaling / unmarshalling wchar data */
    public static final int MARSHAL_WCHAR = BASE_VALUE + 17;
    /** Recursive typecode offset does not match any known typecode */
    public static final int MARSHAL_TC_OFFSET = BASE_VALUE + 18;
    /** Typecode kind unknown */
    public static final int MARSHAL_TC_KIND = BASE_VALUE + 19;
    /** Problem with fixed type */
    public static final int MARSHAL_FIXED = BASE_VALUE + 20;
    /** Problem with valuetype encoding */
    public static final int MARSHAL_VALUE = BASE_VALUE + 21;
    /** Failed to close encapsulation layer before calling close operation */
    public static final int MARSHAL_ENCAPS = BASE_VALUE + 22;

    /** Problem with valuetype encoding */
    public static final int NO_RESOURCES_STACK_OVERFLOW = BASE_VALUE + 23;

    /** The port published in the IOR was 0. This is probably a bidir only target */
    public static final int NO_PERMISSION_INVALID_PORT = BASE_VALUE + 24;

    // do not instantiate
    private IIOPMinorCodes()
    {
    }
}
