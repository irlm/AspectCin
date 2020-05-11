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

import org.apache.tools.ant.BuildException;
import java.io.File;
import java.util.Vector;
import java.util.StringTokenizer;


/**
 * Commandline objects help handling command lines specifying processes to
 * execute.
 *
 * The class can be used to define a command line as nested elements or as a
 * helper to define a command line by an application.
 * <p>
 * <code>
 * &lt;someelement&gt;<br>
 * &nbsp;&nbsp;&lt;acommandline executable="/executable/to/run"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 1" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument line="argument_1 argument_2 argument_3" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 4" /&gt;<br>
 * &nbsp;&nbsp;&lt;/acommandline&gt;<br>
 * &lt;/someelement&gt;<br>
 * </code>
 * The element <code>someelement</code> must provide a method
 * <code>createAcommandline</code> which returns an instance of this class.
 *
 * @author thomas.haas@softwired-inc.com
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a> 
 */
public class DefineList implements Cloneable {

    private Vector arguments = new Vector();

    public DefineList() {
        super();
    }

    /**
     * Used for nested xml command line definitions.
     */
    public class Argument {

        private String part;

        /**
         * Sets a single commandline argument.
         *
         * @param value a single commandline argument.
         */
        public void setValue(String value) {
            part = value;
        }

        /**
         * Returns the parts this Argument consists of.
         */
        public String getPart() {
            return part;
        }
    }


    /**
     * Creates an argument object.
     * Each commandline object has at most one instance of the argument class.
     * @return the argument object.
     */
    public Argument createArgument() {
        Argument argument = new Argument();
        arguments.addElement(argument);
        return argument;
    }

    public void addArguments(String[] line) {
        for (int i=0; i < line.length; i++) {
            createArgument().setValue(line[i]);
        }
    }



    /**
     * Returns all arguments defined by <code>addLine</code>,
     * <code>addValue</code> or the argument object.
     */
    public String[] getArguments() {
        Vector result = new Vector(arguments.size()*2);
        for (int i=0; i<arguments.size(); i++) {
            Argument arg = (Argument) arguments.elementAt(i);
            result.addElement(arg.getPart());
        }
        
        String [] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }

    public int size() {
        return getArguments().length;
    }

    public Object clone() {
        DefineList c = new DefineList();
        c.addArguments(getArguments());
        return c;
    }

    /**
     * Clear out the whole command line.  */
    public void clear() {
        arguments.removeAllElements();
    }

    /**
     * Clear out the arguments but leave the executable in place for another operation.
     */
    public void clearArgs() {
        arguments.removeAllElements();
    }     
}
