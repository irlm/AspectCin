/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public aspect OptionalBeanRecovery {

    private boolean OptionalBean.failed = false;
    private String OptionalBean.beanName;
    
    public void OptionalBean.onError(Exception e) {
        defaultOnError(e);
    }

    public final void OptionalBean.defaultOnError(Exception e) {
        logWarn("Unable to initialize "+beanName, e);
    }
    
    public void OptionalBean.setBeanName(String name) {
        beanName = name;
    }
    
    public String OptionalBean.getBeanName() {
        return beanName;
    }
    
    void around(OptionalBean optionalBean) : execution(void afterPropertiesSet()) && this(optionalBean) {
        try {
            proceed(optionalBean);
        } catch (Exception e) {
            optionalBean.failed = true;
            optionalBean.onError(e);
        }
    }

    void around(OptionalBean optionalBean) : execution(void destroy()) && this(optionalBean) {
        if (!optionalBean.failed) {
            proceed(optionalBean);
        }
    }    
            
}
