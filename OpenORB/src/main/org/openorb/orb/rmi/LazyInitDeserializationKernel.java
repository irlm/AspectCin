/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

/**
 * A DeserializationKernel that delegates all operations to another kernel,
 * the delegate is created on demand.
 *
 * @version $Revision: 1.3 $ $Date: 2005/02/28 10:13:57 $
 * @author lkuehne
 * @author Richard G Clark
 */
final class LazyInitDeserializationKernel implements DeserializationKernel
{
    private static String s_delegateShortName;

    LazyInitDeserializationKernel()
    {
    }

    static synchronized void setDelegateName( final String delegateName )
    {
        s_delegateShortName = delegateName;
    }

    private static synchronized String getDelegateName()
    {
        return s_delegateShortName;
    }

    /**
     * Thread safe lock-less lazy singleton.
     * This class holds a reference to the singleton instance in VALUE static field.
     * The singleton instance is instantiated when this class is loaded.
     * This occurs when the getKernel() is called for the first time.
     */
    private static final class SINGLETON
    {
        static final DeserializationKernel VALUE
                = DeserializationKernelFactory.createDeserializationKernel( getDelegateName() );
    }

    private static DeserializationKernel getKernel()
    {
        return SINGLETON.VALUE;
    }


    public Object allocateNewObject( final Class c, final Class base )
            throws InstantiationException, IllegalAccessException
    {
        return getKernel().allocateNewObject( c, base );
    }

    public void setObjectField( final Class c, final String n, final Object o, final Object v )
    {
        getKernel().setObjectField( c, n, o, v );
    }

    public void setBooleanField( final Class c, final String n, final Object o, final boolean v )
    {
        getKernel().setBooleanField( c, n, o, v );
    }

    public void setByteField( final Class c, final String n, final Object o, final byte v )
    {
        getKernel().setByteField( c, n, o, v );
    }

    public void setCharField( final Class c, final String n, final Object o, final char v )
    {
        getKernel().setCharField( c, n, o, v );
    }

    public void setShortField( final Class c, final String n, final Object o, final short v )
    {
        getKernel().setShortField( c, n, o, v );
    }

    public void setIntField( final Class c, final String n, final Object o, final int v )
    {
        getKernel().setIntField( c, n, o, v );
    }

    public void setLongField( final Class c, final String n, final Object o, final long v )
    {
        getKernel().setLongField( c, n, o, v );
    }

    public void setFloatField( final Class c, final String n, final Object o, final float v )
    {
        getKernel().setFloatField( c, n, o, v );
    }

    public void setDoubleField( final Class c, final String n, final Object o, final double v )
    {
        getKernel().setDoubleField( c, n, o, v );
    }
}
