# $Id: popular_subsets.py,v 1.1 2003/07/10 21:40:31 ravip Exp $

# !! in src/edu/wustl/doc/facet !!

# Prints out size information for popular subsets
import combo
import find_combo

popular_feature_sets=[
    ['Base',],
    ['Cos Event Service (push only)','use_event_any'],
    ['3','use_eventbody_any','use_corba_ttl','use_corba_eventvec'],
    ['4','use_eventbody_octetseq','use_eventtype_filter','use_supplier_dispatch'],
    ['5','use_eventbody_any','use_corba_ttl','use_corba_eventvec',
        'use_eventtype_filter','use_corba_eventtype','use_supplier_dispatch',
        'use_event_pull'],
    ['6','use_eventbody_octetseq','use_eventbody_any','use_corba_timestamp',
        'use_filter'],
    ['7','use_eventbody_any','use_eventbody_string','use_filter'],
    ['Large config','use_eventbody_any','use_corba_ttl','use_corba_eventvec',
        'use_eventtype_filter','use_corba_eventtype','use_supplier_dispatch',
        'use_event_pull','use_basic_counters','use_corba_timestamp',
        'use_filter'],
    ['Large config w/ tracing','use_eventbody_any','use_corba_ttl',
        'use_corba_eventvec',
        'use_eventtype_filter','use_corba_eventtype','use_supplier_dispatch',
        'use_event_pull','use_basic_counters','use_corba_timestamp',
        'use_filter','use_tracing'],
    ['CosEventService (push only) w/ tracing','use_event_any','use_tracing'],
    ]

def get_popular_subsets():
    'Returns a list of combination indices of the popular subsets'
    subsets=[]
    for feature_set in popular_feature_sets:
        ids=find_combo.names_to_feature_ids(feature_set[1:])
        matches=find_combo.find_smallest_combos(ids)
        if len(matches)>0:
            subsets.append(matches[0])
    return subsets

if __name__ == "__main__":
    # Print out popular subsets
    for feature_set in popular_feature_sets:
        ids=find_combo.names_to_feature_ids(feature_set[1:])
        matches=find_combo.find_smallest_combos(ids)
        if len(matches)>0:
            print '%s => %d' % (feature_set[0],matches[0])
