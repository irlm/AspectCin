/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.util.Comparator;


/**
 * Requests represent a specific instance of a request to something. They can be compared based on elapsed time.
 * 
 * @author Ron Bodkin
 *
 */
public interface Request extends Comparable {

    /** 
     * Canonical String description of request, e.g., dynamic SQL, prepared SQL (without parameter values),
     * request URL (with GET parameters but not POST parameters). 
     */
    String getRequestString();
    
    /** Description only of parameters not encoded in request string (i.e., POST parameters in URL or prepared */
    String getParameterString();

    /** Relatively self-contained string description for logging, e.g., Slow query: select * from foo where id=?, parameters=1234 */
    String getDescription();
    
    Request copy();
    
    long getElapsedTime();
    void setElapsedTime(long elapsedTime);
    
    long getLastTime();
    void setLastTime(long elapsedTime);
    
    /** 
     * The parameters in a request might be a shared collection that changes when finished processing the query, Web request, etc.
     * This operation says to make a long-lived copy.  
     */
    void cloneParameters();
    
    static final Comparator END_TIME_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            Request r1 = (Request)o1;
            Request r2 = (Request)o2;
            
            // sort descending
            if (r1.getLastTime() > r2.getLastTime()) {
                return -1;
            } else if (r1.getLastTime() < r2.getLastTime()) {
                return 1;
            } else {
                // we want a total ordering...
                return r1.compareTo(r2);
            }
        }
    };

}
