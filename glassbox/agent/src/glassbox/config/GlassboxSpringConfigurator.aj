/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import junit.framework.TestCase;

/**
 * Derived from Adrian Colyer's DefaultSpringInitializer. This will configure the Spring application context using the
 * set of beans.xml files available in the classpath (what you want for many applications). Configuration files can be
 * overriden by specifying the glassbox.config.configLocations property (useful for testing etc.).
 */
public aspect GlassboxSpringConfigurator extends SpringBeanConfigurator {

    // only auto-configure objects that explicitly ask for configuration
	// more typically, Spring will just instantiate & configure things and we don't want
	// to step on the toes of Spring (e.g., trying to instantiate stuff before it has finished setting up)
    protected pointcut beanScope() :
		within(Bean+);
   
    private static final long serialVersionUID = 1L;
}
