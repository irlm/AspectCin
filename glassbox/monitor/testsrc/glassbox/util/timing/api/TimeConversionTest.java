/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing.api;

import java.util.Date;

import junit.framework.TestCase;

public class TimeConversionTest extends TestCase {

    public void testConvertNanosToDate() {
        Date date = new Date();
        long millis = TimeConversion.convertMillisToNanos(date.getTime());
        Date actual = TimeConversion.convertNanosToDate(millis);
        assertEquals(date, actual);
    }

    public void testConvertNanosToMillis() {
        assertEquals(1L, TimeConversion.convertNanosToMillis(1000L*1000L));
    }

    public void testConvertMillisToNanos() {
        assertEquals(1000L*1000L, TimeConversion.convertMillisToNanos(1L));
    }

    public void testConvertNanosToSeconds() {
        assertEquals(222L, TimeConversion.convertNanosToMillis(222L*1000L*1000L));
    }

    public void testConvertSecondsToNanos() {
        assertEquals(1000L*1000L*1000L, TimeConversion.convertSecondsToNanos(1L));
    }

    public void testNanosToDoubleSeconds() {
        assertEquals(0.5, TimeConversion.convertNanosToDoubleSeconds(1000L*1000L*1000L/2L), 1e-8);
    }

    public void testMeanNanosInSeconds() {
        assertEquals(0.25, TimeConversion.meanNanosInSeconds(1000L*1000L*1000L, 4), 1e-8);
    }

    public void testFormatMillis() {
        assertEquals("1.5 sec.", TimeConversion.formatMillis(1500L));
        assertEquals("0.00 ms", TimeConversion.formatMillis(0L));
        assertEquals("-1.5 sec.", TimeConversion.formatMillis(-1500L));
    }

    public void testFormatTime() {
        assertEquals("0.01 ms", TimeConversion.formatTime(1000L*5L));
        assertEquals("0.00 ms", TimeConversion.formatTime(1000L*5L-1L));
    }

    public void testFormatMeanNanos() {
        assertEquals("250 ms", TimeConversion.formatMeanNanos(1000L*1000L*24900L, 100));
    }

    public void testFormatTimeInSeconds() {
        assertEquals("0.05 ms", TimeConversion.formatTimeInSeconds(0.051345/1000.));
        assertEquals("0.06 ms", TimeConversion.formatTimeInSeconds(0.055/1000.)); 
        assertEquals("0.23 ms", TimeConversion.formatTimeInSeconds(0.23499/1000.)); 
        assertEquals("0.23 ms", TimeConversion.formatTimeInSeconds(0.225/1000.)); 
        assertEquals("4.1 ms", TimeConversion.formatTimeInSeconds(4.14999/1000.));
        assertEquals("4.2 ms", TimeConversion.formatTimeInSeconds(4.2/1000.));
        assertEquals("9.9 ms", TimeConversion.formatTimeInSeconds(9.9499999999/1000.));
        assertEquals("10 ms", TimeConversion.formatTimeInSeconds(9.95/1000.));
        assertEquals("99 ms", TimeConversion.formatTimeInSeconds(99.4999999/1000.));
        assertEquals("100 ms", TimeConversion.formatTimeInSeconds(99.5/1000.));
        assertEquals("100 ms", TimeConversion.formatTimeInSeconds(101./1000.));
    }

}
