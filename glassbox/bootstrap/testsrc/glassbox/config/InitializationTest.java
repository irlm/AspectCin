/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;
import junit.framework.TestCase;


public class InitializationTest extends TestCase {

    // we need virtual mocks to test initialization more thoroughly, or refactor it to be more OO
    public void testStopIsHarmless() {
        GlassboxInitializer.stop();
    }

}
