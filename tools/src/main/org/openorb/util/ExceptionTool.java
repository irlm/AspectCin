/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

/**
 * A utility class for optionaly attaching causal objects to exceptions
 * if the exception implementation supports <code>initCause</code>.
 * Note that these utilities only are safe to use in JDK 1.3 bu only have
 * an effect under JDK 1.4+.
 *
 * @author Richard G Clark
 * @version $Id: ExceptionTool.java,v 1.4 2004/02/10 21:28:45 mrumpf Exp $
 */
public final class ExceptionTool
{
    private static final boolean JRE_1_4 = JREVersion.V1_4;

    private ExceptionTool()
    {
    }

    /**
     * Returns the cause associated with the <code>Throwable</code> if
     * available.
     *
     * @param e the <code>Throwable</code> to get the cause from
     * @return the cause if available, <code>null</code> if not
     */
    public static Throwable getCause( final Throwable e )
    {
        return e.getCause();
    }

    /**
     * Attaches the cause to the exception if this operation is supported
     * by the current environment.
     *
     * @param e the exception to attach the cause to
     * @param cause the cause of this specified exception
     *
     * @return the parameter e
     */
    public static SystemException initCause( final SystemException e,
            final Throwable cause )
    {
        if ( JRE_1_4  && ( null != cause ) )
        {
            e.initCause( cause );
        }
        return e;
    }

    /**
     * Attaches the cause to the exception if this operation is supported
     * by the current environment.
     *
     * @param e the exception to attach the cause to
     * @param cause the cause of this specified exception
     *
     * @return the parameter e
     */
    public static UserException initCause( final UserException e,
            final Throwable cause )
    {
        if ( JRE_1_4  && ( null != cause ) )
        {
            e.initCause( cause );
        }
        return e;
    }

    /**
     * Attaches the cause to the exception if this operation is supported
     * by the current environment.
     *
     * @param e the exception to attach the cause to
     * @param cause the cause of this specified exception
     *
     * @return the parameter e
     */
    public static Throwable initCause( final Throwable e,
            final Throwable cause )
    {
        if ( JRE_1_4  && ( null != cause ) )
        {
            e.initCause( cause );
        }
        return e;
    }

    /**
     * Attaches the cause to the exception if this operation is supported
     * by the current environment.
     *
     * @param e the exception to attach the cause to
     * @param cause the cause of this specified exception
     *
     * @return the parameter e
     */
    public static RuntimeException initCause( final RuntimeException e,
            final Throwable cause )
    {
        if ( JRE_1_4  && ( null != cause ) )
        {
            e.initCause( cause );
        }
        return e;
    }

    /**
     * Attaches the cause to the exception if this operation is supported
     * by the current environment.
     *
     * @param e the exception to attach the cause to
     * @param cause the cause of this specified exception
     *
     * @return the parameter e
     */
    public static Error initCause( final Error e, final Throwable cause )
    {
        if ( JRE_1_4  && ( null != cause ) )
        {
            e.initCause( cause );
        }
        return e;
    }

    /**
     * Attaches a <code>Throwable</code> to the end of the causal chain,
     * if possible. If the cause of the last <code>Throwable</code> is
     * explicity set to null then attachment is not possible.
     *
     * @param e the recipient of the attachment
     * @param cause the attachment
     *
     * @return the recipient
     *
     * @throws NullPointerException if e is <code>null</code>
     * @throws IllegalArgumentException if <code>e == cause</code>
     */
    public static Throwable appendCause( final Throwable e,
            final Throwable cause )
    {
        if ( !JRE_1_4 || ( null == cause ) )
        {
            return e;
        }

        Throwable current = e;

        while ( null != current.getCause() )
        {
            current = current.getCause();
        }

        try
        {
            current.initCause( cause );
        }
        catch ( final IllegalStateException ex )
        {
            // Throwable was explicity set to "no cause", silently ignore.
        }

        return e;
    }

    /**
     * Attaches a <code>Throwable</code> to the end of the causal chain as
     * a possible cause, if possible. If the cause of the last
     * <code>Throwable</code> is explicity set to null then attachment is
     * not possible. This utility would be used to retain the possible root
     * cause of a problem that can be masked by an exception thrown in the
     * <code>finally</code> block. For Example:
     *
     * <pre>
     *     final Throwable cause = null;
     *     try {
     *         someMethod();
     *     } catch (final Exception e) {
     *         possibleCause = e;
     *         throw e;
     *     } finally {
     *        try {
     *            cleanUp();
     *        } catch (final Exception e2) {
     *            ExceptionTool.appendPossibleCause(e2, e);
     *            throw e2;
     *        }
     *     }
     * </pre>
     *
     * @param e the recipient of the attachment
     * @param cause the attachment
     *
     * @return the recipient
     *
     * @throws NullPointerException if e is <code>null</code>
     * @throws IllegalArgumentException if <code>e == cause</code>
     */
    public static Throwable appendPossibleCause( final Throwable e,
            final Throwable cause )
    {
        if ( !JRE_1_4  || ( null == cause ) )
        {
            return e;
        }

        final Throwable possibleCause = new PossibleCause( cause );

        return appendCause( e, possibleCause );
    }


    /**
     * A utility class used to indicate a possible / direct cause of the
     * exception to which it has been chained to.
     */
    private static final class PossibleCause extends Throwable
    {
        /**
         * The standard message used for all PossibleCauses.
         */
        private static final String STANDARD_MESSAGE
                = "The next cause is a possible cause of previous exception";

        /**
         * The zero length stack used for all PossibleCauses.
         */
        private static final StackTraceElement[] NO_STACK
                = new StackTraceElement[0];


        /**
         * Constructs a PossibleCause with the specified cause.
         *
         * @param cause the cause detail
         */
        public PossibleCause( final Throwable cause )
        {
            super( STANDARD_MESSAGE, cause );
            setStackTrace( NO_STACK );
        }

        /**
         * This method is overridden so than no stack elements are generated.
         *
         * @return this object
         */
        public Throwable fillInStackTrace()
        {
            return this;
        }
    }
}

