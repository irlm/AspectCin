/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package edu.wustl.doc.ant.taskdefs;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.Hashtable;
import java.util.Vector;
import java.io.File;
import java.io.IOException;

/**
 * Executes a given command with batches of a set of files as arguments.
 *
 * This task is very similar to the Apply task.  The difference is
 * that all files may not be passed at once.  This is to overcome
 * command line length and program argument count issues.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @author <a href="mailto:mariusz@rakiura.org">Mariusz Nowostawski</a>
 */
public class Batch extends ExecuteOn {

    private int parallelAmount = 0;
    private boolean skipEmpty = false;
    private boolean parallel = false;

    /**
     * Shall the command work on all specified files in parallel?
     */
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    /**
     * If parallel is true, then up to how many files can be processed
     * at a time? ( <= 0 indicates no upper bound)
     */
    public void setParallelAmount(int amount) {
        this.parallelAmount = amount;
    }

    /**
     * Should empty filesets be ignored?
     */
    public void setSkipEmptyFilesets(boolean skip) {
        skipEmpty = skip;
    }

    protected void runInParallel(Execute exe,
                                 Vector fileNames,
                                 Vector baseDirs)
        throws BuildException, IOException {
        String[] s = new String[fileNames.size()];
        fileNames.copyInto(s);
        File[] b = new File[baseDirs.size()];
        baseDirs.copyInto(b);

        if (this.parallelAmount <= 0) {
            String[] command = getCommandline(s, b);
            log("Executing " + Commandline.toString(command),
                Project.MSG_VERBOSE);
            exe.setCommandline(command);
            runExecute(exe);
        } else {
            int amountLeft = fileNames.size();
            int currentOffset = 0;
            while (amountLeft > 0) {
                int currentAmount = Math.min(amountLeft, this.parallelAmount);
                String[] cs = new String[currentAmount];
                System.arraycopy(s, currentOffset, cs, 0, currentAmount);
                File[] cb = new File[currentAmount];
                System.arraycopy(b, currentOffset, cb, 0, currentAmount);
                String[] command = getCommandline(cs, cb);
                log("Executing " + Commandline.toString(command),
                    Project.MSG_VERBOSE);
                exe.setCommandline(command);
                runExecute(exe);

                amountLeft -= currentAmount;
                currentOffset += currentAmount;
            }
        }
    }

    protected void runExec(Execute exe) throws BuildException {
        try {

            Vector fileNames = new Vector();
            Vector baseDirs = new Vector();
            for (int i=0; i<filesets.size(); i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                File base = fs.getDir(project);
                DirectoryScanner ds = fs.getDirectoryScanner(project);

                if (!"dir".equals(type)) {
                    String[] s = getFiles(base, ds);
                    for (int j=0; j<s.length; j++) {
                        fileNames.addElement(s[j]);
                        baseDirs.addElement(base);
                    }
                }

                if (!"file".equals(type)) {
                    String[] s = getDirs(base, ds);;
                    for (int j=0; j<s.length; j++) {
                        fileNames.addElement(s[j]);
                        baseDirs.addElement(base);
                    }
                }

                if (fileNames.size() == 0 && skipEmpty) {
                    log("Skipping fileset for directory "
                        + base + ". It is empty.", Project.MSG_INFO);
                    continue;
                }

                if (!parallel) {
                    String[] s = new String[fileNames.size()];
                    fileNames.copyInto(s);
                    for (int j=0; j<s.length; j++) {
                        String[] command = getCommandline(s[j], base);
                        log("Executing " + Commandline.toString(command),
                            Project.MSG_VERBOSE);
                        exe.setCommandline(command);
                        runExecute(exe);
                    }
                    fileNames.removeAllElements();
                    baseDirs.removeAllElements();
                }
            }

            if (parallel) {
                if (fileNames.size() > 0 || !skipEmpty) {
                    runInParallel(exe, fileNames, baseDirs);
                } else {
                    log("Skipping since no nonempty filesets",
                        Project.MSG_INFO);
                }
            }
        } catch (IOException e) {
            throw new BuildException("Execute failed: " + e, e, location);
        } finally {
            // close the output file if required
            logFlush();
        }
    }
}
