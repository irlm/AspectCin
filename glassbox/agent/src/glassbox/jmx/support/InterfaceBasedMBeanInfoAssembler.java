/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.jmx.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.Descriptor;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanUtils;

public class InterfaceBasedMBeanInfoAssembler extends
        org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler {

    protected ModelMBeanOperationInfo[] getOperationInfo(Object managedBean, String beanKey) {
        Method[] methods = getClassToExpose(managedBean).getMethods();
        List infos = new ArrayList();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            ModelMBeanOperationInfo info = null;

            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
            if (pd != null) {
                if ((method.equals(pd.getReadMethod()) && includeReadAttribute(method, beanKey)) ||
                        (method.equals(pd.getWriteMethod()) && includeWriteAttribute(method, beanKey))) {
                    // Attributes need to have their methods exposed as
                    // operations to the JMX server as well.
                    info = createModelMBeanOperationInfo(method, pd.getName(), beanKey);
                    Descriptor desc = info.getDescriptor();
                    if(desc == null) {
                        desc = createDefaultDescriptor(info);
                    }
                    if (method.equals(pd.getReadMethod())) {
                        desc.setField(FIELD_ROLE, ROLE_GETTER);
                    }
                    else {
                        desc.setField(FIELD_ROLE, ROLE_SETTER);
                    }
                    desc.setField(FIELD_VISIBILITY, ATTRIBUTE_OPERATION_VISIBILITY);
                    if (isExposeClassDescriptor()) {
                        desc.setField(FIELD_CLASS, getClassForDescriptor(managedBean).getName());
                    }
                    try {
                        info.setDescriptor(desc);
                    } catch (RuntimeOperationsException rte) {
                        // JMX 1.2 won't let you add an attribute method as an operation
                        // just carry on in that case
                        info = null;
                    }
                }
            }
            else if (includeOperation(method, beanKey)) {
                info = createModelMBeanOperationInfo(method, method.getName(), beanKey);
                Descriptor desc = info.getDescriptor();
                if (desc == null) {
                    desc = createDefaultDescriptor(info);
                }
                desc.setField(FIELD_ROLE, ROLE_OPERATION);
                if (isExposeClassDescriptor()) {
                    desc.setField(FIELD_CLASS, getClassForDescriptor(managedBean).getName());
                }
                populateOperationDescriptor(desc, method, beanKey);
                info.setDescriptor(desc);
            }

            if (info != null) {
                infos.add(info);
            }
        }

        return (ModelMBeanOperationInfo[]) infos.toArray(new ModelMBeanOperationInfo[infos.size()]);
    }
    
    private Descriptor createDefaultDescriptor(ModelMBeanOperationInfo info) {
        return (Descriptor) new DescriptorSupport(new String[] {"descriptorType=operation",
                ("name=" + info.getName()),
                "role=operation",
                ("displayname=" + info.getName())});
    }
}
