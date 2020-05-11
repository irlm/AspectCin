/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.policy;

import glassbox.monitor.MonitorResponseTestCase;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import junit.framework.TestCase;

public aspect TestPolicy {

    declare error: staticinitialization(TestCase+) && within(TestCase+) && !within(*..*Test) && 
        /* within(isAbstract())*/ 
        !staticinitialization(glassbox.test.spring.DelegatedDependencyInjectionSpringContextTests+ || 
                VirtualMockObjectTestCase || MonitorResponseTestCase || glassbox..Abstract* || glassbox.common.BaseTestCase):
        "tests must end in Test";
}
