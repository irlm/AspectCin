/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.rmi;

import org.openorb.compiler.CompilerProperties;

/**
 * This class contains all properties for the RMI compiler
 *
 * @author putrycz
 */
public class RmiCompilerProperties extends CompilerProperties
{
    /** Flag that indicates if IDL files must be mapped. */
    private boolean m_mapIDL = true;

    public boolean getMapIDL()
    {
        return m_mapIDL;
    }

    public void setMapIDL( boolean mapIDL )
    {
        m_mapIDL = mapIDL;
    }

    /**
     * Flag that indicates if Ties should conform to the mapping
     * of RMI exceptions to CORBA system exceptions (see EJB2.0,
     * 19.5.3 Mapping of system exceptions).
     *
     * Calling the mapException method from the tie works for bean
     * managed transactions only. If the transactions are container
     * managed the invocation is too early. The correct location for
     * all the cases is in the Container's postInvoke() method.
     * (See. BaseContainer.postInvoke() for the J2EE RI)
     */
    private boolean m_mapEJBExceptions = false;

    public boolean getMapEJBExceptions()
    {
        return m_mapEJBExceptions;
    }

    public void setMapEJBExceptions( boolean mapEJBExceptions )
    {
        m_mapEJBExceptions = mapEJBExceptions;
    }

    /**
     * Map local optimizations.
     */
    private boolean m_mapLocal = true;

    public boolean getMapLocal()
    {
        return m_mapLocal;
    }

    public void setMapLocal( boolean mapLocal )
    {
        m_mapLocal = mapLocal;
    }

    /** Reference to the java file name. */
    private java.util.Vector m_includedFiles = new java.util.Vector();

    public java.util.Vector getIncludedFiles()
    {
        return m_includedFiles;
    }

    private boolean m_generateValueMethods = true;

    public boolean getGenerateValueMethods()
    {
        return m_generateValueMethods;
    }

    public void setGenerateValueMethods( boolean generateValueMethods )
    {
        m_generateValueMethods = generateValueMethods;
    }

    private ClassLoader m_classloader = null;

    /**
     * Returns the m_classloader.
     * @return ClassLoader
     */
    public ClassLoader getClassloader()
    {
        return m_classloader;
    }

    /**
     * Sets the m_classloader.
     * @param m_classloader The m_classloader to set
     */
    public void setClassloader( ClassLoader m_classloader )
    {
        this.m_classloader = m_classloader;
    }
}

