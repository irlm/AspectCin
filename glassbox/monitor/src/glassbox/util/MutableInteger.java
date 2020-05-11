/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.util;

/**
 * @author Ron
 *
 * An integer holder that is mutable: you can set its value.
 */
public class MutableInteger {
    private int value;
    
    public MutableInteger() {
    }
    public MutableInteger(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
}
