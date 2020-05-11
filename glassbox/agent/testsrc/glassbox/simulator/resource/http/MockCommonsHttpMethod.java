/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.http;

import glassbox.test.DelayingRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

public class MockCommonsHttpMethod implements org.apache.commons.httpclient.HttpMethod {

    protected MockHttpConnection CONNECTION = new MockHttpConnection(new MockHostConfiguration());
    protected MockHttpState STATE = new MockHttpState();

    protected DelayingRunnable delayer = new DelayingRunnable();

    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }

    public void delay() {
        delayer.run();
    }

    public int execute() throws HttpException, IOException {
        execute(STATE, CONNECTION);
        return 0;
    }

    public int execute(HttpState arg0, HttpConnection arg1) throws HttpException, IOException {
        delay();
        return 0;
    }

    public String getName() {
        return "MOCK HTTP METHOD NAME";
    }

    public HostConfiguration getHostConfiguration() {
        return null;
    }

    public void setPath(String arg0) {
    }

    public String getPath() {
        return null;
    }

    public URI getURI() throws URIException {
        return new URI("http://test.com/");
    }

    public void setStrictMode(boolean arg0) {
    }

    public boolean isStrictMode() {
        return false;
    }

    public void setRequestHeader(String arg0, String arg1) {
    }

    public void setRequestHeader(Header arg0) {
    }

    public void addRequestHeader(String arg0, String arg1) {
    }

    public void addRequestHeader(Header arg0) {
    }

    public Header getRequestHeader(String arg0) {
        return null;
    }

    public void removeRequestHeader(String arg0) {
    }

    public boolean getFollowRedirects() {
        return false;
    }

    public void setFollowRedirects(boolean arg0) {
    }

    public void setQueryString(String arg0) {
    }

    public void setQueryString(NameValuePair[] arg0) {
    }

    public String getQueryString() {
        return null;
    }

    public Header[] getRequestHeaders() {
        return null;
    }

    public boolean validate() {

        return false;
    }

    public int getStatusCode() {

        return 0;
    }

    public String getStatusText() {

        return null;
    }

    public Header[] getResponseHeaders() {

        return null;
    }

    public Header getResponseHeader(String arg0) {

        return null;
    }

    public Header[] getResponseFooters() {

        return null;
    }

    public Header getResponseFooter(String arg0) {

        return null;
    }

    public byte[] getResponseBody() {

        return null;
    }

    public String getResponseBodyAsString() {

        return null;
    }

    public InputStream getResponseBodyAsStream() throws IOException {

        return null;
    }

    public boolean hasBeenUsed() {

        return false;
    }

    public void recycle() {

    }

    public void releaseConnection() {

    }

    public void addResponseFooter(Header arg0) {

    }

    public StatusLine getStatusLine() {

        return null;
    }

    public boolean getDoAuthentication() {

        return false;
    }

    public void setDoAuthentication(boolean arg0) {

    }

}
