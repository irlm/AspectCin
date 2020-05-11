package glassbox.monitor.resource;

import glassbox.monitor.AbstractMonitorClass;
import glassbox.monitor.ui.ServletRequestMonitor;
import glassbox.response.Response;
import glassbox.track.api.OperationDescription;

import java.io.Serializable;

import com.tc.hooks.StatsListener;
import com.tc.object.bytecode.*;

public class TerracottaMonitor extends AbstractMonitorClass {
    public TerracottaMonitor() {
        try {
            new TcStatsListener();
        } catch (NoClassDefFoundError err) {
            // not available
            logDebug("Terracotta monitor disabled: not on classpath.");
        } catch (Throwable t) {
            logError("Unxpected problem initializing Terracotta Monitor", t);
        }
    }
    
    protected Serializable getKey(Object locked) {
        System.err.println("Monitoring: "+locked);
        return ByteCodeUtil.generateAutolockName(((Manageable)locked).__tc_managed().getObjectID());
    }
    
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
    
    // inner class to allow use when TC not on system class path
    public class TcStatsListener implements StatsListener {
        public TcStatsListener() {
            ManagerUtil.registerStatsListener(this);
        }

        public void beginLockAquire(String lockId) {
            beginTc(lockId, "Distributed Lock");            
        }

        public void beginObjectFault(int oid) {
            beginTc(new Integer(oid), "Distributed Object Fault");            
        }

        public void beginTransactionCommit(String txnId) {
            beginTc(txnId, "Distributed Transaction");            
        }

        public void endLockAquire(String lockId) {
            endNormally("endLockAquire");
        }

        public void endObjectFault(int oid) {
            endNormally("endObjectFault");
        }

        public void endTransactionCommit(String txnId) {
            endNormally("endTransactionCommit");            
        }
        
        void beginTc(Object key, Object opType) {
            OperationDescription operation = operationFactory.makeRemoteOperation("Terracotta Monitor", opType.toString(), "");
            Response response = createResponse(operation, ServletRequestMonitor.JSP_PRIORITY);
            response.set(Response.PARAMETERS, new Object[] { key });
            response.start();
        }
    }
        
}
