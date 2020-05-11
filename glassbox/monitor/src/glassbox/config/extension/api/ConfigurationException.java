package glassbox.config.extension.api;


public class ConfigurationException extends Exception {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message);
        doInitCause(cause);
    }
    
    protected void doInitCause(Throwable cause) {
        try {
            initCause(cause);
        } catch (Throwable jdk13Error) {
            ;//ignore
        }
    }

}
