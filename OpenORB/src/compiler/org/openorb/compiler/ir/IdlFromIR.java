/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.ir;

import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.object.IdlArray;
import org.openorb.compiler.object.IdlAttribute;
import org.openorb.compiler.object.IdlConst;
import org.openorb.compiler.object.IdlContext;
import org.openorb.compiler.object.IdlEnum;
import org.openorb.compiler.object.IdlEnumMember;
import org.openorb.compiler.object.IdlExcept;
import org.openorb.compiler.object.IdlFactory;
import org.openorb.compiler.object.IdlFactoryMember;
import org.openorb.compiler.object.IdlIdent;
import org.openorb.compiler.object.IdlInterface;
import org.openorb.compiler.object.IdlModule;
import org.openorb.compiler.object.IdlNative;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlOp;
import org.openorb.compiler.object.IdlParam;
import org.openorb.compiler.object.IdlRaises;
import org.openorb.compiler.object.IdlSequence;
import org.openorb.compiler.object.IdlSimple;
import org.openorb.compiler.object.IdlStateMember;
import org.openorb.compiler.object.IdlString;
import org.openorb.compiler.object.IdlStruct;
import org.openorb.compiler.object.IdlStructMember;
import org.openorb.compiler.object.IdlTypeDef;
import org.openorb.compiler.object.IdlUnion;
import org.openorb.compiler.object.IdlUnionMember;
import org.openorb.compiler.object.IdlValue;
import org.openorb.compiler.object.IdlValueBox;
import org.openorb.compiler.object.IdlValueInheritance;
import org.openorb.compiler.object.IdlWString;
import org.openorb.compiler.parser.CompilationException;
import org.openorb.compiler.parser.IdlParser;

/**
 * This class provides an import mechanism to get IDL descriptions from Interface Repository.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2005/03/13 12:59:56 $
 */

public class IdlFromIR implements IRImport
{
    private java.util.Hashtable _imported = new java.util.Hashtable();

    private boolean _initialized;

    private org.omg.CORBA.Repository _ir;

    private IdlParser _parser;

    private org.omg.CORBA.ORB _orb;

    private CompilerProperties m_cp = null;

    /**
     * Constructor
     */
    public IdlFromIR( CompilerProperties cp )
    {
        _initialized = false;
        m_cp = cp;
        _ir = null;
        _orb = org.omg.CORBA.ORB.init( new String[ 0 ], null );
    }

    /**
     * Constructor
     */
    public IdlFromIR( CompilerProperties cp, org.omg.CORBA.ORB orb )
    {
        _initialized = false;
        m_cp = cp;
        _ir = null;
        _orb = orb;
    }

    /**
     * Set the parser
     */
    public void set_parser( IdlParser parser )
    {
        _parser = parser;
    }

    /**
     * Initialize ( not required if normal use : getDescriptionFromIR )
     */
    public void initialize()
    {
        init();
    }

    /**
     * This method is called from the IDL parser to get an IR container description.
     */
    public void getDescriptionFromIR( String scope_name, IdlObject current_scope )
    {
        if ( isAlreadyImported( scope_name ) )
            return;

        if ( checkFromImportFromFile( scope_name ) )
            return;

        org.omg.CORBA.Container _scope = null;

        init();

        if ( scope_name.startsWith( "::" ) )
        {
            _scope = org.omg.CORBA.ContainerHelper.narrow( _ir );
        }
        else
        {
            String repId = current_scope.getId();

            if ( repId.equals( "IDL:" ) )
            {
                _scope = org.omg.CORBA.ContainerHelper.narrow( _ir );
            }
            else
            {
                org.omg.CORBA.Contained c = _ir.lookup_id( repId );

                if ( c == null )
                {
                    _parser.show_error( "Unable to find the IR description for container : " + current_scope.name() );
                    return;
                }

                try
                {
                    _scope = org.omg.CORBA.ContainerHelper.narrow( c );
                }
                catch ( org.omg.CORBA.BAD_PARAM ex )
                {
                    _parser.show_error( "Bad current scope : " + current_scope.name() );
                    return;
                }
            }
        }

        org.omg.CORBA.Contained contained = _scope.lookup( scope_name );

        if ( contained == null )
        {
            _parser.show_error( "Unable to find into IR : " + scope_name );
            return;
        }

        org.omg.CORBA.Container container = null;

        try
        {
            container = org.omg.CORBA.ContainerHelper.narrow( contained );
        }
        catch ( org.omg.CORBA.BAD_PARAM ex )
        {
            _parser.show_error( "Bad imported scope : " + current_scope.name() );
            return;
        }

        IdlObject begin_scope = createOrFindScope( scope_name, current_scope, _scope );

        irContainer( container, begin_scope, true );

        addAsImported( scope_name );
    }

    /**
     * Initialization : Get IR access
     */
    private void init()
    {
        if ( ! _initialized )
        {

            try
            {
                if ( _orb == null )
                    return;

                org.omg.CORBA.Object obj = _orb.resolve_initial_references( "InterfaceRepository" );

                _ir = org.omg.CORBA.RepositoryHelper.narrow( obj );
            }
            catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
            {
                throw new CompilationException( "Unable to get access to the 'Interface Repository'" );
            }
            catch ( org.omg.CORBA.COMM_FAILURE cf )
            {
                System.out.println( "COMM_FAILURE exception" );
                return;
            }

            if ( _ir == null )
                throw new CompilationException( "Unable to get access to the 'Interface Repository'" );
        }
    }

    /**
     * Add an IR description
     */
    public IdlObject irDescription( org.omg.CORBA.IRObject type, IdlObject scope, boolean visible )
    {
        switch ( type.def_kind().value() )
        {

        case org.omg.CORBA.DefinitionKind._dk_Attribute :

        case org.omg.CORBA.DefinitionKind._dk_Operation :

        case org.omg.CORBA.DefinitionKind._dk_ValueMember :

        case org.omg.CORBA.DefinitionKind._dk_Constant :

        case org.omg.CORBA.DefinitionKind._dk_Alias :

        case org.omg.CORBA.DefinitionKind._dk_Enum :

        case org.omg.CORBA.DefinitionKind._dk_ValueBox :

        case org.omg.CORBA.DefinitionKind._dk_Native :
            // IR Contained
            org.omg.CORBA.Contained contained = org.omg.CORBA.ContainedHelper.narrow( type );

            return irContained( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Exception :

        case org.omg.CORBA.DefinitionKind._dk_Interface :

        case org.omg.CORBA.DefinitionKind._dk_Module :

        case org.omg.CORBA.DefinitionKind._dk_Struct :

        case org.omg.CORBA.DefinitionKind._dk_Union :

        case org.omg.CORBA.DefinitionKind._dk_Value :
            // IR Container
            org.omg.CORBA.Container container = org.omg.CORBA.ContainerHelper.narrow( type );

            return irContainer( container, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Primitive :
            return irPrimitive( type, scope );

        case org.omg.CORBA.DefinitionKind._dk_String :
            return irString( type, scope );

        case org.omg.CORBA.DefinitionKind._dk_Sequence :
            return irSequence( type, scope );

        case org.omg.CORBA.DefinitionKind._dk_Array :
            return irArray( type, scope );

        case org.omg.CORBA.DefinitionKind._dk_Wstring :
            return irWstring( type, scope );
        }

        return null;
    }

    /**
     * Add a container description
     */
    public IdlObject irContainer( org.omg.CORBA.Container container, IdlObject scope, boolean visible )
    {
        switch ( container.def_kind().value() )
        {

        case org.omg.CORBA.DefinitionKind._dk_Module :
            return irModule( container, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Struct :
            return irStruct( container, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Exception :
            return irException( container, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Union :
            return irUnion( container, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Interface :
            return irInterface( container, scope, visible, 0 );

        case org.omg.CORBA.DefinitionKind._dk_LocalInterface :
            return irInterface( container, scope, visible, 1 );

        case org.omg.CORBA.DefinitionKind._dk_AbstractInterface :
            return irInterface( container, scope, visible, 2 );

        case org.omg.CORBA.DefinitionKind._dk_Value :
            return irValue( container, scope, visible );
        }

        return null;
    }

    /**
     * Add a contained description
     */
    public IdlObject irContained( org.omg.CORBA.Contained contained, IdlObject scope , boolean visible )
    {
        switch ( contained.def_kind().value() )
        {

        case org.omg.CORBA.DefinitionKind._dk_Enum :
            return irEnum( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Native :
            return irNative( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Alias :
            return irAlias( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_ValueBox :
            return irValueBox( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Constant :
            return irConstant( contained, scope, visible );

        case org.omg.CORBA.DefinitionKind._dk_Attribute :
            return irAttribute( contained, scope );

        case org.omg.CORBA.DefinitionKind._dk_Operation :
            return irOperation( contained, scope );

        case org.omg.CORBA.DefinitionKind._dk_ValueMember :
            return irValueMember( contained, scope );
        }

        return null;
    }

    /**
     * Add a module description
     */
    public IdlObject irModule( org.omg.CORBA.Container container, IdlObject scope, boolean content )
    {
        IdlModule idl_module = null;
        org.omg.CORBA.ModuleDef ir_module = org.omg.CORBA.ModuleDefHelper.narrow( container );

        IdlObject exist = scope.returnObject( ir_module.name(), true );

        if ( exist == null )
        {
            idl_module = new IdlModule( scope );

            scope.addIdlObject( idl_module );

            if ( !m_cp.getM_map_all() )
                idl_module._map = true;

            idl_module.name( ir_module.name() );
        }
        else
        {
            idl_module = ( IdlModule ) exist;

            if ( !content )
                return idl_module;
        }

        if ( content )
        {
            idl_module._import = true;

            org.omg.CORBA.Contained [] contenu = ir_module.contents( org.omg.CORBA.DefinitionKind.dk_all, true );

            for ( int i = 0; i < contenu.length; i++ )
            {
                irDescription( contenu[ i ], idl_module, content );
            }
        }
        else
            idl_module._import = false;

        return idl_module;
    }

    /**
     * Add an interface description
     * type : 0 = NORMAL, 1 = LOCAL, 2 = ABSTRACT
     */
    public IdlObject irInterface( org.omg.CORBA.Container container, IdlObject scope, boolean content, int type )
    {
        org.omg.CORBA.InterfaceDef ir_interface = org.omg.CORBA.InterfaceDefHelper.narrow( container );

        IdlInterface idl_interface = null;

        IdlObject exist = scope.returnObject( ir_interface.name(), true );

        if ( exist == null )
        {
            idl_interface = new IdlInterface( scope );

            if ( !m_cp.getM_map_all() )
                idl_interface._map = true;
        }
        else
        {
            idl_interface = ( IdlInterface ) exist;

            if ( idl_interface._import )
                return idl_interface;
        }

        idl_interface.name( ir_interface.name() );

        switch ( type )
        {

        case 1 :
            idl_interface.local_interface( true );
            break;

        case 2 :
            idl_interface.abstract_interface( true );
            break;
        }

        if ( content )
        {
            idl_interface._import = true;

            org.omg.CORBA.InterfaceDef [] bases = ir_interface.base_interfaces();

            for ( int i = 0; i < bases.length; i++ )
            {
                idl_interface.addInheritance( getDescription( bases[ i ] ) );
            }

            org.omg.CORBA.Contained [] contenu = ir_interface.contents( org.omg.CORBA.DefinitionKind.dk_all, true );

            for ( int i = 0; i < contenu.length; i++ )
            {
                irDescription( contenu[ i ], idl_interface, content );
            }
        }
        else
            idl_interface._import = false;

        scope.addIdlObject( idl_interface );

        if ( !m_cp.getM_map_all() )
            idl_interface._map = true;

        return idl_interface;
    }

    /**
     * Add a value description
     */
    public IdlObject irValue( org.omg.CORBA.Container container, IdlObject scope, boolean content )
    {
        org.omg.CORBA.ValueDef ir_value = org.omg.CORBA.ValueDefHelper.narrow( container );

        IdlValue idl_value = null;
        IdlValueInheritance inheritance = null;

        IdlObject exist = scope.returnObject( ir_value.name(), true );

        if ( exist == null )
        {
            idl_value = new IdlValue( scope );

            if ( !m_cp.getM_map_all() )
                idl_value._map = true;
        }
        else
        {
            idl_value = ( IdlValue ) exist;

            if ( idl_value._import )
                return idl_value;
        }

        idl_value.name( ir_value.name() );

        if ( content )
        {
            idl_value._import = true;

            idl_value.abstract_value( ir_value.is_abstract() );
            idl_value.custom_value( ir_value.is_custom() );

            org.omg.CORBA.ValueDef concrete = ir_value.base_value();

            if ( concrete != null )
            {
                inheritance = new IdlValueInheritance( idl_value );
                inheritance.truncatable_member( ir_value.is_truncatable() );
                inheritance.addIdlObject( getDescription( concrete ) );

                idl_value.addInheritance( inheritance );
            }

            org.omg.CORBA.ValueDef [] bases = ir_value.abstract_base_values();

            for ( int i = 0; i < bases.length; i++ )
            {
                inheritance = new IdlValueInheritance( idl_value );
                inheritance.addIdlObject( getDescription( bases[ i ] ) );

                idl_value.addInheritance( inheritance );
            }

            java.util.Vector supports_list = new java.util.Vector();

            org.omg.CORBA.InterfaceDef [] supported = ir_value.supported_interfaces();

            for ( int i = 0; i < supported.length; i++ )
                supports_list.addElement( getDescription( supported[ i ] ) );

            idl_value.supports( supports_list );

            org.omg.CORBA.Initializer [] factories = ir_value.initializers();

            irFactory( factories, idl_value );

            org.omg.CORBA.Contained [] contenu = ir_value.contents( org.omg.CORBA.DefinitionKind.dk_all, true );

            for ( int i = 0; i < contenu.length; i++ )
            {
                irDescription( contenu[ i ], idl_value, content );
            }
        }
        else
            idl_value._import = false;

        scope.addIdlObject( idl_value );

        if ( !m_cp.getM_map_all() )
            idl_value._map = true;

        return idl_value;
    }

    /**
     * Add a struct description
     */
    public IdlObject irStruct( org.omg.CORBA.Container container, IdlObject scope, boolean content )
    {
        IdlStructMember member = null;
        org.omg.CORBA.StructDef ir_struct = org.omg.CORBA.StructDefHelper.narrow( container );

        IdlStruct idl_struct = null;

        IdlObject exist = scope.returnObject( ir_struct.name(), true );

        if ( exist == null )
        {
            idl_struct = new IdlStruct( scope );

            if ( !m_cp.getM_map_all() )
                idl_struct._map = true;
        }
        else
        {
            idl_struct = ( IdlStruct ) exist;

            if ( idl_struct._import )
                return idl_struct;
        }

        idl_struct.name( ir_struct.name() );

        if ( content )
        {
            org.omg.CORBA.StructMember [] members = ir_struct.members();

            for ( int i = 0; i < members.length; i++ )
            {
                member = new IdlStructMember( idl_struct );

                member.name( members[ i ].name );
                member.type( getType( members[ i ].type_def, idl_struct ) );

                idl_struct.addIdlObject( member );
            }
        }
        else
            idl_struct._import = false;

        scope.addIdlObject( idl_struct );

        if ( !m_cp.getM_map_all() )
            idl_struct._map = true;

        return idl_struct;
    }

    /**
     * Add an union description
     */
    public IdlObject irUnion( org.omg.CORBA.Container container, IdlObject scope, boolean content )
    {
        IdlUnionMember member = null;
        org.omg.CORBA.UnionDef ir_union = org.omg.CORBA.UnionDefHelper.narrow( container );

        IdlUnion idl_union = null;

        IdlObject exist = scope.returnObject( ir_union.name(), true );

        if ( exist == null )
        {
            idl_union = new IdlUnion( scope );

            if ( !m_cp.getM_map_all() )
                idl_union._map = true;
        }
        else
        {
            idl_union = ( IdlUnion ) exist;

            if ( idl_union._import )
                return idl_union;
        }

        idl_union.name( ir_union.name() );

        if ( content )
        {
            org.omg.CORBA.UnionMember [] members = ir_union.members();

            // Add first discriminator
            member = new IdlUnionMember( idl_union );
            member.name( "__d" );
            member.addIdlObject( getType( ir_union.discriminator_type_def(), idl_union ) );
            idl_union.addIdlObject( member );

            for ( int i = 0; i < members.length; i++ )
            {
                member = new IdlUnionMember( idl_union );

                member.name( members[ i ].name );
                member.type( getType( members[ i ].type_def, idl_union ) );
                member.setValue( getValue( members[ i ].label ) );
                member.setExpression( getExpression( members[ i ].label, ir_union.discriminator_type() ) );

                idl_union.addIdlObject( member );
            }
        }
        else
            idl_union._import = false;

        scope.addIdlObject( idl_union );

        if ( !m_cp.getM_map_all() )
            idl_union._map = true;

        return idl_union;
    }

    /**
     * Add an exception description
     */
    public IdlObject irException( org.omg.CORBA.Container container, IdlObject scope, boolean content )
    {
        IdlStructMember member = null;
        org.omg.CORBA.ExceptionDef ir_exception = org.omg.CORBA.ExceptionDefHelper.narrow( container );

        IdlExcept idl_exception = null;

        IdlObject exist = scope.returnObject( ir_exception.name(), true );

        if ( exist == null )
        {
            idl_exception = new IdlExcept( scope );

            if ( !m_cp.getM_map_all() )
                idl_exception._map = true;
        }
        else
        {
            idl_exception = ( IdlExcept ) exist;

            if ( idl_exception._import )
                return idl_exception;
        }

        idl_exception.name( ir_exception.name() );

        if ( content )
        {
            org.omg.CORBA.StructMember [] members = ir_exception.members();

            for ( int i = 0; i < members.length; i++ )
            {
                member = new IdlStructMember( idl_exception );

                member.name( members[ i ].name );
                member.type( getType( members[ i ].type_def, idl_exception ) );

                idl_exception.addIdlObject( member );
            }
        }
        else
            idl_exception._import = false;

        scope.addIdlObject( idl_exception );

        if ( !m_cp.getM_map_all() )
            idl_exception._map = true;

        return idl_exception;
    }

    /**
     * Add an enum description
     */
    public IdlObject irEnum( org.omg.CORBA.Contained contained, IdlObject scope, boolean visible )
    {
        org.omg.CORBA.EnumDef ir_enum = org.omg.CORBA.EnumDefHelper.narrow( contained );

        IdlEnum idl_enum = null;

        IdlObject exist = scope.returnObject( ir_enum.name(), true );

        if ( exist == null )
        {
            idl_enum = new IdlEnum( scope );

            if ( !m_cp.getM_map_all() )
                idl_enum._map = true;

            idl_enum._import = visible;
        }
        else
        {
            idl_enum = ( IdlEnum ) exist;

            if ( idl_enum._import )
                return idl_enum;
        }

        idl_enum.name( ir_enum.name() );
        idl_enum._import = true;

        IdlEnumMember member = null;

        String [] members = ir_enum.members();

        for ( int i = 0; i < members.length; i++ )
        {
            member = new IdlEnumMember( idl_enum );

            member.name( members[ i ] );
            member.setValue( i );

            idl_enum.addIdlObject( member );
        }

        scope.addIdlObject( idl_enum );

        return idl_enum;
    }

    /**
     * Add an native description
     */
    public IdlObject irNative( org.omg.CORBA.Contained contained, IdlObject scope, boolean visible )
    {
        org.omg.CORBA.NativeDef ir_native = org.omg.CORBA.NativeDefHelper.narrow( contained );

        IdlNative idl_native = null;

        IdlObject exist = scope.returnObject( ir_native.name(), true );

        if ( exist == null )
        {
            idl_native = new IdlNative( scope );

            if ( !m_cp.getM_map_all() )
                idl_native._map = true;

            idl_native._import = visible;
        }
        else
        {
            idl_native = ( IdlNative ) exist;

            if ( idl_native._import )
                return idl_native;
        }

        idl_native.name( ir_native.name() );
        idl_native._import = true;

        scope.addIdlObject( idl_native );

        return idl_native;
    }

    /**
     * Add an alias description
     */
    public IdlObject irAlias( org.omg.CORBA.Contained contained, IdlObject scope, boolean visible )
    {
        org.omg.CORBA.AliasDef ir_alias = org.omg.CORBA.AliasDefHelper.narrow( contained );

        IdlTypeDef idl_alias = null;

        IdlObject exist = scope.returnObject( ir_alias.name(), true );

        if ( exist == null )
        {
            idl_alias = new IdlTypeDef( scope );

            if ( !m_cp.getM_map_all() )
                idl_alias._map = true;

            idl_alias._import = visible;
        }
        else
        {
            idl_alias = ( IdlTypeDef ) exist;

            if ( idl_alias._import )
                return idl_alias;
        }

        idl_alias.name( ir_alias.name() );
        idl_alias._import = true;

        idl_alias.type( getType( ir_alias.original_type_def(), idl_alias ) );

        scope.addIdlObject( idl_alias );

        return idl_alias;
    }

    /**
     * Add a valuebox description
     */
    public IdlObject irValueBox( org.omg.CORBA.Contained contained, IdlObject scope, boolean visible )
    {
        org.omg.CORBA.ValueBoxDef ir_valuebox = org.omg.CORBA.ValueBoxDefHelper.narrow( contained );

        IdlValueBox idl_valuebox = null;

        IdlObject exist = scope.returnObject( ir_valuebox.name(), true );

        if ( exist == null )
        {
            idl_valuebox = new IdlValueBox( scope );

            if ( !m_cp.getM_map_all() )
                idl_valuebox._map = true;

            idl_valuebox._import = visible;
        }
        else
        {
            idl_valuebox = ( IdlValueBox ) exist;

            if ( idl_valuebox._import )
                return idl_valuebox;
        }

        idl_valuebox.name( ir_valuebox.name() );
        idl_valuebox._import = true;

        idl_valuebox.type( getType( ir_valuebox.original_type_def(), idl_valuebox ) );

        scope.addIdlObject( idl_valuebox );

        return idl_valuebox;
    }

    /**
     * Add a constant description
     */
    public IdlObject irConstant( org.omg.CORBA.Contained contained, IdlObject scope, boolean visible )
    {
        org.omg.CORBA.ConstantDef ir_const = org.omg.CORBA.ConstantDefHelper.narrow( contained );

        IdlConst idl_const = null;

        IdlObject exist = scope.returnObject( ir_const.name(), true );

        if ( exist == null )
        {
            idl_const = new IdlConst( scope );

            if ( !m_cp.getM_map_all() )
                idl_const._map = true;

            idl_const._import = visible;
        }
        else
        {
            idl_const = ( IdlConst ) exist;

            if ( idl_const._import )
                return idl_const;
        }

        idl_const.name( ir_const.name() );
        idl_const._import = true;

        idl_const.type( getType( ir_const.type_def(), idl_const ) );

        idl_const.expression( getConstExpression( ir_const.value(), ir_const.type() ) );

        setConstValue( ir_const.value(), idl_const );

        scope.addIdlObject( idl_const );

        return idl_const;
    }

    /**
     * Return a primitive description
     */
    private IdlObject irPrimitive( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.PrimitiveDef primitive = org.omg.CORBA.PrimitiveDefHelper.narrow( type );

        switch ( primitive.kind().value() )
        {

        case org.omg.CORBA.PrimitiveKind._pk_void :
            return IdlSimple.void_type;

        case org.omg.CORBA.PrimitiveKind._pk_short :
            return IdlSimple.short_type;

        case org.omg.CORBA.PrimitiveKind._pk_long :
            return IdlSimple.long_type;

        case org.omg.CORBA.PrimitiveKind._pk_ushort :
            return IdlSimple.ushort_type;

        case org.omg.CORBA.PrimitiveKind._pk_ulong :
            return IdlSimple.ulong_type;

        case org.omg.CORBA.PrimitiveKind._pk_float :
            return IdlSimple.float_type;

        case org.omg.CORBA.PrimitiveKind._pk_double :
            return IdlSimple.double_type;

        case org.omg.CORBA.PrimitiveKind._pk_boolean :
            return IdlSimple.boolean_type;

        case org.omg.CORBA.PrimitiveKind._pk_char :
            return IdlSimple.char_type;

        case org.omg.CORBA.PrimitiveKind._pk_octet :
            return IdlSimple.octet_type;

        case org.omg.CORBA.PrimitiveKind._pk_any :
            return IdlSimple.any_type;

        case org.omg.CORBA.PrimitiveKind._pk_TypeCode :
            return IdlSimple.typecode_type;

        case org.omg.CORBA.PrimitiveKind._pk_string :
            return new IdlString( 0, scope );

        case org.omg.CORBA.PrimitiveKind._pk_objref :
            return IdlSimple.object_type;

        case org.omg.CORBA.PrimitiveKind._pk_longlong :
            return IdlSimple.longlong_type;

        case org.omg.CORBA.PrimitiveKind._pk_ulonglong :
            return IdlSimple.ulonglong_type;

        case org.omg.CORBA.PrimitiveKind._pk_wchar :
            return IdlSimple.wchar_type;

        case org.omg.CORBA.PrimitiveKind._pk_wstring :
            return new IdlWString( 0, scope );

        case org.omg.CORBA.PrimitiveKind._pk_value_base :
            return IdlSimple.valuebase_type;
        }

        return null;
    }

    /**
     * Return a string description
     */
    private IdlObject irString( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.StringDef ir_string = org.omg.CORBA.StringDefHelper.narrow( type );

        return new IdlString( ir_string.bound(), scope );
    }

    /**
     * Return a wstring description
     */
    private IdlObject irWstring( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.WstringDef ir_wstring = org.omg.CORBA.WstringDefHelper.narrow( type );

        return new IdlWString( ir_wstring.bound(), scope );
    }

    /**
     * Return a sequence description
     */
    private IdlObject irSequence( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.SequenceDef ir_seq = org.omg.CORBA.SequenceDefHelper.narrow( type );

        IdlSequence seq = new IdlSequence( scope );

        seq.addIdlObject( getType( ir_seq.element_type_def(), seq ) );

        seq.setSize( ir_seq.bound() );

        return seq;
    }

    /**
     * Return an array description
     */
    private IdlObject irArray( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.ArrayDef ir_array = org.omg.CORBA.ArrayDefHelper.narrow( type );

        IdlArray array = new IdlArray( scope );

        array.addIdlObject( getType( ir_array.element_type_def(), array ) );

        array.setDimension( ir_array.length() );

        return array;
    }

    /**
     * Add factories description
     */
    private void irFactory( org.omg.CORBA.Initializer [] factories, IdlValue idl_value )
    {
        IdlFactory factory = null;
        IdlFactoryMember member = null;

        for ( int i = 0; i < factories.length; i++ )
        {
            factory = new IdlFactory( idl_value );

            factory.name( factories[ i ].name );

            for ( int j = 0; j < factories[ i ].members.length; j++ )
            {
                member = new IdlFactoryMember( factory );

                member.name( factories[ i ].members[ j ].name );
                member.type( getType( factories[ i ].members[ j ].type_def, factory ) );

                factory.addIdlObject( member );
            }

            idl_value.addIdlObject( factory );
        }
    }

    /**
     * Add an attribute description
     */
    private IdlObject irAttribute( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.AttributeDef ir_attr = org.omg.CORBA.AttributeDefHelper.narrow( type );

        IdlAttribute idl_attr = new IdlAttribute( scope );

        idl_attr.name( ir_attr.name() );

        if ( ir_attr.mode().value() == org.omg.CORBA.AttributeMode._ATTR_NORMAL )
            idl_attr.readOnly( false );
        else
            idl_attr.readOnly( true );

        idl_attr.type( getType( ir_attr.type_def(), idl_attr ) );

        scope.addIdlObject( scope );

        return scope;
    }

    /**
     * Add an operation description
     */
    private IdlObject irOperation( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        IdlParam param = null;
        IdlRaises idl_raises = null;
        IdlContext idl_ctx = null;

        org.omg.CORBA.OperationDef ir_op = org.omg.CORBA.OperationDefHelper.narrow( type );

        IdlOp idl_op = new IdlOp( scope );

        idl_op.name( ir_op.name() );

        if ( ir_op.mode().value() == org.omg.CORBA.OperationMode._OP_NORMAL )
            idl_op.oneway( false );
        else
            idl_op.oneway( true );

        idl_op.addIdlObject( getType( ir_op.result_def(), idl_op ) );

        org.omg.CORBA.ParameterDescription [] params = ir_op.params();

        for ( int i = 0; i < params.length; i++ )
        {
            param = new IdlParam( idl_op );

            param.name( params[ i ].name );

            switch ( params[ i ].mode.value() )
            {

            case org.omg.CORBA.ParameterMode._PARAM_IN :
                param.param_attr( 0 );
                break;

            case org.omg.CORBA.ParameterMode._PARAM_OUT :
                param.param_attr( 1 );
                break;

            case org.omg.CORBA.ParameterMode._PARAM_INOUT :
                param.param_attr( 2 );
                break;
            }

            param.type( getType( params[ i ].type_def, idl_op ) );

            idl_op.addIdlObject( param );
        }

        org.omg.CORBA.ExceptionDef [] exs = ir_op.exceptions();

        if ( exs.length != 0 )
        {
            idl_raises = new IdlRaises( idl_op );

            for ( int i = 0; i < exs.length; i++ )
            {
                idl_raises.addIdlObject( getType( exs[ i ], idl_op ) );
            }

            idl_op.addIdlObject( idl_raises );
        }

        String [] ctx = ir_op.contexts();

        if ( ctx.length != 0 )
        {
            idl_ctx = new IdlContext( idl_op );

            for ( int i = 0; i < ctx.length; i++ )
            {
                idl_ctx.addValue( ctx[ i ] );
            }

            idl_op.addIdlObject( idl_ctx );
        }

        scope.addIdlObject( idl_op );

        return idl_op;
    }

    /**
     * Add a value member description
     */
    private IdlObject irValueMember( org.omg.CORBA.IRObject type, IdlObject scope )
    {
        org.omg.CORBA.ValueMemberDef ir_vmember = org.omg.CORBA.ValueMemberDefHelper.narrow( type );

        IdlStateMember idl_vmember = new IdlStateMember( scope );

        idl_vmember.name( ir_vmember.name() );

        if ( ir_vmember.access() == org.omg.CORBA.PRIVATE_MEMBER.value )
            idl_vmember.public_member( false );
        else
            idl_vmember.public_member( true );

        scope.type( getType( ir_vmember.type_def(), idl_vmember ) );

        scope.addIdlObject( idl_vmember );

        return idl_vmember;
    }

    /**
     * Find or create the parent scope to import description
     */
    public IdlObject createOrFindScope( String scope_name, IdlObject current_scope, org.omg.CORBA.Container ir_scope )
    {
        String name = null;
        org.omg.CORBA.Contained contained = null;
        org.omg.CORBA.Container container = null;
        IdlObject parent = null;

        if ( scope_name.startsWith( "::" ) )
            parent = _parser.root;
        else
            parent = current_scope;

        java.util.StringTokenizer token = new java.util.StringTokenizer( scope_name, "::" );

        container = ir_scope;

        while ( token.hasMoreTokens() )
        {
            name = token.nextToken();

            if ( token.hasMoreTokens() == false )
                return parent;

            contained = container.lookup( name );

            if ( contained == null )
            {
                throw new CompilationException( "Unable to find information into IR !" );
            }

            try
            {
                container = org.omg.CORBA.ContainerHelper.narrow( contained );
            }
            catch ( org.omg.CORBA.BAD_PARAM ex )
            {
                throw new CompilationException( "Incorrect scope !" );
            }

            if ( parent.isDefined( name, true ) )
            {
                parent = parent.returnObject( name, true );
            }
            else
            {
                parent = createContainer( container, parent );
            }
        }

        return parent;
    }

    /**
     * Create a new container
     */
    public IdlObject createContainer( org.omg.CORBA.Container container, IdlObject parent )
    {
        switch ( container.def_kind().value() )
        {

        case org.omg.CORBA.DefinitionKind._dk_Module :
            return irModule( container, parent, false );

        case org.omg.CORBA.DefinitionKind._dk_Interface :
            return irInterface( container, parent, false, 0 );

        case org.omg.CORBA.DefinitionKind._dk_LocalInterface :
            return irInterface( container, parent, false, 1 );

        case org.omg.CORBA.DefinitionKind._dk_AbstractInterface :
            return irInterface( container, parent, false, 2 );

        case org.omg.CORBA.DefinitionKind._dk_Value :
            return irValue( container, parent, false );

        case org.omg.CORBA.DefinitionKind._dk_Struct :
            return irStruct( container, parent, false );

        case org.omg.CORBA.DefinitionKind._dk_Union :
            return irUnion( container, parent, false );

        case org.omg.CORBA.DefinitionKind._dk_Exception :
            return irException( container, parent, false );

        default :
            throw new CompilationException( "Incorrect IR Container type" );
        }
    }

    /**
     * Return a data type
     */
    public IdlObject getType( org.omg.CORBA.IRObject type, IdlObject parent )
    {
        try
        {
            switch ( type.def_kind().value() )
            {

            case org.omg.CORBA.DefinitionKind._dk_Exception :
                org.omg.CORBA.ExceptionDef exc = org.omg.CORBA.ExceptionDefHelper.narrow( type );

                return new IdlIdent( exc.name(), parent, getDescription( type ) );

            case org.omg.CORBA.DefinitionKind._dk_Alias :

            case org.omg.CORBA.DefinitionKind._dk_Enum :

            case org.omg.CORBA.DefinitionKind._dk_ValueBox :

            case org.omg.CORBA.DefinitionKind._dk_Native :

            case org.omg.CORBA.DefinitionKind._dk_Interface :

            case org.omg.CORBA.DefinitionKind._dk_Struct :

            case org.omg.CORBA.DefinitionKind._dk_Union :

            case org.omg.CORBA.DefinitionKind._dk_Value :
                org.omg.CORBA.IDLType idl_type = org.omg.CORBA.IDLTypeHelper.narrow( type );

                return new IdlIdent( idl_type.type().name(), parent, getDescription( type ) );

            case org.omg.CORBA.DefinitionKind._dk_Primitive :
                return irPrimitive( type, parent );

            case org.omg.CORBA.DefinitionKind._dk_String :
                return irString( type, parent );

            case org.omg.CORBA.DefinitionKind._dk_Sequence :
                return irSequence( type, parent );

            case org.omg.CORBA.DefinitionKind._dk_Array :
                return irArray( type, parent );

            case org.omg.CORBA.DefinitionKind._dk_Wstring :
                return irWstring( type, parent );
            }
        }
        catch ( java.lang.Exception ex )
        {
            ex.printStackTrace();
            throw new CompilationException( "Unexpected error during type import" );
        }

        return null;
    }

    /**
     * Return an object description
     */
    private IdlObject getDescription( org.omg.CORBA.IRObject type )
    {
        String scope_name = irScopeName( type );

        IdlObject parent = createOrFindScope( scope_name, null, _ir );

        return irDescription( type, parent, false );
    }

    /**
     * Return a scope name for a IDL Type
     */
    private String irScopeName( org.omg.CORBA.IRObject type )
    {
        org.omg.CORBA.Contained contained = null;

        switch ( type.def_kind().value() )
        {

        case org.omg.CORBA.DefinitionKind._dk_Primitive :

        case org.omg.CORBA.DefinitionKind._dk_String :

        case org.omg.CORBA.DefinitionKind._dk_Sequence :

        case org.omg.CORBA.DefinitionKind._dk_Array :

        case org.omg.CORBA.DefinitionKind._dk_Wstring :
            _parser.warning( "Unexpected state during import..." );
            break;

        case org.omg.CORBA.DefinitionKind._dk_Alias :

        case org.omg.CORBA.DefinitionKind._dk_Enum :

        case org.omg.CORBA.DefinitionKind._dk_ValueBox :

        case org.omg.CORBA.DefinitionKind._dk_Native :

        case org.omg.CORBA.DefinitionKind._dk_Interface :

        case org.omg.CORBA.DefinitionKind._dk_Struct :

        case org.omg.CORBA.DefinitionKind._dk_Union :

        case org.omg.CORBA.DefinitionKind._dk_Value :

        case org.omg.CORBA.DefinitionKind._dk_Exception :
            contained = org.omg.CORBA.ContainedHelper.narrow( type );
            return "::" + contained.absolute_name();
        }

        return null;
    }

    /**
     * Return an union case value
     */
    private long getValue( org.omg.CORBA.Any any )
    {
        switch ( any.type().kind().value() )
        {

        case org.omg.CORBA.TCKind._tk_boolean :
            boolean b = any.extract_boolean();

            if ( b == true )
                return 1;

            return 0;

        case org.omg.CORBA.TCKind._tk_short :
            short s = any.extract_short();

            return ( long ) s;

        case org.omg.CORBA.TCKind._tk_ushort :
            short us = any.extract_ushort();

            return ( long ) us;

        case org.omg.CORBA.TCKind._tk_long :
            int l = any.extract_long();

            return ( long ) l;

        case org.omg.CORBA.TCKind._tk_ulong :
            int ul = any.extract_ulong();

            return ( long ) ul;

        case org.omg.CORBA.TCKind._tk_longlong :
            long ll = any.extract_longlong();

            return ll;

        case org.omg.CORBA.TCKind._tk_ulonglong :
            long ull = any.extract_ulonglong();

            return ull;

        case org.omg.CORBA.TCKind._tk_char :
            char c = any.extract_char();

            return ( long ) c;
        }

        return 0;
    }

    /**
     * Return a switch case expression
     */
    private String getExpression( org.omg.CORBA.Any any, org.omg.CORBA.TypeCode switch_tc )
    {
        try
        {
            switch ( any.type().kind().value() )
            {

            case org.omg.CORBA.TCKind._tk_boolean :
                boolean b = any.extract_boolean();

                if ( b == true )
                    return "TRUE";

                return "FALSE";

            case org.omg.CORBA.TCKind._tk_short :
                short s = any.extract_short();

                return "" + s;

            case org.omg.CORBA.TCKind._tk_ushort :
                short us = any.extract_ushort();

                return "" + us;

            case org.omg.CORBA.TCKind._tk_long :
                int l = any.extract_long();

                return "" + l;

            case org.omg.CORBA.TCKind._tk_ulong :
                int ul = any.extract_ulong();

                if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) switch_tc )._base_type().kind().value() == org.omg.CORBA.TCKind._tk_enum )
                {
                    return ( ( org.openorb.orb.core.typecode.TypeCodeBase ) switch_tc )._base_type().member_name( ul );
                }

                return "" + ul;

            case org.omg.CORBA.TCKind._tk_longlong :
                long ll = any.extract_longlong();
                return "" + ll;

            case org.omg.CORBA.TCKind._tk_ulonglong :
                long ull = any.extract_ulonglong();
                return "" + ull;

            case org.omg.CORBA.TCKind._tk_char :
                char c = any.extract_char();
                return "" + c;
            }
        }
        catch ( java.lang.Exception ex )
        { }

        return null;
    }

    /**
     * Return a switch case expression
     */
    private String getConstExpression( org.omg.CORBA.Any any, org.omg.CORBA.TypeCode switch_tc )
    {
        try
        {
            switch ( any.type().kind().value() )
            {

            case org.omg.CORBA.TCKind._tk_boolean :
                boolean b = any.extract_boolean();

                if ( b == true )
                    return "TRUE";

                return "FALSE";

            case org.omg.CORBA.TCKind._tk_short :
                short s = any.extract_short();

                return "" + s;

            case org.omg.CORBA.TCKind._tk_ushort :
                short us = any.extract_ushort();

                return "" + us;

            case org.omg.CORBA.TCKind._tk_long :
                int l = any.extract_long();

                return "" + l;

            case org.omg.CORBA.TCKind._tk_ulong :
                int ul = any.extract_ulong();

                if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) switch_tc )._base_type().kind().value() == org.omg.CORBA.TCKind._tk_enum )
                {
                    return ( ( org.openorb.orb.core.typecode.TypeCodeBase ) switch_tc )._base_type().member_name( ul );
                }

                return "" + ul;

            case org.omg.CORBA.TCKind._tk_longlong :
                long ll = any.extract_longlong();
                return "" + ll;

            case org.omg.CORBA.TCKind._tk_ulonglong :
                long ull = any.extract_ulonglong();
                return "" + ull;

            case org.omg.CORBA.TCKind._tk_char :
                char c = any.extract_char();
                return "" + c;

            case org.omg.CORBA.TCKind._tk_octet :
                byte o = any.extract_octet();
                return "" + o;

            case org.omg.CORBA.TCKind._tk_float :
                float f = any.extract_float();
                return "" + f;

            case org.omg.CORBA.TCKind._tk_double :
                double d = any.extract_double();
                return "" + d;

            case org.omg.CORBA.TCKind._tk_wchar :
                char wc = any.extract_wchar();
                return "" + wc;

            case org.omg.CORBA.TCKind._tk_string :
                return any.extract_string();

            case org.omg.CORBA.TCKind._tk_wstring :
                return any.extract_wstring();
            }
        }
        catch ( java.lang.Exception ex )
        { }

        return null;
    }

    /**
     * Return an union case value
     */
    private void setConstValue( org.omg.CORBA.Any any, IdlConst idl_const )
    {
        switch ( any.type().kind().value() )
        {

        case org.omg.CORBA.TCKind._tk_boolean :
            boolean b = any.extract_boolean();

            if ( b == true )
            {
                idl_const.intValue( 1 );
                idl_const.floatValue( 1 );
            }
            else
            {
                idl_const.intValue( 0 );
                idl_const.floatValue( 0 );
            }

            break;

        case org.omg.CORBA.TCKind._tk_short :
            short s = any.extract_short();
            idl_const.intValue( ( long ) s );
            idl_const.floatValue( s );
            break;

        case org.omg.CORBA.TCKind._tk_ushort :
            short us = any.extract_ushort();
            idl_const.intValue( ( long ) us );
            idl_const.floatValue( us );
            break;

        case org.omg.CORBA.TCKind._tk_long :
            int l = any.extract_long();
            idl_const.intValue( ( long ) l );
            idl_const.floatValue( l );
            break;

        case org.omg.CORBA.TCKind._tk_ulong :
            int ul = any.extract_ulong();
            idl_const.intValue( ( long ) ul );
            idl_const.floatValue( ul );
            break;

        case org.omg.CORBA.TCKind._tk_longlong :
            long ll = any.extract_longlong();
            idl_const.intValue( ll );
            idl_const.floatValue( ll );
            break;

        case org.omg.CORBA.TCKind._tk_ulonglong :
            long ull = any.extract_ulonglong();
            idl_const.intValue( ull );
            idl_const.floatValue( ull );
            break;

        case org.omg.CORBA.TCKind._tk_char :
            char c = any.extract_char();
            idl_const.intValue( ( long ) c );
            idl_const.floatValue( c );
            break;

        case org.omg.CORBA.TCKind._tk_octet :
            byte o = any.extract_octet();
            idl_const.intValue( ( long ) o );
            idl_const.floatValue( o );
            break;

        case org.omg.CORBA.TCKind._tk_float :
            float f = any.extract_float();
            idl_const.intValue( 0 );
            idl_const.floatValue( f );
            break;

        case org.omg.CORBA.TCKind._tk_double :
            double d = any.extract_double();
            idl_const.intValue( 0 );
            idl_const.floatValue( d );
            break;

        case org.omg.CORBA.TCKind._tk_wchar :
            char wc = any.extract_wchar();
            idl_const.intValue( ( long ) wc );
            idl_const.floatValue( 0 );
            break;

        case org.omg.CORBA.TCKind._tk_string :
            idl_const.intValue( 0 );
            idl_const.floatValue( 0 );
            break;

        case org.omg.CORBA.TCKind._tk_wstring :
            idl_const.intValue( 0 );
            idl_const.floatValue( 0 );
            break;
        }
    }

    /**
     * This operation is used to import a container from an IDL file if a link has been
     * provided.
     *
     * Only top level import are accepted and only one level container are accepted :
     *
     * import ::top  // OK
     * import top::foo // NOT OK
     * import ::top::foo // NOT OK
     */
    public boolean checkFromImportFromFile( String container_name )
    {
        if ( !container_name.startsWith( "::" ) )
            return false;

        container_name = container_name.substring( 2 );

        if ( container_name.indexOf( "::" ) != -1 )
            return false;

        if ( importFromIDLFile( container_name ) == true )
        {
            addAsImported( container_name );
            return true;
        }

        return false;
    }

    /**
     * Import a container from an IDL file
     */
    private boolean importFromIDLFile( String container_name )
    {
        String idl_file = getIDLFileName( container_name );

        if ( idl_file != null )
        {
            _parser.include_idl_file( idl_file );

            return true;
        }

        return false;
    }

    /**
     * Return the IDL file name.
     */
    private String getIDLFileName( String container_name )
    {
        for ( int i = 0; i < m_cp.getM_importLink().size(); i++ )
        {
            if ( getLinkContainerName( ( String ) m_cp.getM_importLink().elementAt( i ) ).equalsIgnoreCase( container_name ) )
            {
                return getLinkFileName( ( String ) m_cp.getM_importLink().elementAt( i ) );
            }
        }

        return null;
    }

    /**
     * Return the container name from a link name
     */
    private String getLinkContainerName( String link )
    {
        return link.substring( 0, link.indexOf( ":" ) );
    }

    /**
     * Return the file name from a link name
     */
    private String getLinkFileName( String link )
    {
        return link.substring( link.indexOf( ":" ) + 1, link.length() );
    }

    /**
     * Return TRUE if this scope name was previously imported
     */
    private boolean isAlreadyImported( String scope_name )
    {
        if ( !scope_name.startsWith( "::" ) )
            scope_name = "::" + scope_name;

        if ( _imported.get( scope_name ) != null )
            return true;

        return false;
    }

    /**
     * Add a scope name into the imported scope name
     */
    private void addAsImported( String scope_name )
    {
        if ( !scope_name.startsWith( "::" ) )
            scope_name = "::" + scope_name;

        _imported.put( scope_name, scope_name );
    }
}
