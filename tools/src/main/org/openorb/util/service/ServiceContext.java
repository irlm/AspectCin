/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.service;

import org.apache.avalon.framework.context.DefaultContext;

/**
 * Context information for the Service.
 * This class is mostly a placeholder for the future
 * to provide OpenORB specific methods on top
 * of DefaultContext.
 *
 * @author Gary Shea
 * @author Shawn Boyce
 */
public class ServiceContext
    extends DefaultContext
{
    /** ORB parameter key */
    public static final String ORB  = "ORB";

    /** POA parameter key */
    public static final String POA = "POA";


    /**
     * Sets the ORB value.
     * This method can only be called when context is read-write.
     * @param valueObj ORB value
     * @deprecated use put( ORB, valueObj ) instead
     */
    public void setORB( Object valueObj )
    {
        put( ORB, valueObj );
    }

    /**
     * Sets the POA value.
     * This method can only be called when context is read-write.
     * @param valueObj POA value
     * @deprecated use put( POA, valueObj ) instead
     */
    public void setPOA( Object valueObj )
    {
        put( POA, valueObj );
    }


    /**
     * Changes the context to be read-only.
     * @deprecated use makeReadOnly() instead
     */
    public void setReadOnly()
    {
        makeReadOnly();
    }
}

