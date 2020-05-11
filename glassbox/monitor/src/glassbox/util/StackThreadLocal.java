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

import java.util.*;

public class StackThreadLocal { //implements IStack {
    private ThreadLocal stackHolder = new StackThreadLocalHolder();
    
    private static class StackThreadLocalHolder extends ThreadLocal { 
        public Object initialValue() {
            return new ArrayList(20);
        }
    };
    
    protected List getList() {
        return (List)stackHolder.get();            
    }
    public void push(Object data) {
        getList().add(data);
    }
    public Object pop() throws EmptyStackException {
        List list = getList(); 
        int last = list.size()-1;
        if (last<0) {
            throw new EmptyStackException();
        }
        return list.remove(last);
    }
    public Object peek() {
        List list = getList(); 
        int last = list.size()-1;
        if (last<0) {
            return null;
        }
        return list.get(last);
    }
    public boolean isEmpty() {
        return getList().size() == 0;
    }
    public boolean remove(Object obj) {
        return getList().remove(obj);
    }
}