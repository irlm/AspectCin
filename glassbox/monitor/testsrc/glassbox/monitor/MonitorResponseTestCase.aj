/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor;

import glassbox.response.*;
import glassbox.test.DefinedLoaderCoreMock;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.api.FailureDescription;
import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.jmock.Mock;
import org.jmock.core.*;
import org.jmock.core.stub.StubSequence;

public abstract class MonitorResponseTestCase extends VirtualMockObjectTestCase {

    protected Mock mockClock;
    protected ResponseFactory responseFactory;
    protected long time[];
    protected CopyListListener listener;
    protected OperationFactory operationFactory;

    protected void setUp() throws Exception {
        List listeners = new ArrayList();
        listener = new CopyListListener();
        listeners.add(listener);
        responseFactory = new DefaultResponseFactory();
        AbstractMonitor.setResponseFactory(responseFactory);
        operationFactory = new OperationFactory();
        operationFactory.setResponseFactory(responseFactory);
        AbstractMonitor.setOperationFactory(operationFactory);
        AbstractMonitor.setAllDisabled(false);
        responseFactory.setListeners(listeners);
        mockClock = mock(Clock.class);
        responseFactory.setClock((Clock)mockClock.proxy());
        
        time = new long[getMaxRequests()];
        Stub stubs[] = new Stub[time.length];
        for (int i=0; i<time.length; i++) {
            time[i] = TimeConversion.convertMillisToNanos(i+1);
            stubs[i] = returnValue(time[i]);
        }
        StubSequence stubSequence = new StubSequence(stubs);
        mockClock.stubs().method("getTime").will(stubSequence);        
        mockClock.stubs().method("getTimeQuickly").will(stubSequence);        
    }

    public int getMaxRequests() {
        return 32;
    }
    
    public static abstract class ExecuteStub implements Stub {
    
        public abstract Object execute() throws Throwable;
        
        /* (non-Javadoc)
         * @see org.jmock.core.Stub#invoke(org.jmock.core.Invocation)
         */
        public Object invoke(Invocation invocation) throws Throwable {
            return execute();
        }
    
        /* (non-Javadoc)
         * @see org.jmock.core.SelfDescribing#describeTo(java.lang.StringBuffer)
         */
        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append("execute stub: "+toString());
            return buffer;
        }
        
    }

    protected void assertFailingResponse(Throwable throwable, Response response) {
        FailureDescription fd = (FailureDescription)response.get(Response.FAILURE_DATA);
        assertNotNull(fd);
        assertEquals(throwable.getClass().getName(), fd.getThrowableClassName());       
        // the stack traces aren't equal - when it's thrown new frames are filled in...
    }

    protected Response getResponse(int n) {
        return (Response)listener.responses.get(n);
    }

    protected static class CopyListListener implements ResponseListener {
        public List responses = new ArrayList();
    
        public void finishedResponse(Response response) {
            responses.add(copy(response));
        }
    
        public void startedResponse(Response response) {
            responses.add(copy(response));
        }
        
        private Response copy(Response response) {        
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(response);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            Response copy = (Response)ois.readObject();
            ois.close();
            return copy;
        }
    }

    /**
     * We want to mock classes that are defined in the bootstrap loader (JDBC classes), but we want to define them
     * in the application class loader so that we can weave them. It would be better if AspectJ did allow us to weave
     * into bootstrap classes...
     */
    protected DynamicMock newCoreMock(Class mockedType, String roleName) {
        return new DefinedLoaderCoreMock(mockedType, roleName, getClass().getClassLoader());
    }

    protected void assertProperties(Object statKey, Object layer, Response response) {
        assertProperties(statKey, null, layer, response);
    }

    protected void assertProperties(Object statKey, Object resKey, Object layer, Response response) {
        assertEquals(statKey, response.getKey());
        assertEquals(resKey, response.get(Response.RESOURCE_KEY));
        assertEquals(layer, response.getLayer());
    }

    protected void assertFailure(Class exceptionClass, int responseNum) {
        Response end = (Response) (listener.responses.get(responseNum));
        FailureDescription fd = (FailureDescription) end.get(Response.FAILURE_DATA);
        assertNotNull(fd);
        assertEquals(exceptionClass.getName(), fd.getThrowableClassName());
    }        
}