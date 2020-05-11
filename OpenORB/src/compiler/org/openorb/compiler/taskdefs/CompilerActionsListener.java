/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.taskdefs;

import java.io.File;

/**
 * @author Erik Putrycz erik.putrycz_at_ieee.org
 * Interface used to separate the Ant Task from the Compiler
 * (no Ant inheriance dependencies)
 */
public interface CompilerActionsListener
{
    void addTargetJavaFile( File file );
}
