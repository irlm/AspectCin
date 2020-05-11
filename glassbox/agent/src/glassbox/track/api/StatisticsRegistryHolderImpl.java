package glassbox.track.api;

import org.springframework.beans.factory.DisposableBean;

public class StatisticsRegistryHolderImpl implements DisposableBean {

    private StatisticsRegistry local;
    private StatisticsRegistry remote;
    private boolean alive = true;
    
    public StatisticsRegistry getLocal() {
        return local;
    }

    public void setLocal(StatisticsRegistry local) {
        this.local = local;
    }

    public StatisticsRegistry getRemote() {
        return remote;
    }

    public void setRemote(StatisticsRegistry remote) {
        this.remote = remote;
    }

    public StatisticsRegistryHolderImpl() {
        Thread updater = new Thread("stats updater") {
            public void run() {
                do {
                    try {
                        Thread.sleep(5000);
                        if (local != null) {
                            copy();
                        }
                    } catch (Throwable t) {
                        logError("failure:", t);
                    }
                } while(alive);
            }
        };
        updater.setDaemon(true);
        updater.start();
    }
    
    public synchronized void copy() {
        remote = local.cloneRegistry();
    }

    public void destroy() throws Exception {
        alive = false;
    }
    
    
}
