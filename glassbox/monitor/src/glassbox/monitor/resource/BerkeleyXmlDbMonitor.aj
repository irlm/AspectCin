package glassbox.monitor.resource;

import edu.emory.mathcs.util.WeakIdentityHashMap;
import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitoredType;
import glassbox.response.Response;

import java.util.Collections;
import java.util.Map;

import com.sleepycat.dbxml.XmlQueryExpression;
import com.sleepycat.dbxml.XmlResults;

/**
 * Limitations: doesn't handle dynamic (not prepared) xqueries made directly via the XmlManager
 */
public aspect BerkeleyXmlDbMonitor extends AbstractMonitor {

    private static transient Map resultSetQueries = Collections.synchronizedMap(new WeakIdentityHashMap());
    // if false, we won't hold data, e.g., if forbidden by security policy
    // not yet configurable... as of 24-feb-06
    private static final boolean trackingData = true;

    //hokey use of prefixes
    private static final String PREPARE_PREFIX = "Prepare: ";
    private static final String NAVIGATE_PREFIX = "Fetch: ";
        
    public BerkeleyXmlDbMonitor() {
        failureDetectionStrategy = new BerkeleyXmlDbFailureDetectionStrategy();
    }
    
    public static interface IXmlContainer extends MonitoredType /*don't manage logging for this*/ {
        public String getName();
    };
        
    public static interface IXmlQueryExpression extends MonitoredType {
        public String getQuery();
    };
        
    public static interface IXmlResults extends MonitoredType {
    };
    
    declare parents: com.sleepycat.dbxml.XmlContainer implements IXmlContainer;
    declare parents: com.sleepycat.dbxml.XmlQueryExpression implements IXmlQueryExpression;
    declare parents: com.sleepycat.dbxml.XmlResults implements IXmlResults;
    
    // very simple method-level timing... no parameters etc.
    protected pointcut dbStatement() :
        within(IXmlContainer+) && execution(public * com.sleepycat.dbxml.XmlContainer.*(..)) && 
        !execution(* IXmlContainer.*(..)) && !execution(* delete(..));
    
    protected pointcut topLevelDbStatement(IXmlContainer xmlContainer) :
        dbStatement() && !cflowbelow(dbStatement()) && this(xmlContainer);
        
    protected pointcut getContainerFromManager() :
        within(com.sleepycat.dbxml.XmlManager+) && 
        execution(com.sleepycat.dbxml.XmlContainer com.sleepycat.dbxml.XmlManager.*(..));
    
    protected pointcut topLevelGetContainerFromManager() :
        getContainerFromManager() && !cflowbelow(getContainerFromManager());
    
    protected pointcut nameAfterTxn(String name) :
        args(com.sleepycat.dbxml.XmlTransaction, name, ..);
    
    protected pointcut executePreparedXquery(IXmlQueryExpression xmlQueryExpression) : 
        within(IXmlQueryExpression+) && execution(* XmlQueryExpression.execute(..)) && this(xmlQueryExpression);
    
    protected pointcut resultSetNavigation(IXmlResults xmlResults) : 
        within(IXmlResults+) && this(xmlResults) && 
        (execution(* XmlResults.next(..)) || execution(* XmlResults.previous(..)) || execution(* XmlResults.reset(..)));
    
    protected pointcut trackedResultSetNavigation(IXmlResults xmlResults) :
        if (resultSetQueries.get(xmlResults) != null) && resultSetNavigation(xmlResults);
    
    // other possibly slow operations
    //execution(* com.sleepycat.dbxml.XmlManager.reindex*(..))
    public pointcut publicXmlManagerMethod() :
        within(com.sleepycat.dbxml.XmlManager+) && execution(public * com.sleepycat.dbxml.XmlManager.*(..));
    
    public pointcut xmlManagerWork() :
        publicXmlManagerMethod() && !prepareXquery() && !executeDynamicXquery();
    
    public pointcut prepareXquery() : publicXmlManagerMethod() && execution(* prepare(..));
    public pointcut executeDynamicXquery() : publicXmlManagerMethod() && execution(* query(..));
//    public pointcut executeXqueryContext(Object context) : executeXquery() && (executeCtx2(context) || executeCtx3(context));
//    private pointcut executeCtx2(Object context) : execution(* *(*, com.sleepycat.dbxml.XmlQueryContext, ..)) && args(*, context, ..);
//    private pointcut executeCtx3(Object context) : execution(* *(*, *, com.sleepycat.dbxml.XmlQueryContext, ..)) && args(*, *, context, ..);
    
//            XmlQueryExpression.setVariableValue
    
    protected pointcut monitorEnd() : 
        topLevelDbStatement(*) || topLevelGetContainerFromManager() || prepareXquery() || executePreparedXquery(*) || trackedResultSetNavigation(*);
    
    before(String name) : topLevelGetContainerFromManager() && args(name, ..) {
        getContainerFromManager(name);
    }
    
    before(String name) : topLevelGetContainerFromManager() && nameAfterTxn(name) {
        getContainerFromManager(name);
    }
    
    private void getContainerFromManager(String name) {
        Response response = createResponse(name, Response.RESOURCE_DATABASE_CONNECTION);
        response.set(Response.RESOURCE_KEY, name);
        response.start();
    }
        
    before(IXmlContainer xmlContainer, String name) : topLevelDbStatement(xmlContainer) && execution(* getDocument(..)) && args(name, ..) {
        getXmlDbContainer(xmlContainer, name);
    }
    
    before(IXmlContainer xmlContainer, String name) : topLevelDbStatement(xmlContainer) && execution(* getDocument(..)) && nameAfterTxn(name) {
        getXmlDbContainer(xmlContainer, name);
    }
    
    private void getXmlDbContainer(IXmlContainer xmlContainer, String name) {
        Response response = createResponse(name, Response.RESOURCE_DATABASE_STATEMENT);
        response.set(Response.RESOURCE_KEY, getName(xmlContainer));
        response.start();
    }
        
    before(IXmlContainer xmlContainer) : topLevelDbStatement(xmlContainer) && !execution(* getDocument(..)) {
        Response response = createResponse(thisJoinPointStaticPart.getSignature().getName(), Response.RESOURCE_DATABASE_STATEMENT);
        response.set(Response.RESOURCE_KEY, getName(xmlContainer));
        response.start();
    }

    before(String query) : prepareXquery() && args(query, ..) {
        prepareXquery(query);
    }
    
    before(String query) : prepareXquery() && nameAfterTxn(query) {
        prepareXquery(query);
    }
    
    private String getName(IXmlContainer xmlContainer) {
        String name = xmlContainer.getName();
        if (name==null || name.equals("")) {
            return "no container";
        }
        return name;
    }
    
    private void prepareXquery(String query) {
        Response response = createResponse(PREPARE_PREFIX+query, Response.RESOURCE_DATABASE_STATEMENT);
        response.set(Response.RESOURCE_KEY, "dynamic"); // we don't parse XQuery expressions to determine the container(s) or document(s)
        response.start();        
    }
    
    before(IXmlQueryExpression xmlQueryExpression) : executePreparedXquery(xmlQueryExpression) { 
        Response response = createResponse(xmlQueryExpression.getQuery(), Response.RESOURCE_DATABASE_STATEMENT);
        response.set(Response.RESOURCE_KEY, "dynamic"); // we don't parse XQuery expressions to determine the container(s) or document(s)
        response.start();        
    }
    
    before(IXmlResults xmlResults) : resultSetNavigation(xmlResults) {
        IXmlQueryExpression query = (IXmlQueryExpression)resultSetQueries.get(xmlResults);
        if (query != null) {
            //Serializable connectionKey = (Serializable)connections.get(connection);
            IXmlQueryExpression xmlQueryExpression = (IXmlQueryExpression)resultSetQueries.get(xmlResults);
                
            Response response = createResponse(NAVIGATE_PREFIX+xmlQueryExpression.getQuery(), Response.RESOURCE_DATABASE_STATEMENT);
            response.set(Response.RESOURCE_KEY, "dynamic"); // we don't parse XQuery expressions to determine the container(s) or document(s)
//            if(trackingData) {
//                List parameters = getParameterList(xmlResults);
//                if (parameters != null) {
//                    response.set(Response.PARAMETERS, parameters);
//                }
//            }
            response.start();
        }
    }

    public String getLayer() {
        return Response.RESOURCE_DATABASE;
    }      

}
