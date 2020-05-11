package edu.wustl.doc.facet.EventChannelAdmin;

public interface PerformanceCounters {

	long counter (String counter_name);

	Counter [] all_counters ();

}
