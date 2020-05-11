/**
 * 
 */
package glassbox.thread.context;

import java.lang.reflect.Field;

public class SavedContextLoader {
    private Object contextLoaderFieldVal = DONT_RESTORE_LOADER;
    private ClassLoader contextLoader;
    private static final Object DONT_RESTORE_LOADER = SavedContextLoader.class;
    private static Field threadContextLoaderField = initField();
    
    public SavedContextLoader() {
        Thread thread = Thread.currentThread();
        if (threadContextLoaderField != null) {
            try {
                contextLoaderFieldVal = threadContextLoaderField.get(thread);
            } catch (IllegalAccessException ae) {
                cantAccessFieldWarning();
                threadContextLoaderField = null;
            }
        }
        contextLoader = thread.getContextClassLoader();
    }
    
    public void restore() {
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(contextLoader);
        if (contextLoaderFieldVal != DONT_RESTORE_LOADER) {
            try {
                threadContextLoaderField.set(thread, contextLoaderFieldVal);
                //System.err.println("Reset loader to "+contextLoaderFieldVal);
            } catch (IllegalAccessException ae) {
                cantAccessFieldWarning();
                threadContextLoaderField = null;
            }
        }
    }

    private static void cantAccessFieldWarning() {
        System.err.println("Warning: can't restore context loader field. This is normally not a problem, although it "+
                "is a problem on Oracle application servers. If you are running an Oracle application server, "+
                "please contact Glassbox support.");
    }

    private static Field initField() {
        try {
            Field field = Thread.class.getDeclaredField("contextClassLoader");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            cantAccessFieldWarning();
            return null;
        }
    }
}