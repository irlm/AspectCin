/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.util.*;
import java.util.Map.Entry;

// finishing up: request vs. problem event
// ensure proper cardinality in basic abstractions...

public abstract class AbstractProblemEvent implements Request {

    private Map parameters;
    private String description;

    public AbstractProblemEvent(String description, Map parameters) {
        this.parameters = parameters == null ? null : Collections.unmodifiableMap(new HashMap(parameters));
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Map getParameters() {
        return parameters;
    }

    public String describeParameters() {
        if (parameters == null) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (Iterator paramIt = parameters.entrySet().iterator(); paramIt.hasNext();) {
            Entry entry = (Entry) paramIt.next();
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(entry.getKey().toString());
            buffer.append("=");
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(value);
            }
        }
        return buffer.toString();
    }

}
