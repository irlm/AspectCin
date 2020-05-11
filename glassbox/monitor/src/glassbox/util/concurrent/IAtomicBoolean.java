/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.concurrent;

/**
 * Interface for atomic booleans whether Java 5+ or backported...
 *
 */
public interface IAtomicBoolean {
    public boolean get();
    public boolean compareAndSet(boolean expect, boolean update);
    public boolean weakCompareAndSet(boolean expect, boolean update);
    public void set(boolean newValue);
    public boolean getAndSet(boolean newValue);
}
