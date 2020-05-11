package glassbox.monitor.resource;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitoredType;
import glassbox.response.Response;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint.StaticPart;

/** Monitors both the Java api to native Berkeley DB and the Berkeley DB Java edition */
public aspect BerkeleyDbMonitor extends AbstractMonitor {

    public static interface IDatabase extends MonitoredType /*don't manage logging for this*/ {
        public String getDatabaseName();
    };

    public static interface IDb extends MonitoredType /*don't manage logging for this*/ {
        public String get_home();
    };
    
    public static interface IDatabaseException extends MonitoredType /*don't manage logging for this*/ {
        int getErrno();
    };
    
    declare parents: (com.sleepycat.db.Database || com.sleepycat.je.Database) implements IDatabase;
    declare parents: com.sleepycat.db.internal.Db implements IDb;
    declare parents: com.sleepycat.db.DatabaseException implements IDatabaseException;


    
    protected pointcut dbMethod() :
        within(IDatabase+) && execution(* com.sleepycat.*.Database.*(..)) && 
        !execution(* IDatabase.*(..)) && !cflow(adviceexecution() && within(BerkeleyDbMonitor));
    
    // monitoring lets us track time spent creating or opening databases too
    // very simple method-level timing... no parameters etc.
    protected pointcut openJeDb() :
        dbMethod() && execution(* com.sleepycat.je.Database.init*(..));
    
    protected pointcut openBdb(IDb db) : execution(com.sleepycat.db.Database.new(com.sleepycat.db.internal.Db)) && args(db);

    protected pointcut dbStatement() :
        dbMethod() && execution(public * *(..));

    protected pointcut monitorEnd() :
        openJeDb() || openBdb(IDb) || dbStatement();
    
    //TODO handle cursors
    
    before(IDatabase database) : openJeDb() && this(database) {
        beginResponse(database, Response.RESOURCE_DATABASE_CONNECTION, thisJoinPointStaticPart);
    }

    before(IDb db) : openBdb(db) {
        Response response = createResponse("connection", Response.RESOURCE_DATABASE_CONNECTION);
        response.set(Response.RESOURCE_KEY, nameOf(db.get_home()));
        response.start();
    }
    
    before(IDatabase database) : dbStatement() && this(database) {
        beginResponse(database, Response.RESOURCE_DATABASE_STATEMENT, thisJoinPointStaticPart);
    }

    protected void beginResponse(IDatabase database, String layer, StaticPart staticPart) {
        Response response = createResponse(staticPart.getSignature().getName(), layer);
        response.set(Response.RESOURCE_KEY, nameOf(database.getDatabaseName()));
        response.start();
    }
    
    protected String nameOf(String name) {
        if (name==null || name.equals("")) {
            return "Berkeley DB";
        }
        return name;
    }
    
    public BerkeleyDbMonitor() {
        failureDetectionStrategy = new BerkeleyDbFailureDetectionStrategy();
    }
    
    public String getLayer() {
        return Response.RESOURCE_DATABASE;
    }      

}
