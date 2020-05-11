package glassbox.util.jmx;

import glassbox.util.jmx.JmxManagement.ManagedBean;

/**
 * "firewall" interface - allows monitor classes to interact with JMX
 * without having to have JMX classes in the class loader where the monitor classes live
 * 
 * @author Ron Bodkin
 *
 */
public interface JmxServerManager {
    void register(ManagedBean bean);
    void unregister(ManagedBean bean);
    
    /** get the default mbean for a managed bean - used by default ManagedBean ITD implementation  */
    Object getDefaultMBean(ManagedBean bean);
    
    /** @return object of type javax.management.MBeanServer */
    Object getMBeanServer();
}
