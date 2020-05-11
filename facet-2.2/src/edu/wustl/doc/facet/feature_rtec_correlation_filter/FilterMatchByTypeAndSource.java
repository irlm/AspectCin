package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Match an event based on the type and source fields.
 *
 */
public class FilterMatchByTypeAndSource implements FilterNode {

	private int typeToMatch_;
	private int sourceToMatch_;
	
	private int lastFired_ = INVALID_EVENT_NUM;

	public FilterMatchByTypeAndSource (int type, int source)
	{
                typeToMatch_ = type;
		sourceToMatch_ = source;
	}

	public int pushEvent (EventCarrier ec, int eventnum)
	{
		int retval = NOMATCH;

		if (lastFired_ != eventnum) {
			int type = ec.getEvent().getHeader().getType();
			int source = ec.getEvent ().getHeader().getSource();

			if (type == typeToMatch_ && source == sourceToMatch_) {
				/* Save away that we've matched for this particular
				 * event so that if we're asked again, we can respond
				 * quickly.
				 */
				lastFired_ = eventnum;
				retval = SAVE;
			}
		}

		return (lastFired_ == eventnum) ? (retval | FIRE) : retval;
	}
}
