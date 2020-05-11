/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import glassbox.policy.ContractChecking;
import glassbox.track.api.StatisticsRegistry;

import java.util.Iterator;
import java.util.Map.Entry;

public aspect EnforceContractsInTest {
    // in any code that has test source included, enforce contracts
    before() : staticinitialization(ContractChecking) {
        System.setProperty(ContractChecking.class.getName(), "true");
    }

    // this contract is too expensive to enforce in production code!
    after(StatisticsRegistry registry) returning: execution(* StatisticsRegistry.*(..)) && this(registry) && !cflow(adviceexecution() && within(EnforceContractsInTest)) {
        for (Iterator it=registry.getEntries(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            if (entry.getValue() == null) {
                throw new IllegalStateException("Exiting "+thisJoinPointStaticPart+" with null stats for "+entry.getKey());
            }
        }
    }
}
