/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

/**
 * Configure and validate any object whose type is annotated with @Bean. 
 * Validation occurs for any type implementing the ValidateConfiguration
 * interface. To be subclassed for each IoC mechanism you want to
 * support (eg. Spring, HiveMind, Pico, JDK, ...).
 * 
 * @author Ron Bodkin derived from the annotation-driven version from Adrian Colyer
 */
public abstract aspect BeanConfigurator {

    /**
     * The creation of a new bean
     */
    // in general, we don't want to configure or validate beans when an IoC container
    // is already running, so we don't validate things prematurely
    protected pointcut beanCreation(Object beanInstance) :
        execution(new(..)) && this(beanInstance) && beanScope()  && 
        !cflow(configuring());
    
    protected abstract pointcut beanScope();
    protected abstract pointcut configuring();

    /**
     * All beans should be configured after construction.
     */
    after(Object beanInstance) returning : beanCreation(beanInstance) 
    {
      configureBean(beanInstance,getBeanName(beanInstance));
    }
    
    /**
     * If a bean implements the ValidateConfiguration interface, then call
     * validate() on it once it has been configured (this advice runs after the
     * configuration advice). 
     */
    after(ValidateConfiguration beanInstance) returning : 
        beanCreation(Object) && this(beanInstance) {
        beanInstance.validate();
    }
    
    /**
     * The bean name is either the value given in the annotation (@Bean("MyBean") ),
     * or the name of the type if no value is given (@Bean ).
     */
    protected String getBeanName(Object beanInstance) {
        return beanInstance.getClass().getName();     
    }

    /**
     * To be overriden by sub-aspects. Configure the bean instance using the given
     * bean name.
     */
    protected abstract void configureBean(Object bean,String beanName);
}
