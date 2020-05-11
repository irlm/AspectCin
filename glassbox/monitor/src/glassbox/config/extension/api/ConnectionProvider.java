package glassbox.config.extension.api;

import java.util.List;

/**
 * Implementation of Glassbox connection provider class that automatically creates connections for external servers (the
 * created nodes are NOT persisted to the configuration database).
 * 
 * @author Ron Bodkin
 * 
 */
public interface ConnectionProvider {
    public static final String JMX_RMI_PROTOCOL = "jmx/rmi";
    public static final String RMI_PROTOCOL = "rmi";
    public static final String LOCAL_PROTOCOL = "local";

    public interface Connection {

        /**
         * @return a descriptive name for this connection
         */
        String getName();

        /**
         * if description is null then Glassbox will use the name for the description
         * 
         * @return a description for this connection
         */
        String getDescription();

        /**
         * @return the DNS host name or ip address of this connection
         */
        String getHostName();

        /**
         * @return the Glassbox string for the protocol used by this connection
         * @see glassbox.config.extension.api.ConnectionProvider for constant values
         */
        String getProtocol();

        /**
         * @return the TCP/IP port used for this connection
         */
        String getPort();
    }

    /**
     * @return a list of automatically created connections: type is List<Connection>
     */
    List getConnections();
}
