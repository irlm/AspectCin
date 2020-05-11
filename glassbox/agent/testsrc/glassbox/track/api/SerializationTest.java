/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the 
 * terms of the
 * Lesser Gnu Public License v2.1, which accompanies this distribution and is available at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.analysis.api.OperationSummary;
import glassbox.analysis.api.OperationSummaryImpl;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class SerializationTest extends TestCase {

    public void testSerializeSummary() {
        List findings = new LinkedList();
        findings.add(OperationSummary.EXCESS_WORK);
        OperationSummaryImpl summary = new OperationSummaryImpl(new OperationDescriptionImpl(SerializationTest.class
                .getName(), "oName", "cName", "someApp", false), 20, 200L, 0, findings, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(summary);
        os.close();
        byte[] wrote = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(wrote);
        ObjectInputStream is = new ObjectInputStream(bis);
        Object read = is.readObject();
        assertEquals(summary, read);
    }
}
