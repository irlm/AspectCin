/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

/**
 * This class implemets extra operations for streams for inserting/extracting
 * fixed types. These operations should be present in the next java streams
 * mapping.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:49 $
 */
public interface ExtendedOutputStream
{
    /**
     * Get the orb associated with the stream.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Fixes problems in old write_fixed.
     * see  http://www.omg.org/issues/issue3431.txt
     */
    void write_fixed( java.math.BigDecimal val, org.omg.CORBA.TypeCode tc );

    /**
     * Fixes problems in old write_fixed.
     * see  http://www.omg.org/issues/issue3431.txt
     */
    void write_fixed( java.math.BigDecimal val, short digits, short scale );
}

