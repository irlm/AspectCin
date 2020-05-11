package glassbox.util.jmx;

import glassbox.jmx.support.InterfaceBasedMBeanInfoAssembler;

import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.jmx.export.assembler.MBeanInfoAssembler;

public class SpringAssembler implements Assembler {
    public ModelMBeanInfo assemble(Class interfaze, Object managed, String operationName) {
        try {
            return makeAssembler(interfaze).getMBeanInfo(managed, operationName);        
        } catch (NoClassDefFoundError e) {
            // this is an expected condition if you try to register a monitor that refers to types that
            // don't exist ... thus far the only case that applies is the servlet monitor where we won't
            // register it outside of a container
            logInfo("Unable to register bean due to missing type "+e.getMessage()+" for "+operationName);
            logDebug("Stack trace", e);
            throw new RegistrationFailureException("can't register bean ", e);
        } catch (JMException e) {
            logWarn("Unexpected error when registering bean for "+operationName);
            throw new RegistrationFailureException("can't register bean ", e);
        }
    }
 
    private MBeanInfoAssembler makeAssembler(Class interfaze) {      
        Class[] managedInterfaces = new Class[] { interfaze };
        InterfaceBasedMBeanInfoAssembler anAssembler = new InterfaceBasedMBeanInfoAssembler();
        anAssembler.setManagedInterfaces(managedInterfaces);
        return anAssembler;
    }    
 }
