package glassbox.monitor.resource;

import glassbox.monitor.MonitorResponseTestCase;

import java.io.File;
import java.util.*;

import com.sleepycat.bind.serial.*;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.*;
import com.sleepycat.je.*;

// commented out to work around classpath problem in test harness
/**
 * @author Mark Hayes
 */
public class BerkeleyDbUnitTest extends MonitorResponseTestCase {// implements TransactionWorker {

    public void testNothing() {}
    
//    public void testHello() throws Exception {
//        doMain();
//        for (int i=0; i<listener.responses.size(); i++) {
//            System.err.println(listener.responses.get(i).toString());
//        }
//        assertTrue(listener.responses.size()>0);
//        assertTrue(listener.responses.size()%2==0);
//    }
//    
//    public int getMaxRequests() {
//        return 200;
//    }
//    
//    private static final String[] INT_NAMES = {
//        "Hello", "Database", "World",
//    };
//    private static boolean create = true;
//
//    private Environment env;
//    private ClassCatalog catalog;
//    private Database db;
//    private SortedMap map;
//
//    /** Creates the environment and runs a transaction */
//    protected void doMain() throws Exception {
//
//        String dir = System.getProperty("java.io.tmpdir");
//
//        // environment is transactional
//        EnvironmentConfig envConfig = new EnvironmentConfig();
//        envConfig.setTransactional(true);
//        if (create) {
//            envConfig.setAllowCreate(true);
//        }
//        env = new Environment(new File(dir), envConfig);
//
//        open();
//        // create the application and run a transaction
//        TransactionRunner runner = new TransactionRunner(env);
//        try {
//            // open and access the database within a transaction
//            runner.run(this);
//        } finally {
//            // close the database outside the transaction
//            close();
//        }
//    }
//
//    /** Performs work within a transaction. */
//    public void doWork()
//        throws Exception {
//
//        writeAndRead();
//    }
//
//    /** Opens the database and creates the Map. */
//    private void open()
//        throws Exception {
//
//        // use a generic database configuration
//        DatabaseConfig dbConfig = new DatabaseConfig();
//        dbConfig.setTransactional(true);
//        if (create) {
//            dbConfig.setAllowCreate(true);
//        }
//
//        // catalog is needed for serial bindings (java serialization)
//        Database catalogDb = env.openDatabase(null, "catalog", dbConfig);
//        catalog = new StoredClassCatalog(catalogDb);
//
//        // use Integer tuple binding for key entries
//        TupleBinding keyBinding =
//            TupleBinding.getPrimitiveBinding(Integer.class);
//
//        // use String serial binding for data entries
//        SerialBinding dataBinding = new SerialBinding(catalog, String.class);
//
//        this.db = env.openDatabase(null, "helloworld", dbConfig);
//
//        // create a map view of the database
//        this.map = new StoredSortedMap(db, keyBinding, dataBinding, true);
//    }
//
//    /** Closes the database. */
//    private void close()
//        throws Exception {
//
//        if (catalog != null) {
//            catalog.close();
//            catalog = null;
//        }
//        if (db != null) {
//            db.close();
//            db = null;
//        }
//        if (env != null) {
//            env.close();
//            env = null;
//        }
//    }
//
//    /** Writes and reads the database via the Map. */
//    private void writeAndRead() {
//
//        // check for existing data
//        Integer key = new Integer(0);
//        String val = (String) map.get(key);
//        if (val == null) {
//            System.out.println("Writing data");
//            // write in reverse order to show that keys are sorted
//            for (int i = INT_NAMES.length - 1; i >= 0; i -= 1) {
//                map.put(new Integer(i), INT_NAMES[i]);
//            }
//        }
//        // get iterator over map entries
//        Iterator iter = map.entrySet().iterator();
//        try {
//            System.out.println("Reading data");
//            while (iter.hasNext()) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                System.out.println(entry.getKey().toString() + ' ' +
//                                   entry.getValue());
//            }
//        } finally {
//            // all database iterators must be closed!!
//            StoredIterator.close(iter);
//        }
//    }
}
