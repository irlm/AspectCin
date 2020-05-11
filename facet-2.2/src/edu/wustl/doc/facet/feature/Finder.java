/*
 * $Id: Finder.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

/**
 * This interface is used to find items inside of data structures
 * without having to expose the structure of the items to the data
 * structure nor have to export external iterators.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
*/
public interface Finder {

    /**
     * Called on each object in a set.  Return true if this is the desired one.
     *
     * @param o a candidate object
     * @return true if this is the right one.
     */
    public boolean check(Object o);
}
