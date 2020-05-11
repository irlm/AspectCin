/*
 * $Id: InitializationBarrier.java,v 1.1 2002/09/28 19:58:29 ravip Exp $
 */

package edu.wustl.doc.utils;

/**
 * A simple initialization synchronization barrier for multithreaded
 * test cases.  This enables client threads to wait until their 
 * servers have been initialized before running.
 *
 * @author     Frank Hunleth
 * @version    $Revision: 1.1 $
 */
public class InitializationBarrier {
    private int count_;

    /**
     * Create a new InitializationBarrier.
     *
     * @param count The number of completed notifications that
     *              are needed before releasing the waiters.
     */
    public InitializationBarrier(int count) {
        count_ = count;
    }
    
    /**
     * Notify the InitializationBarrier that an initialization
     * has completed.
     */
    synchronized public void initCompleted() {
        count_--;

        if (count_ <= 0) {
          notifyAll();
        }
    }
    
    /**
     * Wait for all of the initializations to complete.
     *
     * @throws InterruptedException If the thread was interrupted.
     */
    synchronized void waitForInitialization() 
        throws InterruptedException {
        while (count_ > 0) {
            wait();
        }
    }
}
