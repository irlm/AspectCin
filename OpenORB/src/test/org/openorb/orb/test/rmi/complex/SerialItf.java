/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

import java.io.Serializable;

/**
 * This is the base Serializable interface.
 */
public interface SerialItf
    extends Serializable
{
    int size();
}

