/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing.api;

import glassbox.util.timing.Clock;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class TimeConversion {

    /**
     * Constant to convert from milliseconds to nanoseconds.
     */
    public static final long NANOSECONDS_PER_MILLISECOND = 1000000L;

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    
    public static final long NANOSECONDS_PER_SECOND = NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_SECOND;
    
    public static final String SECONDS_FORMAT = "0.0#";
    
    private static final long serialVersionUID = 1;

    public static Date convertNanosToDate(long nanos) {
        return new Date(convertNanosToMillis(nanos));
    }
    
    public static long convertNanosToMillis(long nanos) {
        return (nanos+NANOSECONDS_PER_MILLISECOND/2)/NANOSECONDS_PER_MILLISECOND;
    }
    
    public static long convertNanosToSeconds(long nanos) {
        return (nanos+NANOSECONDS_PER_SECOND/2)/NANOSECONDS_PER_SECOND;
    }

    public static long convertSecondsToNanos(long seconds) {
        return seconds * NANOSECONDS_PER_SECOND;
    }
    
    public static String formatMillis(long millis) {
        return formatTime(convertMillisToNanos(millis));
    }
    
    public static String formatDate(long nanos) {
        if (nanos == Clock.UNDEFINED_TIME) {
            return "undefined";
        }        
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(convertNanosToMillis(nanos)));
    }
    
    public static String formatTime(long nanos) {
        if (nanos == Clock.UNDEFINED_TIME) {
            return "undefined";
        }
        return formatTimeInSeconds(.000000001*(double)nanos);
    }
    
    public static String formatTimeInSeconds(double seconds) {
        DecimalFormat df;
        
        if (seconds < 0) {
            return "-"+formatTimeInSeconds(-seconds);
        }
        
        if (seconds >= 0.95) {
            df = new DecimalFormat(SECONDS_FORMAT);
            return df.format(seconds)+" sec.";
        }
        
        double millis = seconds * 1000.;
        
        // show only 2 significant figures and have an appropriate decimal point
        String fmt;
        if (millis >= 9.95) {
            if (millis >= 100.) {
                millis = Math.round(seconds * 1000. * 0.1) * 10.;
            }
            fmt = "0";
        } else if (millis >= 0.995) {
            millis = Math.round(seconds * 1000. * 10.) * .1;
            fmt = "0.0";
        } else {
            millis = Math.round(seconds * 1000. * 100.) * .01;
            fmt = "0.00";
        }
        df = new DecimalFormat(fmt);
        return df.format(millis)+" ms";
    }
    
    public static long convertMillisToNanos(long millis) {
        return millis*NANOSECONDS_PER_MILLISECOND;
    }

    public static double convertNanosToDoubleSeconds(long nanos) {
        return ((double)nanos)/((double)TimeConversion.NANOSECONDS_PER_SECOND);        
    }
    
    /**
     * 
     * @param totalTime total accumulated time, in nanoseconds
     * @param count count of number of requests
     * @return mean time per counted item in seconds: zero if totalTime is zero or count is zero
     * @throws NullPointerException if totalTime>0 and count==0
     */
    
    public static double meanNanosInSeconds(long totalTime, int count) {
        if (count==0) return 0.;
        return convertNanosToDoubleSeconds(totalTime)/(double)count;
    }
    
    public static String formatMeanNanos(long totalTime, int count) {
        if (count==0) {
            return "N/A";
        }
        return formatTimeInSeconds(meanNanosInSeconds(totalTime, count));
    }    
}
