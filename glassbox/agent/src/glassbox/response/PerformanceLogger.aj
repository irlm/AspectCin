package glassbox.response;

import glassbox.config.GlassboxInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.springframework.beans.factory.DisposableBean;

public class PerformanceLogger implements ResponseListener, DisposableBean {
    public static final int NO_LOG = 0; 
    public static final int PROBLEM_LOG = 0x01; 
    public static final int ALL_LOG = 0x02;
    public static final int SUMMARY_LOG = 0x10;
    public static final int DETAIL_LOG = 0x20;
    public static final boolean disabled = Boolean.getBoolean("glassbox.performance.log.disabled");
    public static final String DELIM = "|";

    private int level;
    private long threshold;
    private boolean allLog = false;
    private Timer timer;
    
    public PerformanceLogger(ResponseFactory factory) {
        if (!disabled) {
            TimerTask refreshTask = new TimerTask() {
                public void run() {
                    refresh();
                }
            };
            timer = new Timer(true);
            timer.schedule(refreshTask, 0, 15000);
            
            factory.addListener(this);
        }
    }
    
    public void destroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    private static boolean cantRead=false;
    
    public void refresh() {
        String dir = GlassboxInitializer.getConfigDir();
        if (dir != null) {
            level = NO_LOG;
            String fileLoc = dir+File.separatorChar+"runtime.properties";
            try {
                File file = new File(fileLoc);
                Properties props = new Properties();
                props.load(new FileInputStream(file));
                
                level = Integer.valueOf(props.getProperty("glassbox.performance.log.level", "0").trim()).intValue();
                allLog = (level&ALL_LOG) != 0;
                
                threshold = Long.valueOf(props.getProperty("glassbox.performance.log.threshold", "100000").trim()).longValue();
                cantRead=false;
            } catch (Throwable t) {
                if (!cantRead) {
                    cantRead=true;
                    logWarn("Unable to reconfigure using properties file "+fileLoc+", using defaults", t);
                }
            }
        }
    }
    
    private static final String keys[] = {
        Response.FAILURE_DATA,
        Response.EXCEPTION_WARNING,
        Response.RESOURCE_KEY,
        Response.REQUEST
    };
    
    public void startedResponse(Response response) {
        // no op
    }
    
    public void finishedResponse(Response response) {
        if (allLog) {
            long duration = response.getDuration();
            if (duration >= threshold) {
                StringBuffer msg = new StringBuffer(Thread.currentThread()+DELIM+response.getApplication()+DELIM+response.getKey()+DELIM+duration);
                for (int i=0; i<keys.length; i++) {
                    Object o = response.get(keys[i]);
                    if (o!=null) {
                        msg.append(DELIM+keys[i]+":"+o.toString());                        
                    }
                }
                //Object params =                         Response.PARAMETERS, // not yet
                
                logInfo(msg.toString());
            }
        }
    }
    
//    declare precedence: StatsSummarizer.SummarizeTopLevelStats, ResponseTracer;
//
//    private ThreadLocal indentHolder = new ThreadLocal() {
//        public Object initialValue() {
//            return new int[] { 0 };
//        }
//    };
//
//    after(PerfStats stats, Response response, StatsSummarizer summarizer) returning: 
//      StatsSummarizer.startStats(stats, response, summarizer) && if (enabled) {
//        String extra = "";
//        if(stats.isOperationKey()) {
//            extra = ", may be opkey";
//        }
//        if (summarizer.getPrimaryOperationStats()!=null) {
//            extra += ", existing primary";
//        }
//        logInfo(indent() + "Started response " + response.getKey() + " on "+ Thread.currentThread() + extra);
//        logDebug("app = "+response.getApplication());
//    }
//
//    after(Response response, StatsSummarizer summarizer) returning: 
//      StatsSummarizer.endTopLevelStats(*, response, summarizer) && if (enabled) {
//        Object pk = (summarizer.getPrimaryOperationStats() == null ? null : summarizer.getPrimaryOperationStats()
//                .getKey());
//
//        logInfo(unindent() + "Finished response " + response.getKey() + ", primary operation = " + pk
//                + ", elapsed = " + TimeConversion.formatTime(response.getDuration())+" on "+Thread.currentThread());
//        logDebug("app = "+response.getApplication());
//    }
//
//    after(PerfStats stats, Response response, StatsSummarizer summarizer) returning: 
//      StatsSummarizer.endNestedStats(stats, response, summarizer) && if (enabled) {
//        int[] indent = (int[]) indentHolder.get();
//        String opStatus = "";
//        if (stats.isOperationThisRequest()) {
//            if (stats == summarizer.getPrimaryOperationStats()) {
//                opStatus = " (primary operation key)";
//            } else {
//                opStatus = " (secondary operation key)";
//            }
//        }
//        FailureDescription failure = (FailureDescription)response.get(Response.FAILURE_DATA);
//        if (failure != null) {
//            opStatus = opStatus + ", failed: "+failure.getSummary();
//        }
//                
//        logInfo(unindent() + "Finished response " + response.getKey() + opStatus + ", elapsed = "
//                + TimeConversion.formatTime(response.getDuration()));
//        logDebug("app = "+response.getApplication());
//    }
//
//    private static aspect DebugData {
//        private static final boolean enabled = ResponseTracer.enabled && aspectOf().isDebugEnabled();
//        
//        after() returning: adviceexecution() && within(ResponseTracer) && !within(DebugData) && if(enabled) {
//            logDebug("on " + Thread.currentThread().getName(), new Throwable());
//        }
//        
//    }
//
//    private String indent() {
//        int[] indent = (int[]) indentHolder.get();
//        return spaces(indent[0]++);
//    }
//
//    private String unindent() {
//        int[] indent = (int[]) indentHolder.get();
//        return spaces(--indent[0]);
//    }
//
//    private String spaces(int nspaces) {
//        StringBuffer spaces = new StringBuffer();
//        for (int i = 0; i < nspaces; i++) {
//            spaces.append(' ');
//        }
//        return spaces.toString();
//    }

}
