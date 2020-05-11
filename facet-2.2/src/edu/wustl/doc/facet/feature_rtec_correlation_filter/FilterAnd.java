package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * All subtrees need to fire before this node fires.
 *
 * Note: Semantics will need to be improved.  This one is mostly
 *       proof of concept right now.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterAnd implements FilterNode {
    
    private FilterNode [] children_;
    private int countOfChildren_;
    
    public FilterAnd (int count, FilterNode [] children) {
        countOfChildren_ = count;
        children_ = children;
      }
    
    public int pushEvent(EventCarrier ec, int eventnum) {

        int finalRetVal = 0;
        int childrenRetVal = 0;
        int returnValue = 0;
        
        for (int i = 0; i != countOfChildren_; i++) {
            returnValue = children_ [i].pushEvent (ec, eventnum);
            finalRetVal = finalRetVal | returnValue;
            if ( i == 0)
                childrenRetVal = returnValue;
            else
                childrenRetVal = childrenRetVal & returnValue;
        }

        
         if ((childrenRetVal & FIRE) == 0)
             finalRetVal = finalRetVal & ~FIRE;


        return finalRetVal;
                                    

    }

}
