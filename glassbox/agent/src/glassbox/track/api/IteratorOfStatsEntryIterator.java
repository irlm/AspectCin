/**
 * 
 */
package glassbox.track.api;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

class IteratorOfStatsEntryIterator implements Iterator {
    private final StatisticsType type;
    private Iterator children; 
    private Iterator childIterator;
    private Object nextChild = null;

    public IteratorOfStatsEntryIterator(StatisticsType type, Iterator children) {
        this.type = type;
        this.children = children;
    }
    
    public boolean hasNext() {
        while (nextChild==null && children.hasNext() && (childIterator==null || !childIterator.hasNext())) {
            advance();
        };
        return (nextChild != null) || (childIterator!=null && childIterator.hasNext());
    }

    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (nextChild != null) {
            Object next=nextChild;
            nextChild=null;
            return next;
        }
        return childIterator.next();
    }

    public void remove() {
        if (childIterator != null) {
            childIterator.remove();
        } else {
            throw new IllegalStateException();
        }                
    }

    private void advance() {
        Entry entry = (Entry)children.next();
        PerfStats stats = (PerfStats)entry.getValue();
        if (stats.getType() == type) {
            nextChild = entry;
        } else if (stats instanceof CompositePerfStats) {
            childIterator = ((CompositePerfStats)stats).getEntriesForType(type);
        }
    }
}