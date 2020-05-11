/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

import org.openorb.orb.core.typecode.TypeCodeFactoryImpl;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

/**
 * The ORB Singleton is a way to create TypeCode and Any. Any attempt to
 * invoke any other ORB method shall raise the system exception NO_IMPLEMENT.
 *
 * @author Chris Wood
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/03/27 21:11:40 $
 */
public class ORBSingleton
    extends org.omg.CORBA_2_3.ORB
    implements LogEnabled
{
    private Logger m_logger;

    /**
     * Use ORB.init() to get an instance.
     */
    public ORBSingleton()
    {
    }

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public String [] list_initial_services()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object resolve_initial_references ( String object_name )
        throws org.omg.CORBA.ORBPackage.InvalidName
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void run()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void shutdown( boolean wait_for_completion )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean work_pending()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void perform_work()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.NVList create_list( int count )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.NamedValue create_named_value ( String name,
          org.omg.CORBA.Any value, int flags )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.ExceptionList create_exception_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.ContextList create_context_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Context get_default_context()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Environment create_environment()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void connect( org.omg.CORBA.Object obj )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void disconnect( org.omg.CORBA.Object obj )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String object_to_string( org.omg.CORBA.Object obj )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object string_to_object( String str )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void send_multiple_requests_oneway ( org.omg.CORBA.Request [] req )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void send_multiple_requests_deferred ( org.omg.CORBA.Request [] req )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean poll_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request get_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    protected void set_parameters( String[] args, java.util.Properties props )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    protected void set_parameters( java.applet.Applet app, java.util.Properties props )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.ValueFactory register_value_factory ( String id,
            org.omg.CORBA.portable.ValueFactory factory )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void unregister_value_factory( String id )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.ValueFactory lookup_value_factory( String id )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_delegate( java.lang.Object wrapper )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean get_service_information( short service_type,
          org.omg.CORBA.ServiceInformationHolder service_information )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy create_policy( int policy_type, org.omg.CORBA.Any val )
        throws org.omg.CORBA.PolicyError
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Create a struct typecode
     */
    public org.omg.CORBA.TypeCode create_struct_tc( String id, String name,
          org.omg.CORBA.StructMember[] members )
    {
        return TypeCodeFactoryImpl.getInstance().create_struct_tc( id, name, members );
    }

    /**
     * Create an union typecode
     */
    public org.omg.CORBA.TypeCode create_union_tc( String id,
            String name,
            org.omg.CORBA.TypeCode discriminator_type,
            org.omg.CORBA.UnionMember[] members )
    {
        return TypeCodeFactoryImpl.getInstance().create_union_tc(
              id, name, discriminator_type, members );
    }

    /**
     * Create an enum typecode
     */
    public org.omg.CORBA.TypeCode create_enum_tc( String id,
            String name,
            String[] members )
    {
        return TypeCodeFactoryImpl.getInstance().create_enum_tc( id, name, members );
    }

    /**
     * Create an alias typecode
     */
    public org.omg.CORBA.TypeCode create_alias_tc( String id,
            String name,
            org.omg.CORBA.TypeCode original_type )
    {
        return TypeCodeFactoryImpl.getInstance().create_alias_tc( id, name, original_type );
    }

    /**
     * Create an exception typecode
     */
    public org.omg.CORBA.TypeCode create_exception_tc( String id,
            String name,
            org.omg.CORBA.StructMember[] members )
    {
        return TypeCodeFactoryImpl.getInstance().create_exception_tc( id, name, members );
    }

    /**
     * Create an interfac typecode
     */
    public org.omg.CORBA.TypeCode create_interface_tc( String id, String name )
    {
        return TypeCodeFactoryImpl.getInstance().create_interface_tc( id, name );
    }

    /**
     * Create a native typecode
     */
    public org.omg.CORBA.TypeCode create_native_tc( String id, String name )
    {
        return TypeCodeFactoryImpl.getInstance().create_native_tc( id, name );
    }


    /**
     * Create a string typecode
     */
    public org.omg.CORBA.TypeCode create_string_tc( int bound )
    {
        return TypeCodeFactoryImpl.getInstance().create_string_tc( bound );
    }

    /**
     * Create a wstring typecode
     */
    public org.omg.CORBA.TypeCode create_wstring_tc( int bound )
    {
        return TypeCodeFactoryImpl.getInstance().create_wstring_tc( bound );
    }

    /**
     * Create a sequence typecode
     */
    public org.omg.CORBA.TypeCode create_sequence_tc( int bound,
          org.omg.CORBA.TypeCode element_type )
    {
        return TypeCodeFactoryImpl.getInstance().create_sequence_tc( bound, element_type );
    }

    /**
     * Create a recursive sequence typecode
     *
     * @deprecated Deprecated by CORBA 2.3.
     */
    public org.omg.CORBA.TypeCode create_recursive_sequence_tc( int bound, int offset )
    {
        return TypeCodeFactoryImpl.getInstance().create_recursive_sequence_tc( bound, offset );
    }

    /**
     * Create a recursive member typecode
     */
    /*
    public org.omg.CORBA.TypeCode create_recursive_member( int offset )
    {
      return TypeCodeFactoryImpl.getInstance().create_recursive_member_tc(offset);
    }
    */

    /**
     * Create a recursive typecode
     */
    public org.omg.CORBA.TypeCode create_recursive_tc( String id )
    {
        return TypeCodeFactoryImpl.getInstance().create_recursive_tc( id );
    }

    /**
     * Create an array typecode
     */
    public org.omg.CORBA.TypeCode create_array_tc( int length, org.omg.CORBA.TypeCode element_type )
    {
        return TypeCodeFactoryImpl.getInstance().create_array_tc( length, element_type );
    }

    /**
     * Create a fixed typecode
     */
    public org.omg.CORBA.TypeCode create_fixed_tc( short digits, short scale )
    {
        return TypeCodeFactoryImpl.getInstance().create_fixed_tc( digits, scale );
    }

    /**
     * Create a valuetype typecode
     */
    public org.omg.CORBA.TypeCode create_value_tc( String id, String name,
            short type_modifier,
            org.omg.CORBA.TypeCode concrete_base,
            org.omg.CORBA.ValueMember [] members )
    {
        return TypeCodeFactoryImpl.getInstance().create_value_tc( id,
              name, type_modifier, concrete_base, members );
    }

    /**
     * Create a value box typecode
     */
    public org.omg.CORBA.TypeCode create_value_box_tc( String id, String name,
          org.omg.CORBA.TypeCode boxed_type )
    {
        return TypeCodeFactoryImpl.getInstance().create_value_box_tc( id, name, boxed_type );
    }

    /**
     * Create an abstract interface typecode
     */
    public org.omg.CORBA.TypeCode create_abstract_interface_tc( String id, String name )
    {
        return TypeCodeFactoryImpl.getInstance().create_abstract_interface_tc( id, name );
    }

    /**
     * Create a local interface typecode
     */
    public org.omg.CORBA.TypeCode create_local_interface_tc( String id, String name )
    {
        return TypeCodeFactoryImpl.getInstance().create_local_interface_tc( id, name );
    }

    /**
     * This operation returns a primitive typecode from the corresponding TC kind.
     */
    public org.omg.CORBA.TypeCode get_primitive_tc( org.omg.CORBA.TCKind tcKind )
    {
        return TypeCodeFactoryImpl.getInstance().get_primitive_tc( tcKind );
    }

    /**
     * Create an any
     */
    public org.omg.CORBA.Any create_any()
    {
        return new org.openorb.orb.core.Any( this );
    }
}

