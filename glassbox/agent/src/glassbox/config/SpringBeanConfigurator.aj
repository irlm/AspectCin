/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Configuration of any bean using Spring
 */
public abstract aspect SpringBeanConfigurator extends BeanConfigurator implements ApplicationContextAware {
    
    private ConfigurableApplicationContext applicationContext;
    private ConfigurableListableBeanFactory beanFactory;
    
    protected pointcut configuring() :
        call(ApplicationContext+.new(..));
    
    /**
     * DI the Spring application context in which this aspect should configure beans.
     * @param ctx must implement ConfigurableApplicationContext or be  null
     */
    public void setApplicationContext(ApplicationContext ctx) {
        if (ctx == null) {
            applicationContext = null;
        } else if (!(ctx instanceof ConfigurableApplicationContext)) {
            throw new AspectConfigurationException(
                    "ApplicationContext [" + ctx + "] does not implement ConfigurableApplicationContext.");
        } else {
            applicationContext = (ConfigurableApplicationContext)ctx;
            beanFactory = applicationContext.getBeanFactory();
        }
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * Implementation of configureBean from the super-aspect
     */
    protected void configureBean(Object bean, String beanName) {
        if (applicationContext == null) {
            throw new AspectConfigurationException("No context defined for [" + this + "]"); 
        }
        
        if (beanFactory != null && beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.applyBeanPropertyValues(bean, beanName);
        }
    }
    
}
