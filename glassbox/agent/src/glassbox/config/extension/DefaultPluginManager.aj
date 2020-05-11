package glassbox.config.extension;

import glassbox.analysis.api.*;
import glassbox.config.extension.api.*;
import glassbox.track.OperationTracker;
import glassbox.track.api.OperationDescription;

import java.util.Collection;
import java.util.Iterator;

public aspect DefaultPluginManager extends QueueRegistrationsRegistry {
    public pointcut getOperations() : OperationTracker.gettingOperations();
    public pointcut analyzeOperation(OperationDescription operation, OperationTracker tracker) : 
        within(OperationTracker+) && execution(public OperationAnalysis analyze(..)) && args(operation) && this(tracker);

    public DefaultPluginManager() {
        PluginRegistryLocator.setRegistry(this);
    }
    
    Object around() : getOperations() {
        Collection result = (Collection)proceed();
        for (Iterator it=operationPlugins.values().iterator(); it.hasNext();) {
            OperationPlugin plugin = (OperationPlugin)it.next();
            Collection added = plugin.getOperations();
            if (added != null) {
                for (Iterator inner=added.iterator(); it.hasNext();) {
                    OperationSummary summary = (OperationSummary)inner.next();
                    summary.getOperation().setPlugin(plugin); 
                }
                result.addAll(added);
            }
        }
        return result;
    }
    
    OperationAnalysis around(OperationDescription operation, OperationTracker tracker) : analyzeOperation(operation, tracker) {
        if (operation.getPlugin() == null) {
            return proceed(operation, tracker);
        } else {            
            OperationAnalysis result = operation.getPlugin().analyze(operation, tracker.getStartTime());
            if (result.problems() != null) {
                for (Iterator it=result.problems().iterator(); it.hasNext();) {
                    ProblemAnalysis problem = (ProblemAnalysis)it.next();
                    problem.setPlugin(operation.getPlugin());
                }
            }
            return result;
        }
    }
}
