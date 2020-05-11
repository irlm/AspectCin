package glassbox.monitor;

import glassbox.response.ResponseFactory;

public abstract aspect AbstractMonitorControl extends AbstractMonitorClass {
    public static aspect RuntimeControl perthis(within(AbstractMonitorClass+) && execution(new(..))) {
        protected boolean enabled = true;
        protected static ThreadLocal/*<Boolean>*/ requestEnabled = new ThreadLocal() {
            public Object initialValue() {
                return allDisabled ? Boolean.FALSE : Boolean.TRUE;
            }
        };
        protected static ThreadLocal/*<int[]>*/ requestCounter = new ThreadLocal() {
            public Object initialValue() {
                return new int[] { 0 };
            }
        };

        // unit of work monitors have to be able to enable & disable
        void around() : within(AbstractMonitor+) && adviceexecution() {
            if (enabled && (requestEnabled.get() == Boolean.TRUE)) {
                proceed();
            } else {                
                logDebug("Skipping monitoring of disabled request (it started before Glassbox was initialized)");
            }
        }
        
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }                

        protected static void enter() {
            if (updateCounter(+1)==1) {
                // ensure consistent processing 
                requestEnabled.set(allDisabled ? Boolean.FALSE : Boolean.TRUE);
            }
        }
        
        protected static void exit() {
            updateCounter(-1); 
        }
        
        private static int updateCounter(int incr) {
            int val = ((int[])requestCounter.get())[0] += incr;
            return val;
        }
       
    };
    
    protected pointcut topLevelPoint();
    
    before() : topLevelPoint() {
        RuntimeControl.enter();
    }
    
    after() : topLevelPoint() {
        RuntimeControl.exit();
    }
}
