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

import glassbox.track.api.OperationDescription;
import glassbox.track.api.PerfStatsImpl;

public class GuiFriendlyStatsJmxNameStrategy implements StatsJmxNameStrategy {
    
    public int appendOperationName(PerfStatsImpl stats, StringBuffer buffer) {        
        PerfStatsImpl parent = (PerfStatsImpl)stats.getParent();
        int depth;
        if (parent != null) {
            depth = 1 + appendOperationName(parent, buffer);
            buffer.append(',');
        } else {
            if (stats.getKey() instanceof OperationDescription) {
                OperationDescription oDesc = (OperationDescription)stats.getKey();
                if (oDesc.getContextName() != null) {
                    buffer.append("application=\"");
                    int pos = buffer.length();
                    buffer.append(oDesc.getContextName());
                    DefaultJmxServerManager.jmxEncode(buffer, pos);
                    buffer.append("\",");
                }
            }
            depth = 0;
        }
        buffer.append(stats.getLayer());
        buffer.append(depth);
        buffer.append('=');
        stats.appendName(buffer);
        return depth;
    }
}
