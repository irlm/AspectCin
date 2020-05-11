/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.doc.rtf;

/**
 * This class is used as a model for a RTF document.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:37 $
 */

public class model
{
    /**
     * Font name
     */
    public String fontName;

    /**
     * Font family
     */
    public String fontFamily;

    /**
     * Font number
     */
    public String fontNumber;

    /**
     * Font size
     */
    public String fontSize;

    /**
     * Font color
     */
    public String color;

    /**
     * Font color number
     */
    public String colorNumber;

    /**
     * Extra special effects ( underline, italic, bold, uppercase, left, right, center, justified )
     */
    public String attribute;

    /**
     * Border attributes ( box, bottom, top, left, right, simple, double, dotted, shadowed )
     */
    public String border;

    /**
     * Background color
     */
    public String backcolor;

    /**
     * Background color number
     */
    public String backgroundNumber;

}
