/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
*
* This file is part of the design patterns project at UBC
*
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
*
* Software distributed under the License is distributed on an "AS IS" basis,
* WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
* for the specific language governing rights and limitations under the
* License.
*
* The Original Code is ca.ubc.cs.spl.aspectPatterns.
* 
* For more details and the latest version of this code, please see:
* http://www.cs.ubc.ca/labs/spl/projects/aodps.html
*
* Contributor(s):   
*/

import java.util.*;

/**
 * Defines simple behavior for the Observer design pattern. This version was
 * inspired by the UBC project for reusable design patterns by Hanneman and
 * Kiczales, but unlike that implementation this one is simpler, by using
 * inter-type declarations on the Subject instead of requiring interaction with
 * an aspect to configure observation. The drawback of doing this is that a subject
 * can only participate in a single observing relationship by default (although with
 * additional advice it can participate in more).
 *
 * Each concrete sub-aspect of ObserverProtocol defines a canonical observing
 * relationship.  Within that kind of relationship, there can be any number
 * of <i>Subject</i>s, each with any number of <i>Observer</i>s.
 *
 * The sub-aspect defines three things: <ol>
 *
 *   <li> what types can be <i>Subject</i>s or <i>Observers</i> <br>
 *        this is done using declare parents.
 *
 *   <li> what operations on the <i>Subject</i> require updating the observers <br>
 *        this is done by concretizing the subjectChange(Subject) pointcut
 *
 *   <li> how to update the observers <br>
 *        this is done by defining a method on
 *        Subject.updateObserver(Observer) 
 * </ol>
 *
 * Note that in this implementation, the work of updating is an ITD method
 * on the observer.  This simplifies the common case.
 * 
 * Based on the library aspect from:
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @author  Ron Bodkin
 * @version 1.0
 */
 
public abstract aspect SimpleObserverProtocol {  
    
    
    /**
     * This interface is used by extending aspects to say what types
     * can be <i>Subject</i>s. It models the <i>Subject</i> role.
     */

    protected interface Subject  { 
        List getObservers(); 
        void removeAllObservers();
        void addObserver(Observer observer); 
        void removeObserver(Observer observer);
        void updateObserver(Observer observer);
    }    


    /**
     * This interface is used by extending aspects to say what types
     * can be <i>Observer</i>s. It models the <i>Observer</i> role.
     */

    protected interface Observer {} 


    /**
     * Stores the mapping between <i>Subject</i>s and <i>
     * Observer</i>s. For each <i>Subject</i>, a <code>LinkedList</code>
     * is of its <i>Observer</i>s is stored.
     */
    
    private List Subject.observers = new LinkedList();


    /**
     * Returns a <code>Collection</code> of the <i>Observer</i>s of 
     * a particular subject. Used internally.
     *
     * @param subject the <i>subject</i> for which to return the <i>Observer</i>s
     * @return a <code>Collection</code> of s's <i>Observer</i>s
     */

    public List Subject.getObservers() { 
        return observers;
    }

    
    /**
     * Adds an <i>Observer</i> to a <i>Subject</i>. This is the equivalent of <i>
     * attach()</i>, but is a method on the pattern aspect, not the 
     * <i>Subject</i>. 
     *
     * @param s the <i>Subject</i> to attach a new <i>Observer</i> to
     * @param o the new <i>Observer</i> to attach
     */ 
     
    public void Subject.addObserver(Observer observer) { 
        getObservers().add(observer);    
    }
    
    /**
     * Removes an observer from a <i>Subject</i>. This is the equivalent of <i>
     * detach()</i>, but is a method on the pattern aspect, not the <i>Subject</i>. 
     *
     * @param s the <i>Subject</i> to remove the <i>Observer</i> from
     * @param o the <i>Observer</i> to remove
     */ 
    
    public void Subject.removeObserver(Observer observer) { 
        getObservers().remove(observer); 
    }

    public void Subject.removeAllObservers() {
        getObservers().clear();
    }
    
    /**
     * The join points after which to do the update.
     * It replaces the normally scattered calls to <i>notify()</i>. To be
     * concretized by sub-aspects.
     */ 
     
    protected pointcut subjectChange(Subject s);

    /**
     * Calls <code>updateObserver(..)</code> after a change of interest to 
     * update each <i>Observer</i>.
     *
     * @param subject the <i>Subject</i> on which the change occured
     */

    after(Subject subject) returning: subjectChange(subject) {
        Iterator iter = subject.getObservers().iterator();
        while ( iter.hasNext() ) {
            subject.updateObserver(((Observer)iter.next()));
        }
    } 
    
   /**
     * Defines how each <i>Observer</i> is to be updated when a change
     * to a <i>Subject</i> occurs. To be concretized by sub-aspects.
     *
     * @param subject the <i>Subject</i> on which a change of interest occured
     * @param observer the <i>Observer</i> to be notifed of the change  
     */

    public void Subject.updateObserver(Observer observer) {}
}
