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

public interface Object
{
    boolean _is_a( String repositoryIdentifier );

    boolean _is_equivalent( org.omg.CORBA.Object other );

    boolean _non_existent();

    int _hash( int maximum );

    org.omg.CORBA.Object _duplicate();

    void _release();

    /**
     * @deprecated Deprecated by CORBA 2.3
     */
    org.omg.CORBA.InterfaceDef _get_interface();

    org.omg.CORBA.Object _get_interface_def();

    Request _request( String operation );

    Request _create_request( Context ctx, String operation, NVList arg_list,
          NamedValue result );


    Request _create_request( Context ctx, String operation, NVList arg_list,
          NamedValue result, ExceptionList exclist, ContextList ctxlist );

    Policy _get_policy( int policy_type );

    DomainManager[] _get_domain_managers();

    org.omg.CORBA.Object _set_policy_override( Policy[] policies,
          SetOverrideType set_add );
}
