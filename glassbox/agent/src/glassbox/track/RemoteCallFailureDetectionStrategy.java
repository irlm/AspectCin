/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;


import glassbox.track.api.DefaultFailureDetectionStrategy;

import java.rmi.RemoteException;

public class RemoteCallFailureDetectionStrategy extends DefaultFailureDetectionStrategy {
    private static final long serialVersionUID = 1;

    public int getSeverity(Throwable t) {
        if (t instanceof RemoteException || t instanceof RuntimeException || !(t instanceof Exception)) {
            return FAILURE;
        }
        return WARNING;
    }
    
}
