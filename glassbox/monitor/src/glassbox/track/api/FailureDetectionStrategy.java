/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


public interface FailureDetectionStrategy {
    public static final int NORMAL = 0;
    public static final int WARNING = 1;
    public static final int FAILURE = 2;
    public static final int NUM_LEVELS = 3;

    /**
     * This method is going to be replaced... instead the strategy should be getting a request object
     * and have access to a variety of relevant context parameters.
     * 
     * @param t - Throwable for this failure. Won't be null, but non-exceptions may also be converted to failure descriptions, just not through this API
     * @return
     */
    FailureDescription getFailureDescription(Throwable t);
}
