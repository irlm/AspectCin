/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.agent.api;

/** 
 * By default, any types defined in a glassbox API package are serializable. 
 * We use this interface to explicitly mark exceptions to that rule.
 */
public interface NotSerializable {
}
