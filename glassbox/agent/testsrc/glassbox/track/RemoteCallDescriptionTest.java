/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.RemoteCallDescription;
import junit.framework.TestCase;

public class RemoteCallDescriptionTest extends TestCase {

    public void testParseHttp() {
        String key = "http://foo.org/test";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("http://foo.org", rd.getResourceKey());
        assertEquals("test", rd.getCallKey());
    }
    
    public void testParseHttpRoot() {
        String key = "http://service.org";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("http://service.org", rd.getResourceKey());
        assertEquals(RemoteCallDescription.ROOT_KEY, rd.getCallKey());
    }
    
    public void testParseHttpRoot2() {
        String key = "http://service.org/";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("http://service.org", rd.getResourceKey());
        assertEquals(RemoteCallDescription.ROOT_KEY, rd.getCallKey());
    }
    
    public void testParseHttpPort() {
        String key = "http://test.org:8080/test";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("http://test.org:8080", rd.getResourceKey());
        assertEquals("test", rd.getCallKey());
    }
    
    public void testParseBadHttp() {
        String key = "http://test";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("http://test", rd.getResourceKey());
        assertEquals(RemoteCallDescription.ROOT_KEY, rd.getCallKey());
    }
    
    public void testParseRmiFull() {
        String key = "rmi://full.org:123/Conquerer";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("rmi://full.org:123", rd.getResourceKey());
        assertEquals("Conquerer", rd.getCallKey());
    }

    public void testParseRmiShort() {
        String key = "rmi:/Conquerer";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("rmi", rd.getResourceKey());
        assertEquals("Conquerer", rd.getCallKey());
    }
    
    public void testParseRmiNoColon() {
        String key = "rmi:TaterSpot";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("rmi", rd.getResourceKey());
        assertEquals("TaterSpot", rd.getCallKey());
    }

    public void testParseFtp() {
        String key = "ftp://test.org/sample";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("ftp://test.org", rd.getResourceKey());
        assertEquals("sample", rd.getCallKey());
    }

    public void testParseFtpRoot() {
        String key = "ftp://test.org";
        RemoteCallDescription rd = new RemoteCallDescription(key);
        assertEquals(key, rd.getSummary());
        assertEquals("ftp://test.org", rd.getResourceKey());
        assertEquals(RemoteCallDescription.ROOT_KEY, rd.getCallKey());
    }

//    public void testParseFile() {
//        String key = "file:///c:/dir/file";
//        RemoteCallDescription rd = new RemoteCallDescription(key);
//        assertEquals(key, rd.getSummary());
//        assertEquals("file:///c", rd.getResourceKey());
//        assertEquals("dir/file", rd.getCallKey());
//    }
//
//    public void testParseFileRoot() {
//        String key = "ftp://test.org";
//        RemoteCallDescription rd = new RemoteCallDescription(key);
//        assertEquals(key, rd.getSummary());
//        assertEquals("ftp://test.org", rd.getResourceKey());
//        assertEquals(RemoteCallDescription.ROOT_KEY, rd.getCallKey());
//    }
}
