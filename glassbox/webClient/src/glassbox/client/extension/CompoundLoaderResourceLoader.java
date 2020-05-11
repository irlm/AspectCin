package glassbox.client.extension;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

public class CompoundLoaderResourceLoader extends ResourceLoader {

    public long getLastModified(Resource arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public InputStream getResourceStream(String arg0) throws ResourceNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    public void init(ExtendedProperties arg0) {
        // TODO Auto-generated method stub
        
    }

    public boolean isSourceModified(Resource arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
