/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.concurrent;

import glassbox.util.concurrent.IAtomicBoolean;

/*
 * Based on the JSR-166 backport code ...
 * 
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

/**
 * A <tt>boolean</tt> value that may be updated atomically. See the
 * {@link edu.emory.mathcs.backport.java.util.concurrent.atomic} package specification for
 * summary of the properties of atomic variables. An
 * <tt>AtomicBoolean</tt> is used in applications such as atomically
 * updated flags, and cannot be used as a replacement for a
 * {@link java.lang.Boolean}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class BackportedAtomicBoolean implements java.io.Serializable, IAtomicBoolean {

    private static final long serialVersionUID = 4654671469794556979L;

    private volatile int value;

    /**
     * Creates a new <tt>AtomicBoolean</tt> with the given initial value.
     *
     * @param initialValue the initial value
     */
    public BackportedAtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    /**
     * Creates a new <tt>AtomicBoolean</tt> with initial value <tt>false</tt>.
     */
    public BackportedAtomicBoolean() {
        super();
    }

    /**
     * Returns the current value.
     *
     * @return the current value
     */
    public final boolean get() {
        return value != 0;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final synchronized boolean compareAndSet(boolean expect, boolean update) {
        if (expect == (value != 0)) {
            value = update ? 1 : 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public synchronized boolean weakCompareAndSet(boolean expect, boolean update) {
        if (expect == (value != 0)) {
            value = update ? 1 : 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unconditionally sets to the given value.
     *
     * @param newValue the new value
     */
    public final synchronized void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final synchronized void lazySet(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * Atomically sets to the given value and returns the previous value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final synchronized boolean getAndSet(boolean newValue) {
        int old = value;
        value = newValue ? 1 : 0;
        return old != 0;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    public String toString() {
        return Boolean.toString(get());
    }

}
