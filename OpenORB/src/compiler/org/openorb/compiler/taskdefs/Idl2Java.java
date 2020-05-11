/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.taskdefs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.openorb.compiler.IdlCompiler;

/**
 * This class is the AntTask for the OpenORB IDL Compiler
 * @author Erik Putrycz
 * @version $Revision: 1.17 $ $Date: 2005/03/13 13:05:19 $
 */
public class Idl2Java extends GenericTask
{
    /**
     * the default location of IDL files
     */
    private static final String S_DEFAULT_IDL = "resource:/org/openorb/idl/";

    protected static Idl2Java s_singleton = null;

    /**
     * Constructor for Idl2Java.
     */
    public Idl2Java()
    {
        if ( s_singleton == null )
        {
            s_singleton = this;
        }
        m_comp = new IdlCompiler();
    }

    /**
     * @see GenericTask#getDefaultCacheName
     */
    protected String getDefaultCacheName()
    {
        return "idl2java.cache";
    }

    /*
     * Beginning of setters for the ant task
     */

    public void setQuiet( boolean silent )
    {
        m_cp.setM_silentMode( silent );
    }

    public void setVerbose( boolean verbose )
    {
        m_cp.setM_verbose( verbose );
    }

    public void setUsePrefix( boolean useprefix )
    {
        m_cp.setM_usePrefix( useprefix );
    }

    public void setReversePrefix( boolean reverseprefix )
    {
        m_cp.setM_reversePrefix( reverseprefix );
    }

    public void setGenerateStub( boolean map_stub )
    {
        m_cp.setM_map_stub( map_stub );
    }

    public void setGenerateLocalStub( boolean local_stub )
    {
        m_cp.setM_local_stub( local_stub );
    }

    public void setGenerateSkeleton( boolean map_skeleton )
    {
        m_cp.setM_map_skeleton( map_skeleton );
    }

    public void setGenerateTie( boolean map_tie )
    {
        m_cp.setM_map_tie( map_tie );
    }

    public void setPortableHelper( boolean portableHelper )
    {
        m_cp.setM_portableHelper( portableHelper );
    }

    public void setBOAMode( boolean map_boa )
    {
        m_cp.setM_map_poa( !map_boa );
    }

    public void setDynamic( boolean dynamic )
    {
        m_cp.setM_dynamic( dynamic );
    }

    public void setGenerateAll( boolean gen_all )
    {
        m_cp.setM_map_all( gen_all );
    }

    public void setPIDL( boolean pidl )
    {
        if ( pidl )
        {
            m_cp.setM_pidl( true );
            m_cp.setM_map_stub( false );
            m_cp.setM_map_skeleton( false );
            m_cp.setM_map_tie( false );
        }
    }

    public void setJDK14Code( boolean jdk14 )
    {
        m_cp.setM_jdk1_4( jdk14 );
    }

    public void setRetainPossibleCause( final boolean value )
    {
        m_cp.setM_retainPossibleCause( value );
    }

    public void setUptodateChecks( boolean uptodate )
    {
        m_uptodate_check = uptodate;
    }

    public void setIncludeORBIDL( boolean include_idls )
    {
        m_cp.setM_use_bundled_idl( include_idls );
    }

    public void setSrcdir( Path srcDir )
    {
        if ( m_src_path == null )
        {
            m_src_path = srcDir;
        }
        else
        {
            m_src_path.append( srcDir );
        }
    }

    public void setGenerateValueFactory( String val_factory )
    {
        m_cp.setM_generateValueFactory( val_factory );
    }

    public void setGenerateValueImpl( String valueimpl )
    {
        m_cp.setM_generateValueImpl( valueimpl );
    }

    public void setInvokeMethod( InvokeMethodType method_type )
    {
        m_cp.setM_useReflection( false );
        m_cp.setM_useSwitch( false );
        m_cp.setM_useClasses( false );

        String method_name = method_type.getValue();
        if ( "Classes".equals( method_name ) )
        {
            m_cp.setM_useClasses( true );
        }
        else if ( "Reflection".equals( method_name ) )
        {
            m_cp.setM_useReflection( true );
        }
        else if ( "Switch".equals( method_name ) )
        {
            m_cp.setM_useSwitch( true );
        }
        else
        {
            m_param_exception =
                new BuildException(
                    "'invokeMethod' support arguments: "
                    + "'Classes', 'Switch' and 'Reflection'" );
        }
    }

    public void setMinTableSize( int min_table_size )
    {
        m_cp.setM_minTableSize( min_table_size );
    }

    public void setPackage( String package_name )
    {
        m_cp.setM_use_package( false );
        m_cp.setM_packageName( package_name );
    }

    public void setImportLink( String import_link_name )
    {
        m_cp.getM_importLink().addElement( import_link_name );
    }

    public void setDestDir( File dest_dir )
    {
        if ( m_cp.getM_packageName() == null )
        {
            m_cp.setM_packageName( "" );
            m_cp.setM_use_package( false );
        }

        m_cp.setM_destdir( dest_dir );
    }

    /**
     * Set a nested symbol tag
     * @param new_symbol nested tag
     */
    public void addConfiguredSymbol( Symbol new_symbol )
    {
        if ( ( new_symbol.getName() != null ) && ( new_symbol.getValue() != null ) )
        {
            m_cp.getM_macros().put( new_symbol.getName(), new_symbol.getValue() );

            if ( m_cp.getM_verbose() )
            {
                log(
                    "Setting symbol "
                    + new_symbol.getName()
                    + "="
                    + new_symbol.getValue() );
            }
        }
        else
        {
            m_param_exception =
                new BuildException( "Invalid symbol definition", getLocation() );
        }
    }

    /**
     * Set a nested Native mapping tag
     * @param new_mapping nested tag
     */
    public void addConfiguredNativeMapping( NativeMapping new_mapping )
    {
        if ( ( new_mapping.getName() != null )
                && ( new_mapping.getMapping() != null ) )
        {
            m_cp.getM_nativeDefinition().addElement(
                new_mapping.getName() + ":" + new_mapping.getMapping() );

            if ( m_cp.getM_verbose() )
            {
                log(
                    "Setting native mapping "
                    + new_mapping.getName()
                    + ":"
                    + new_mapping.getMapping() );
            }
        }
        else
        {
            m_param_exception =
                new BuildException(
                    "Invalid native mapping definition",
                    getLocation() );
        }
    }

    /**
     * @see org.openorb.compiler.taskdefs.GenericTask#validateAttributes()
     */
    protected void validateAttributes() throws BuildException
    {
        if ( m_use_bundled_idl )
        {
            // add the bundled idl through a resource url
            try
            {
                m_cp.getM_includeList().addElement( new URL( S_DEFAULT_IDL ) );
            }
            catch ( final MalformedURLException e )
            {
                throw new BuildException( "there are errors..."
                    + "Could not add bundled idl [" + S_DEFAULT_IDL
                    + "] reason [" + e.getMessage() + "]" );
            }
        }
    }

    /**
     * @see org.openorb.compiler.taskdefs.GenericTask#getSingleton()
     */
    protected GenericTask getSingleton()
    {
        return s_singleton;
    }

    /**
     * The list of possible InvokeMethodTypes
     */
    public static class InvokeMethodType extends EnumeratedAttribute
    {
        /**
         * @see org.apache.tools.ant.types.EnumeratedAttribute#getValues()
         */
        public String[] getValues()
        {
            return new String[] {"Classes", "Reflection", "Switch"};
        }
    }

}
