/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.util.timing.Clock;

aspect ResponseInvariants {
    public pointcut requireStartTime(Response response) : 
        (execution(* fail()) || execution(* complete()) || execution(* fail())) && this(response);

    before(Response response) : requireStartTime(response) {
        if (response.getStart() == Clock.UNDEFINED_TIME) {
            throw new IllegalStateException("Can't update before starting");
        }
    }
}
