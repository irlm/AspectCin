/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.FailureDescription;

import java.util.List;

/**
 * A failure analysis provides more details about what happened during a failure. Many problems
 * return a list of these analyses from getEvents() 
 * 
 * @author Ron Bodkin
 *
 */
public interface FailureAnalysis {
    FailureDescription getFailure();

    int getCount();

    List getRelatedURLs();
    
    List getEvents();
}
