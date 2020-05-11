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

public class CanonicalStatsJmxNameStrategy implements StatsJmxNameStrategy {
    /** Determine JMX operation name for this performance statistics bean. */        
    public int appendOperationName(PerfStatsImpl stats, StringBuffer buffer) {
        buffer.append("Type=\"");
        int depth = appendTypeName(stats, buffer);

        buffer.append('"');
        appendParentType(stats, buffer);
        buffer.append(",name=");
        stats.appendName(buffer);
        return depth + 1;
    }
    
    public int appendTypeName(PerfStatsImpl stats, StringBuffer buffer) {
        int depth;
        PerfStatsImpl parent = (PerfStatsImpl)stats.getParent();
        if (parent != null) {
            depth = appendTypeName(parent, buffer);
            buffer.append('.');
        } else {
            buffer.append("Application.");
            depth = 0;
        }
        buffer.append(stats.getLayer());
        return depth;
    }
    
    private void appendParentType(PerfStatsImpl stats, StringBuffer buffer) {
        PerfStatsImpl parent = (PerfStatsImpl)stats.getParent();
        if (parent != null) {
            appendParentType(parent, buffer);
            buffer.append(',');
            appendTypeName(parent, buffer);
            buffer.append("=");
            parent.appendName(buffer);
        } else {
            OperationDescription oDesc = (OperationDescription)stats.getKey(); 
            String contextName = oDesc.getContextName();
            if (contextName != null) {
                final String APPLICATION_ATTR = ",Application=\"";
                buffer.append(APPLICATION_ATTR);
                int pos = buffer.length();
                buffer.append(contextName);
                DefaultJmxServerManager.jmxEncode(buffer, pos);
                buffer.append('"');
            }
        }
    }
    
}
