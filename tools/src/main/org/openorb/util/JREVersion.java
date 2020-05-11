/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.math.BigDecimal;

/**
 * A utility class determinig the runtime version current JRE
 *
 * @author Richard G Clark
 * @version $Revision: 1.4 $ $Date: 2005/03/01 13:00:50 $
 */
public final class JREVersion
{
    private static final String VERSION_KEY = "java.specification.version";

    private static final BigDecimal VERSION_1_4 = new BigDecimal( "1.4" );
    private static final BigDecimal VERSION_1_5 = new BigDecimal( "1.5" );

    public static final BigDecimal VERSION = new BigDecimal( System.getProperty( VERSION_KEY ) );


    /**
     * Indicates that the current JRE is at least 1.4
     */
    public static final boolean V1_4 = VERSION_1_4.compareTo( VERSION ) <= 0;

    /**
     * Indicates that the current JRE is at least 1.5
     */
    public static final boolean V1_5 = VERSION_1_5.compareTo( VERSION ) <= 0;


    private JREVersion()
    {
    }
}

