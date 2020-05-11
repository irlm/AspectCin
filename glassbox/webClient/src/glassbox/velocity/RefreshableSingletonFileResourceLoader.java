package glassbox.velocity;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

public class RefreshableSingletonFileResourceLoader extends FileResourceLoader {
    private static RefreshableSingletonFileResourceLoader _instance;
    private static final Field pathsField = getPathsField();
    
    public RefreshableSingletonFileResourceLoader() {
        synchronized (RefreshableSingletonFileResourceLoader.class) {
            // multiple instances of the same class is useful when testing
            _instance = this;        
        }
    }
    
    public static synchronized RefreshableSingletonFileResourceLoader instance() {
        if (_instance == null) {
            return new RefreshableSingletonFileResourceLoader();
        }
        return _instance;
    }
    
    protected List getPaths() {
        try {
            return (List)pathsField.get(this);
        } catch (Exception e) {
            getLog().error("cannot access paths field of file loader", e);            
            return null;
        }
    }
    
    public void addPath(String path) {
        getPaths().add(path);
    }
    
    public void removePath(String path) {
        getPaths().remove(path);
    }
    
    private static Field getPathsField() {
        try {
            Field f = FileResourceLoader.class.getDeclaredField("paths");
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            getLog().error("cannot access paths field of file loader", e);            
            return null;
        }

    }
    
    private static final Log getLog() {
        return LogFactory.getLog(RefreshableSingletonFileResourceLoader.class);
    }
    
}
