/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator;

import glassbox.acceptance.ServletRunner;
import glassbox.simulator.ui.MockServletThreadContention;
import glassbox.test.MockServlet;
import glassbox.test.TimingTestHelper;

public class MultithreadedRunner {

    public Thread[] createContending(int nThreads, final long totalTime) {
        final RunnableFactory factory = new RunnableFactory() {
            public Runnable create(int nThreads) {
                MockServlet servlet = new MockServletThreadContention(totalTime/nThreads);
                return new ServletRunner(servlet, 1);
            }
        };
        return createThreads(nThreads, factory);
    }
    
    // this is the kind of code where a Jython with closures could be useful...
    public Thread[] createThreads(int nThreads, RunnableFactory factory) {
        Thread[] threads = new Thread[nThreads];
        for (int i=0; i<nThreads; i++) {
            threads[i] = new Thread(factory.create(nThreads), "instance "+i);
        }
        return threads;
    }

    public Thread[] createThreads(int nThreads, final Runnable runnable) {
        return createThreads(nThreads, new RunnableFactory() {
            public Runnable create(int nThreads) {
                return runnable;
            }
        });
    }
    
    public void start(Thread[] threads) throws InterruptedException {
        for (int i=0; i<threads.length; i++) {
            threads[i].start();
        }
    }
    
    public void join(Thread[] threads) throws InterruptedException {
        for (int i=0; i<threads.length; i++) {
            threads[i].join();
        }
    }
    
    public void run(Thread[] threads) throws InterruptedException {
        start(threads);
        join(threads);
    }

    public static interface RunnableFactory {
        Runnable create(int nThreads);
    }

}
