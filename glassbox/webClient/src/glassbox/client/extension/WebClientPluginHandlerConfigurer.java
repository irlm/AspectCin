package glassbox.client.extension;

import glassbox.client.helper.ConnectionHelper;
import glassbox.client.pojo.Preferences;
import glassbox.velocity.RefreshableSingletonFileResourceLoader;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Wrapper class that acts as a firewall to prevent Spring from introspecting WebClientPluginHandler and 
 * blowing up with a NoClassDefFoundError before installation.
 *
 */
public class WebClientPluginHandlerConfigurer implements DisposableBean, InitializingBean {
    private WebClientPluginHandler handler = WebClientPluginHandler.aspectOf();
    
    public void afterPropertiesSet() {
        try {
            handler.configure();
        } catch (NoClassDefFoundError noMonitorErr) {
            ;//ok - just not installed
        }
    }

    public void destroy() {
        try {
            handler.destroy();
        } catch (NoClassDefFoundError noMonitorErr) {
            ;//ok - just not installed
        }
    }

    public RefreshableSingletonFileResourceLoader getVelocityLoader() {
        return handler.getVelocityLoader();
    }

    public void setVelocityLoader(RefreshableSingletonFileResourceLoader velocityLoader) {
        handler.setVelocityLoader(velocityLoader);
    }

    public ConnectionHelper getConnectionHelper() {
        return handler.getConnectionHelper();
    }

    public void setConnectionHelper(ConnectionHelper connectionHelper) {
        handler.setConnectionHelper(connectionHelper);
    }

    public Preferences getPreferences() {
        return handler.getPreferences();
    }

    public void setPreferences(Preferences preferences) {
        handler.setPreferences(preferences);
    }

}
