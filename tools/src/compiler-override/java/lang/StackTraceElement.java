/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package java.lang;

public final class StackTraceElement
    implements java.io.Serializable
{
    private StackTraceElement()
    {
        /* assert false; */
    }

    public String getFileName()
    {
        return null;
    }

    public int getLineNumber()
    {
        return 0;
    }

    public String getClassName()
    {
        return null;
    }

    public String getMethodName()
    {
        return null;
    }

    public boolean isNativeMethod()
    {
        return false;
    }
}
