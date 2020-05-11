package glassbox.util.jmx;

import javax.management.modelmbean.ModelMBeanInfo;

public interface Assembler {
    public ModelMBeanInfo assemble(Class interfaze, Object managed, String operationName);        
}
