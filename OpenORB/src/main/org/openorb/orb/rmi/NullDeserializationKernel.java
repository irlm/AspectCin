/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

/**
 * A {@link DeserializationKernel} that throws {@link UnsupportedOperationException}
 * for every operation. Using this implementation is sufficient for scenarios without
 * RMI/IIOP and avoids the neccessity to create a platform dependent kernel. See
 * {@link DeserializationKernelFactory} why a DeserializationKernel is required for RMI/IIOP.
 *
 * @author lkuehne
 */
public class NullDeserializationKernel implements DeserializationKernel
{
    private static final String MSG =
            "This configuration of OpenORB does not support deserialization of valuetypes.";

    public Object allocateNewObject( Class c, Class base )
            throws InstantiationException, IllegalAccessException
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setObjectField( Class c, String n, Object o, Object v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setBooleanField( Class c, String n, Object o, boolean v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setByteField( Class c, String n, Object o, byte v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setCharField( Class c, String n, Object o, char v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setShortField( Class c, String n, Object o, short v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setIntField( Class c, String n, Object o, int v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setLongField( Class c, String n, Object o, long v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setFloatField( Class c, String n, Object o, float v )
    {
        throw new UnsupportedOperationException( MSG );
    }

    public void setDoubleField( Class c, String n, Object o, double v )
    {
        throw new UnsupportedOperationException( MSG );
    }
}
