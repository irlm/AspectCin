/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.openorb.compiler.taskdefs.CompilerActionsListener;


/**
 * All properties of the IdlCompiler
 * Should ideally be replaced by a real Propertyclass
 * Added the setters and getters to comply with checkstyle rules
 * @author Erik Putrycz
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:36 $
 */
public class CompilerProperties
{
    /**
     * List of files to compile
     */
    private Vector m_compileList = new java.util.Vector();

    /**
     * Include directories
     */
    private Vector m_includeList = new java.util.Vector();

    /**
     * Maps #defined macros to their expansions
     */
    private Hashtable m_macros = new Hashtable();

    /**
     * Pseudo Import list
     */
    private Vector m_importLink = new java.util.Vector();

    /**
     * Native descriptions
     */
    private Vector m_nativeDefinition = new java.util.Vector();

    /**
     * Indicates if the stub must be generated
     */
    private boolean m_map_stub = true;

    /**
     * Indicates using local stubs
     */
    private boolean m_local_stub = true;

    /**
     * Indicates if the skeleton must be generated
     */
    private boolean m_map_skeleton = true;

    /**
     * Indicates if the included elements must be mapped
     */
    private boolean m_map_all = false;

    /**
     * Indicates if TIE approach must be generated
     */
    private boolean m_map_tie = true;

    /**
     * Indicates if the user code must be generated
     */
    private boolean m_map_user = false;

    /**
     * Indicates if the compilation uses PIDL
     */
    private boolean m_pidl = false;

    /**
     * Indicates if the POA adapter is used
     */
    private boolean m_map_poa = true;

    /**
     * Indicates if verbose mode is activated
     */
    private boolean m_verbose = false;

    /**
     * Package name for the generated code
     */
    private String m_packageName = null;

    /**
     * Indicates if a package must be used
     */
    private boolean m_use_package = false;

    /**
     * Use portable helper. This is only needed if the class must be compiled
      * without openorb.
      */
    private boolean m_portableHelper = false;

    /**
     * Indicates the output directory
     */
    private File m_destdir = null;

    /**
     * Indicates if the the stub and skeleton uses DII and DSI
     */
    private boolean m_dynamic = false;

    /**
     * Indicates if the prefix is used to name the packages
     *
     */
    private boolean m_usePrefix = true;

    /**
     * Active silent mode
     */
    private boolean m_silentMode = false;

    /**
     * Reverse the prefix
     */
    private boolean m_reversePrefix = true;

    private boolean m_useReflection;

    private boolean m_useSwitch;

    private boolean m_useClasses = true;

    /**
     * switch to include the bundled idl files
     * does activate the resource handler as well
     */
    private boolean m_use_bundled_idl = true;

    private boolean m_jdk1_4;

    private int m_minTableSize = 9;

    private String m_generateValueFactory;

    private String m_generateValueImpl;

    private boolean m_retainPossibleCause;

    private CompilerActionsListener m_clistener = null;

    public void setM_clistener( CompilerActionsListener m_clistener )
    {
        this.m_clistener = m_clistener;
    }

    public CompilerActionsListener getM_clistener()
    {
        return m_clistener;
    }

    public void setM_compileList( Vector m_compileList )
    {
        this.m_compileList = m_compileList;
    }

    public Vector getM_compileList()
    {
        return m_compileList;
    }

    public void setM_destdir( File m_destdir )
    {
        this.m_destdir = m_destdir;
    }

    public File getM_destdir()
    {
        return m_destdir;
    }

    public void setM_dynamic( boolean m_dynamic )
    {
        this.m_dynamic = m_dynamic;
    }

    public boolean getM_dynamic()
    {
        return m_dynamic;
    }

    public void setM_generateValueFactory( String m_generateValueFactory )
    {
        this.m_generateValueFactory = m_generateValueFactory;
    }

    public String getM_generateValueFactory()
    {
        return m_generateValueFactory;
    }

    public void setM_generateValueImpl( String m_generateValueImpl )
    {
        this.m_generateValueImpl = m_generateValueImpl;
    }

    public String getM_generateValueImpl()
    {
        return m_generateValueImpl;
    }

    public void setM_importLink( Vector m_importLink )
    {
        this.m_importLink = m_importLink;
    }

    public Vector getM_importLink()
    {
        return m_importLink;
    }

    public void setM_includeList( Vector m_includeList )
    {
        this.m_includeList = m_includeList;
    }

    public Vector getM_includeList()
    {
        return m_includeList;
    }

    public void setM_jdk1_4( boolean m_jdk1_4 )
    {
        this.m_jdk1_4 = m_jdk1_4;
    }

    public boolean getM_jdk1_4()
    {
        return m_jdk1_4;
    }

    public void setM_local_stub( boolean m_local_stub )
    {
        this.m_local_stub = m_local_stub;
    }

    public boolean getM_local_stub()
    {
        return m_local_stub;
    }

    public void setM_macros( Hashtable m_macros )
    {
        this.m_macros = m_macros;
    }

    public Hashtable getM_macros()
    {
        return m_macros;
    }

    public void setM_map_all( boolean m_map_all )
    {
        this.m_map_all = m_map_all;
    }

    public boolean getM_map_all()
    {
        return m_map_all;
    }

    public void setM_map_poa( boolean m_map_poa )
    {
        this.m_map_poa = m_map_poa;
    }

    public boolean getM_map_poa()
    {
        return m_map_poa;
    }

    public void setM_map_skeleton( boolean m_map_skeleton )
    {
        this.m_map_skeleton = m_map_skeleton;
    }

    public boolean getM_map_skeleton()
    {
        return m_map_skeleton;
    }

    public void setM_map_stub( boolean m_map_stub )
    {
        this.m_map_stub = m_map_stub;
    }

    public boolean getM_map_stub()
    {
        return m_map_stub;
    }

    public void setM_map_tie( boolean m_map_tie )
    {
        this.m_map_tie = m_map_tie;
    }

    public boolean getM_map_tie()
    {
        return m_map_tie;
    }

    public void setM_map_user( boolean m_map_user )
    {
        this.m_map_user = m_map_user;
    }

    public boolean getM_map_user()
    {
        return m_map_user;
    }

    public void setM_minTableSize( int m_minTableSize )
    {
        this.m_minTableSize = m_minTableSize;
    }

    public int getM_minTableSize()
    {
        return m_minTableSize;
    }

    public void setM_nativeDefinition( Vector m_nativeDefinition )
    {
        this.m_nativeDefinition = m_nativeDefinition;
    }

    public Vector getM_nativeDefinition()
    {
        return m_nativeDefinition;
    }

    public void setM_packageName( String m_packageName )
    {
        this.m_packageName = m_packageName;
    }

    public String getM_packageName()
    {
        return m_packageName;
    }

    public void setM_pidl( boolean m_pidl )
    {
        this.m_pidl = m_pidl;
    }

    public boolean getM_pidl()
    {
        return m_pidl;
    }

    public void setM_portableHelper( boolean m_portableHelper )
    {
        this.m_portableHelper = m_portableHelper;
    }

    public boolean getM_portableHelper()
    {
        return m_portableHelper;
    }

    public void setM_retainPossibleCause( boolean m_retainPossibleCause )
    {
        this.m_retainPossibleCause = m_retainPossibleCause;
    }

    public boolean getM_retainPossibleCause()
    {
        return m_retainPossibleCause;
    }

    public void setM_reversePrefix( boolean m_reversePrefix )
    {
        this.m_reversePrefix = m_reversePrefix;
    }

    public boolean getM_reversePrefix()
    {
        return m_reversePrefix;
    }

    public void setM_silentMode( boolean m_silentMode )
    {
        this.m_silentMode = m_silentMode;
    }

    public boolean getM_silentMode()
    {
        return m_silentMode;
    }

    public void setM_use_bundled_idl( boolean m_use_bundled_idl )
    {
        this.m_use_bundled_idl = m_use_bundled_idl;
    }

    public boolean getM_use_bundled_idl()
    {
        return m_use_bundled_idl;
    }

    public void setM_use_package( boolean m_use_package )
    {
        this.m_use_package = m_use_package;
    }

    public boolean getM_use_package()
    {
        return m_use_package;
    }

    public void setM_useClasses( boolean m_useClasses )
    {
        this.m_useClasses = m_useClasses;
    }

    public boolean getM_useClasses()
    {
        return m_useClasses;
    }

    public void setM_usePrefix( boolean m_usePrefix )
    {
        this.m_usePrefix = m_usePrefix;
    }

    public boolean getM_usePrefix()
    {
        return m_usePrefix;
    }

    public void setM_useReflection( boolean m_useReflection )
    {
        this.m_useReflection = m_useReflection;
    }

    public boolean getM_useReflection()
    {
        return m_useReflection;
    }

    public void setM_useSwitch( boolean m_useSwitch )
    {
        this.m_useSwitch = m_useSwitch;
    }

    public boolean getM_useSwitch()
    {
        return m_useSwitch;
    }

    public void setM_verbose( boolean m_verbose )
    {
        this.m_verbose = m_verbose;
    }

    public boolean getM_verbose()
    {
        return m_verbose;
    }

}
