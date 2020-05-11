/**
 * 
 */
package glassbox.track.api;

import java.util.*;

class ArrayOfMapIterator implements Iterator {
    private int outerCursor;
    private Map[] outer;
    private Iterator innerIterator;
    
    public ArrayOfMapIterator(Map[] data) { 
        this.outer = data;
        outerCursor = -1;
        if (outerCursor+1<data.length) {
            getNextInner();
        }
    }
    
    public boolean hasNext() {
        if (innerIterator == null) {
            // impossible case
            return false;
        } else {
            while (!innerIterator.hasNext() && (outerCursor+1<outer.length)) {
                getNextInner();
            }
            return innerIterator.hasNext();
        }            
    }
    
    public Object next() {
        if (hasNext()) {
            return innerIterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /** not tested */
    public void remove() {
        innerIterator.remove();
    }
    
    private void getNextInner() {
        innerIterator = outer[++outerCursor].entrySet().iterator();
    }
    private static final long serialVersionUID = 1L;
}