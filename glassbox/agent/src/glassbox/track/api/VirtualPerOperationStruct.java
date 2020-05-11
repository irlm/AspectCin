/**
 * 
 */
package glassbox.track.api;

class VirtualPerOperationStruct extends PerOperationStruct {

    public VirtualPerOperationStruct(PerfStats stats) {
        super(stats);
    }

    /* (non-Javadoc)
     * @see glassbox.track.api.PerfStatsImpl.PerOperationStruct#add(glassbox.track.api.PerfStatsImpl.PerOperationStruct)
     */
    public void add(PerOperationStruct otherOperation) {
        super.add(otherOperation);
        
        accumulatedRequestTime += otherOperation.accumulatedRequestTime;
        accumulatedRequestCount += otherOperation.accumulatedRequestCount;            
    }
    
    public void subtract(PerOperationStruct otherOperation) {
        super.subtract(otherOperation);

        accumulatedRequestTime -= otherOperation.accumulatedRequestTime;
        accumulatedRequestCount -= otherOperation.accumulatedRequestCount;            
    }
    
}