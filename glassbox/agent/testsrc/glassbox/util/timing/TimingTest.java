/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing;

import glassbox.util.timing.ClockManager;
import glassbox.util.timing.api.TimeConversion;

import java.util.Date;

import junit.framework.TestCase;


public class TimingTest extends TestCase {

    public void testConvert() {
        int tries = 5;
        int fails = 0; //could fail once if a millisecond tick occurs during the run
        
        for (int i=0; i<tries; i++) {
            ClockManager.setInstance(new OldJavaClock());
            long nanos = ClockManager.getTime();
            long millis = System.currentTimeMillis();

            long dateTime = TimeConversion.convertNanosToDate(nanos).getTime();
            assertEquals(dateTime, TimeConversion.convertNanosToMillis(nanos));
            assertEquals((dateTime+500L)/1000L, TimeConversion.convertNanosToSeconds(nanos));

            long diff = Math.abs(dateTime - millis);

            if (diff>10) {                  // Windows currentTimeMillis resolution is 10ms.
                assertTrue(diff < 101);
                fails++;
            }
        }
        assertTrue(fails<=1);
    }
}
