package glassbox.simulator;

import glassbox.test.DelayingRunnable;

public class MockComponent extends DelayingRunnable {
    
    public void doSlow(int len, String tag) {
        helper();
    }
    
    private void helper() {
        run();
    }
}
