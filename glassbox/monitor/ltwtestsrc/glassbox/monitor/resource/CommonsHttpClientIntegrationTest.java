package glassbox.monitor.resource;

import glassbox.monitor.MonitorResponseTestCase;
import glassbox.response.Response;
import glassbox.track.api.FailureDescription;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class CommonsHttpClientIntegrationTest extends MonitorResponseTestCase {
    public void testHttpPort() {
        test("http", "://127.1.1.1:1234", "127.1.1.1:1234");
    }
    
    public void testHttpWithPath() {
        test("http", "://a.foo.uz/listener", "a.foo.uz");
    }
    
    public void testHttpsDefaultPort() {
        test("https", "://ca.cd:443", "ca.cd");
    }
    
    private void test(String protocol, String main, String expAdr) {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(protocol+main);
        IOException exception=null;
        try {
            client.executeMethod(method);
            fail("should have failed to connect");
        } catch (IOException e) {
            exception = e;
        } finally {
            method.releaseConnection();
        }
        assertEquals("Unexpected listener responses: "+listener.responses, 2, listener.responses.size());
        Response response = (Response)listener.responses.get(1);
        assertEquals(protocol.toUpperCase()+" GET://"+expAdr, response.getKey());
        assertEquals(exception.getClass().getName(), ((FailureDescription)response.get(Response.FAILURE_DATA)).getThrowableClassName());
        assertEquals(CommonsHttpMonitor.class.getName(), response.get("monitor.class"));
    }    

}
