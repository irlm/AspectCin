/*
 * $Id: FilterNode.java,v 1.1 2002/09/28 19:58:29 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Interface for all filter elements.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public interface FilterNode {

    /*
     * pushEvent returns a binary combination of following values
     * indicating whether the event node fired and whether it should
     * be saved.
     */
    int NOMATCH = 0;
    int SAVE = 1;
    int FIRE = 2;

    /*
     * Specify an event number that will never be used to simplify coding.
     */
    int INVALID_EVENT_NUM = -1;
    int FIRST_EVENT_NUM = 0;

    /**
     * Push a new event to this filter.
     *
     * @param ec an <code>EventCarrier</code> value
     * @param eventnum a unique number that would be assigned to the
     * current event if it were to fire.
     * @return int a combination of SAVE and FIRE for how to handle
     *         the event */
    public int pushEvent(EventCarrier ec, int eventnum);
}
