/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.*;
import glassbox.util.ConfigurationException;

import java.util.Stack;


public class ThreadStats extends ThreadLocal {
	private StatisticsRegistry registry;
    
    private class StatsState {
        public Stack stack = makeStack(); // initializing this in the constructor gives an NPE down the line... I don't have to investigate
        public OperationPerfStats firstOperationKey;
        public OperationPerfStats lastOperationKey;

        //fails:
//        public StatsState() {
//            stack = new Stack();
//            stack.push(registry);
//        }            
        private Stack makeStack() {
            Stack stack = new Stack();
            stack.push(registry);
//            System.err.println("made one "+this+", "+Thread.currentThread()+" with "+stack+", reg = "+registry);
            return stack;
        }            
        
        public PerfStats pop() {
             if (stack.size() <= 1) {
                throw new IllegalArgumentException("Trying to pop an empty stack");
            }
            return (PerfStats)stack.pop();
        }
        
        public void reset() {
            // remove the operations keys, if any
            firstOperationKey = null;
            lastOperationKey = null;
        }            
    };
	
	public Object initialValue() {
        return new StatsState();
	}
	
	public PerfStats push(PerfStats perfStats) {
		getStack().push(perfStats);
		return perfStats;
	}
	
	/**
	 * it's an error to pop an the top-level system registry off the stack!
	 * 
	 * @return
	 */
	public PerfStats pop() {
	    StatsState state = (StatsState)get();
        return state.pop();
    }
	
    public void reset() {
        StatsState state = (StatsState)get();
        state.reset();
        if (isDebugEnabled()) logDebug("Reset operation keys");
    }
    
	public StatisticsRegistry peek() {
        Stack stack = getStack();
        Object peeked = stack.peek();
        return (StatisticsRegistry)peeked;
//		return (StatisticsRegistry)getStack().peek();
	}
	
	protected Stack getStack() {
		return ((StatsState)get()).stack;
	}
    
    public OperationPerfStats getFirstOperationKey() {
        StatsState state = (StatsState)get();
        return state.firstOperationKey;
    }

    public void setFirstOperationKey(OperationPerfStats operationsKey) {
        StatsState state = (StatsState)get();
        state.firstOperationKey = operationsKey;
        if (isDebugEnabled()) logDebug("Set first key to "+operationsKey);
    }
	
    public OperationPerfStats getLastOperationKey() {
        StatsState state = (StatsState)get();
        return state.lastOperationKey;
    }

    public void setLastOperationKey(OperationPerfStats operationsKey) {
        StatsState state = (StatsState)get();
        state.lastOperationKey = operationsKey;
        if (isDebugEnabled()) logDebug("Set last key to "+operationsKey);
    }
    
    public void validate() {
        String errors = null;
        if (registry == null) {
            errors = (errors==null ? "" : errors+"; ");
            errors += "no registry defined";
        }
        if (errors != null) {
            throw new ConfigurationException(errors);
        }
    }

    /**
     * Note this doesn't have the desired effect if you call it AFTER initializing a stack for any threads.
     * We don't go back and reset the threads. 
     *  
     * @param systemRegistry
     */
	public void setRegistry(StatisticsRegistry systemRegistry) {
		registry = systemRegistry;
	}
    
    public StatisticsRegistry getRegistry() {
        return registry;
    }

//    private static aspect TraceToInvestigateNpeInCtor {
//        after(ThreadStats threadStats) returning (Object o): execution(* *(..)) && this(stack) && !cflow(adviceexecution() && within(ThreadStats)) {
//            System.err.println("Executed "+thisJoinPoint.toShortString()+" on "+Thread.currentThread()+" with "+stack+" having "+stack.get()+" with "+((StatsState)stack.get()).stack+" returns "+o);
//        }
//    }
}
