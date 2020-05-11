/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test;

public abstract class ReaderThread extends Thread {
    private Object storedValue;
    
    public Object getValue() {
        return storedValue;
    }
    protected abstract Object read();
    public void run() {
        storedValue = read();
    }
}
