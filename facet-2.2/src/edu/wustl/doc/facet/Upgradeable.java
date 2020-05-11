/*
 * $Id: Upgradeable.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet;

/**
 * Any class that uses the event channel and wants to be upgraded
 * automatically to support a new feature should implement this
 * interface.  As a part of the contract of using this interface, the
 * class should also implement all of the "feature" interfaces that it
 * supports without modification.  The list can just be the minimal set.
 *
 */
public interface Upgradeable { }
