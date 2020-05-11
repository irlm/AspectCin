/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.simulator.resource.http;

import java.io.IOException;

import glassbox.test.DelayingRunnable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

public class MockCommonsHttpClient extends HttpClient {

    protected DelayingRunnable delayer = new DelayingRunnable();
    
    public void setDelay(long delay) {
        delayer.setDelay(delay);
    }

    public void delay() {
        delayer.run();
    }
    
    public int executeMethod() throws HttpException, IOException {
        return executeMethod(new MockCommonsHttpMethod());
    }
    
    public int executeMethod(HttpMethod method)  throws HttpException, IOException {
        delay();
        return 0;
    }
}
