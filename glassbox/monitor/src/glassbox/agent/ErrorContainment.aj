/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.agent;

import java.util.HashMap;
import java.util.Map;

import glassbox.util.MutableInteger;

/***
 * Policy enforcement aspect that prevents exceptions from bubbling up into monitored code.
 */
// should pass this information "up" that the monitor data is poisoned
public aspect ErrorContainment {
    // favor safety over efficiency: containment even applies to the advice that prevents disabled monitors from running
    declare precedence: ErrorContainment, glassbox.monitor.AbstractMonitorControl.RuntimeControl;

    /** Map of bottom-most stack trace elements that we've ever seen before */
	private Map previousFailures = new HashMap(); // if this is a material leak, we're in big trouble!
	
	// public static interface DontContainErrors {}
    
	// around/pass through exceptions?
    /** we apply error containment to any agent code, i.e., code that interacts with external code (applications, servers, etc.) */
	public pointcut scope() : 
		(within(glassbox.monitor..*) || within(glassbox.summary..*)|| within(glassbox.agent..*) || within(glassbox.config..*)) && 
        !within(ErrorContainment);// && !within(DontContainErrors+); && !within(NondelegatingURLClassLoader);

	// if there's a checked exception, we will pass it through
    // we will need to exclude "root" unchecked exceptions
    // this doesn't deal with - this advice can only apply to before/after...
    // kind of lame: work-around for compiler not able to pick out advice that returns void statically...
    // we rely on a naming pattern to exclude advice that doesn't match: write a static inner aspect whose name
    // is *Around, and then code carefully!
	Object around() : adviceexecution() && scope() && !within(*..*Around) { //&& if(((AdviceSignature)thisJoinPointStaticPart.getSignature()).getReturnType() == void.class){
		try {
			return proceed();
		} catch (Error e) {
			handle(e);
		} catch (RuntimeException rte) {
			handle(rte);
		}
		return null; // will only be void
	}
	
	declare error: scope() && call((Throwable+ && !Exception+ && !Error+).new(..)): 
		"don't create random throwables in the monitor code!";
	
    pointcut executionNotDeclaringCheckedException(): 
        execution(* *(..)) && !execution(* *(..) throws Exception+) && !execution(* *(..) throws Throwable);
    
    pointcut eitherExecution() : executionNotDeclaringCheckedException() || adviceexecution();       
    
    declare soft: Exception+: eitherExecution() && scope();
    
	/** log and swallow */
	private void handle(Throwable t) {
        boolean shouldLog = true;
        String desc = "";
        
        try {
    		StackTraceElement elt = t.getStackTrace()[0];
    		MutableInteger val = (MutableInteger)previousFailures.get(elt);
    		if (val != null) {
    			val.setValue(val.getValue()+1);
    		} else {
    		    val = new MutableInteger(1);
                previousFailures.put(elt, val);
            }
    		shouldLog = (val.getValue() % 1000 == 1);
            if (shouldLog) {
                desc = (val.getValue()>1 ? "( "+val.getValue() + ") times" : "");
            }
        } catch (Throwable rte) {
            // swallow concurrent modifications, etc.
        }
        
        //basically inlining a cflow test for a single point for efficiency...
        if (shouldLog) {
            // try using the logger, but don't recurse infinitely if that errors out!
            try {
                if (loggingError.get() == null) {
                    loggingError.set("recurse");
                    logError("failure "+t.getMessage()+desc+": ", t);
                }
            } catch (Throwable cantLogError) {
                System.err.println("failure in monitoring aspect "+desc);
                t.printStackTrace();
            } finally {
                loggingError.set(null);
            }
        }
	}
        
    /** holds null if not logging an error, or non-null if already logging an error on the thread */
    private ThreadLocal loggingError = new ThreadLocal();
}
