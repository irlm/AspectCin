package glassbox.client.pojo;

import glassbox.version.InstanceID;

public class UpdateResponse implements Preferences {
    private boolean checkUpdatesAutomatically;
    private String version;
    private String glassboxTitle;
    
    public UpdateResponse() {
        InstanceID instanceId = InstanceID.getInstanceID();
        version = instanceId.getHTTPString();
        checkUpdatesAutomatically = instanceId.isShouldCheckUpdatesAutomatically();
        // glassboxTitle defaults to null!
    }
    
    public boolean getCheckUpdatesAutomatically() {
        return checkUpdatesAutomatically;
    }
    
    public void setCheckUpdatesAutomatically(boolean checkUpdatesAutomatically) {
        this.checkUpdatesAutomatically = checkUpdatesAutomatically;
        // should persist the change, not yet supported
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        // method required by DWR JavaBeans impl - they should allow read-only attributes!
        throw new UnsupportedOperationException("version is read only");
    }

    /**
     * 
     * @return null if use the default else an overriding value
     */
    public String getGlassboxTitle() {
        return glassboxTitle;
    }

    public void setGlassboxTitle(String glassboxTitle) {
        this.glassboxTitle = glassboxTitle;
    }

}
