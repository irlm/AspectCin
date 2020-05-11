/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
                                                                                                                                              
package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable
    implements Serializable
{
    public Throwable()
    {
        // not implemented
    }

    public Throwable( String message )
    {
        // not implemented
    }

    public Throwable( String message, Throwable cause )
    {
        // not implemented
    }

    public Throwable( Throwable cause )
    {
        // not implemented
    }

    public String getMessage()
    {
        // not implemented
        return null;
    }

    public String getLocalizedMessage()
    {
        // not implemented
        return null;
    }

    public Throwable getCause()
    {
        return null;
    }

    public Throwable initCause( Throwable cause )
    {
        return null;
    }

    public void printStackTrace()
    {
        // not implemented
    }

    public void printStackTrace( PrintStream s )
    {
        // not implemented
    }

    public void printStackTrace( PrintWriter s )
    {
        // not implemented
    }

    public Throwable fillInStackTrace()
    {
        return null;
    }

    public StackTraceElement[] getStackTrace()
    {
        return null;
    }

    public void setStackTrace( StackTraceElement[] stackTrace )
    {
        // not implemented
    }
}
