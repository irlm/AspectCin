/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.orb;

/**
 * This exception could be raised by OpenORB when a required property is not found.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:40 $
 */
public class PropertyNotFoundException
    extends Exception
{
    public PropertyNotFoundException()
    {
    }

    public PropertyNotFoundException( String reason )
    {
        super( reason );
    }
}

