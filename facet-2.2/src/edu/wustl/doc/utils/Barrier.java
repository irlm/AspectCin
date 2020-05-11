/*
 * $Id: Barrier.java,v 1.2 2003/08/26 19:49:49 ravip Exp $
 */

package edu.wustl.doc.utils;

/**
 * A simple synchronization barrier for multithreaded
 * test cases.  
 *
 * @author     Frank Hunleth
 * @version    $Revision: 1.2 $
 */
public class Barrier {

        private int count_;

        /**
         * Create a new Barrier.
         *
         * @param count The number of threads that need to wait
         *              on the barrier before it is released.
         */
        public Barrier (int count)
        {
                count_ = count;
        }

        public int getCount ()
        {
                return count_;
        }
        
        /**
         * Notify the Barrier that an initialization
         * has completed.
         *
         * @throws InterruptedException If the thread was interrupted.
         */
        synchronized public void waitOnBarrier() 
                throws InterruptedException
        {
                count_--;

                if (count_ <= 0) {
                        notifyAll();
                } else {
                        wait();
                }
        }
}





