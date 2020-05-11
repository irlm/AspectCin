/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.agent.api;

import java.io.Serializable;

/**
 * Marker interface for any type that is eligible to be included in the remote client API.
 * 
 * @see glassbox.policy.ApiPolicy, which enforces the rule that API elements implement this,.  
 * 
 * @author Ron Bodkin
 *
 */
public interface ApiType extends Serializable {
}
