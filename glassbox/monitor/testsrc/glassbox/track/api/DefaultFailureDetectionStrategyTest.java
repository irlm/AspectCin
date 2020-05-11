package glassbox.track.api;

import glassbox.track.api.DefaultFailureDetectionStrategy.*;

import javax.servlet.ServletException;

import junit.framework.TestCase;

public class DefaultFailureDetectionStrategyTest extends TestCase {
    private DefaultFailureDetectionStrategy fds;
    
    protected void setUp() {
        fds = new DefaultFailureDetectionStrategy();
    }
    
    public void testNull() {
        Throwable t = new RuntimeException();
        FailureDescription fd = testSimple(t);
        assertTrue(fd.getSummary().indexOf("(no description)")>=0);
    }
    
    public void testSimple() {
        Throwable t = new RuntimeException("boom");
        FailureDescription fd = testSimple(t);
        assertTrue(fd.getSummary().indexOf(t.getMessage())>=0);
    }
    
    public void testLongMessage() {
        StringBuffer longStr = new StringBuffer();
        int len = fds.getMaxDescriptionLength()+1;
        for (int i=0; i<len; i++) {
            longStr.append((char)((i%26)+'a'));
        }
        Throwable t = new RuntimeException(longStr.toString());
        FailureDescription fd = testSimple(t);
        assertTrue("summary length is too long: "+fd.getSummary().length()+" should be "+fds.getMaxDescriptionLength(), 
                fd.getSummary().length() <= fds.getMaxDescriptionLength());        
    }

    protected FailureDescription testSimple(Throwable t) {
        FailureDescription fd = fds.getFailureDescription(t);
        assertEquals(FailureDetectionStrategy.FAILURE, fd.getSeverity());
        ThreadState expected = ThreadState.createFromThrowable(t);
        assertEquals(expected, fd.getThreadState());
        return fd;
    }        
    
    public void testNested() {
        Throwable cause = new Exception("rootCause");
        Throwable rethrow = new RuntimeException("wrapper", cause); 
        FailureDescription fd = fds.getFailureDescription(rethrow);
        assertEquals(FailureDetectionStrategy.FAILURE, fd.getSeverity());
        assertTrue("missing resulting message in "+fd.getSummary(), fd.getSummary().indexOf(rethrow.getMessage())>=0);
        assertTrue("missing caused message in "+fd.getSummary(), fd.getSummary().indexOf(cause.getMessage())>=0);
        ThreadState expected = ThreadState.createFromThrowable(cause);
        assertEquals(expected, fd.getThreadState());
        assertTrue(fd.getSummary().indexOf(cause.getMessage())>=0);
        assertTrue(fd.getSummary().indexOf(rethrow.getMessage())>=0);
    }
    
    public void testDeepNested() {
        Throwable cause = new Exception("rootCause");
        Throwable rethrow = new RuntimeException("wrapper", cause);
        Throwable rerethrow = new ServletException("se", rethrow);
        FailureDescription fd = fds.getFailureDescription(rerethrow);
        assertEquals(FailureDetectionStrategy.FAILURE, fd.getSeverity());
        assertTrue("missing resulting message in "+fd.getSummary(), fd.getSummary().indexOf(rerethrow.getMessage())>=0);
        assertTrue("missing caused message in "+fd.getSummary(), fd.getSummary().indexOf(cause.getMessage())>=0);
        ThreadState expected = ThreadState.createFromThrowable(cause);
        assertEquals(expected, fd.getThreadState());
        assertTrue(fd.getSummary().indexOf(cause.getMessage())>=0);
        assertTrue(fd.getSummary().indexOf(rerethrow.getMessage())>=0);
    }
    
    public void testDupCause() {
        Throwable cause = new Exception("messageOnce");
        Throwable rethrow = new RuntimeException(cause); 
        FailureDescription fd = fds.getFailureDescription(rethrow);
        assertEquals(FailureDetectionStrategy.FAILURE, fd.getSeverity());
        int idx = fd.getSummary().indexOf(cause.getMessage());
        assertTrue("missing resulting message in "+fd.getSummary(), idx>-1);
        assertFalse("shouldn't have dup message in "+fd.getSummary(), fd.getSummary().substring(idx+1).indexOf(cause.getMessage())>=0);
    }
    
    public void testReflectionStrategy() {
        testStrategy(new ReflectionCauseStrategy());
    }

//    public void testNormalStrategy() {
//        testStrategy(new Java14CauseStrategy());
//    }
    
    protected void testStrategy(CauseStrategy strategy) {
        Throwable cause1 = new RuntimeException("root");
        Throwable cause2 = new ExceptionInInitializerError(cause1);
        Throwable cause3 = new ExceptionInInitializerError(cause2); 

        assertEquals(cause2, strategy.getCause(cause3));
        assertEquals(cause1, strategy.getRootCause(cause3));
    }

}
