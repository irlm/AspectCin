/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import glassbox.agent.api.ApiType;

import junit.framework.Assert;
import junit.framework.TestCase;

public aspect CheckSerializability {
    after() returning(ApiType ApiType) : call(ApiType glassbox..*(..)) && within(TestCase+) {
        checkApiType(ApiType);
    }
    
    // just useful for debugging: too slow to leave this one on
//    before(Object key, Object value) : call(* getPerfStats(..)) && args(key, value) {
//        checkApiType((ApiType)key);
//        checkApiType((ApiType)value);
//    }
    
    private void checkApiType(ApiType ApiType) {
        try {
            if(ApiType!=null) { 
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                ObjectOutputStream ostr = new ObjectOutputStream(bas);
                ostr.writeObject(ApiType);
                byte[] arr = bas.toByteArray();
                ByteArrayInputStream bis = new ByteArrayInputStream(arr);
                ObjectInputStream istr = new ObjectInputStream(bis);
                Object s = istr.readObject();
                Assert.assertEquals(ApiType.getClass(), s.getClass());
                //System.out.println(ApiType.getClass());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
