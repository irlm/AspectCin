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

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;

public class LocalObject
    implements org.omg.CORBA.Object
{
    public LocalObject()
    {
    }

    public boolean _is_equivalent( org.omg.CORBA.Object that )
    {
        return equals( that );
    }

    public boolean _non_existent()
    {
        return false;
    }

    public int _hash( int maximum )
    {
        return hashCode() % ( maximum + 1 );
    }

    public boolean _is_a( String identifier )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _duplicate()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void _release()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Request _request( String operation )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Request _create_request( Context ctx,
                                    String operation,
                                    NVList arg_list,
                                    NamedValue result )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Request _create_request( Context ctx,
                                    String operation,
                                    NVList arg_list,
                                    NamedValue result,
                                    ExceptionList exceptions,
                                    ContextList contexts )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @deprecated Deprecated by CORBA 2.3.
     */
    public org.omg.CORBA.InterfaceDef _get_interface()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _get_interface_def()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ORB _orb()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Policy _get_policy( int policy_type )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public DomainManager[] _get_domain_managers()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _set_policy_override( Policy[] policies,
            SetOverrideType set_add )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean _is_local()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ServantObject _servant_preinvoke( String operation,
            Class expectedType )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void _servant_postinvoke( ServantObject servant )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public OutputStream _request( String operation,
                                  boolean responseExpected )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public InputStream _invoke( OutputStream output )
        throws ApplicationException, RemarshalException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void _releaseReply( InputStream input )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean validate_connection()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
