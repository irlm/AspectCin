/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.util.jmx;

import glassbox.track.api.PerfStatsImpl;

/**
 * Allows defining different strategies for naming statistics in JMX.
 * 
 * @author Ron Bodkin
 *
 */
public interface StatsJmxNameStrategy {
    /**
     * 
     * @param stats
     * @param buffer
     * @return depth
     */
    int appendOperationName(PerfStatsImpl stats, StringBuffer buffer);
}
