/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.util.jmx;

import glassbox.util.jmx.JmxManagement.ManagedBean;
import glassbox.monitor.MonitoredType;
import glassbox.track.api.*;
import glassbox.track.api.StatisticsRegistryImpl.NullKey;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.*;

/** Applies JMX management to performance statistics beans. */
public aspect StatsJmxManagement {
    public static boolean useCompositeBean = !Boolean.getBoolean(StatsJmxManagement.class.getName()+".simple");
    private StatsJmxNameStrategy nameStrategy;
    
    /** Management interface for performance statistics. A subset of @link glassbox.inspector.track.PerfStats */
    public interface PerfStatsManagementInterface {
        long getAccumulatedTime();
        long getMaxTime();
        double getMeanTime();
        int getCount();
        int getFailureCount();
        //void reset();
    }
    
    public interface PerfStatsMBean extends ManagedBean, PerfStatsManagementInterface {}
    
    public interface TreeTimeStatsManagementInterface {
        //String getSlowestTrace();
        String getSampledTimes();
    }
    
    public interface TreeTimeStatsMBean extends ManagedBean, TreeTimeStatsManagementInterface {}
    
    public static class PerfStatsBeanImpl implements DynamicMBean {
        private PerfStatsImpl stats;
        private MBeanInfo info;

        private static String[] itemNames = { "maxTime", "count", "failureCount", "meanTime", "accumulatedTime" };
        private static String[] itemDescriptions = { "Maximum Time (ms)", "Total Count", "Failure Count", "Mean Time (ms)", "Total Time (ms)" };
        private static OpenType[] itemTypes = { SimpleType.LONG, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.DOUBLE, SimpleType.LONG  };        
        private static CompositeType statsType = makeStatsType();
        private static String[] childItemNames = { "type", "key", "maxTime", "count", "failureCount", "meanTime", "percentage", "link...", "row" };
        private static final int PERCENTAGE_POS = positionFor(childItemNames, "percentage");
        private static final int ROW_POS = positionFor(childItemNames, "row");
        private static String[] childItemDescriptions = { "Statistics Type", "Key", "Maximum Time", "Total Count", "Failure Count", "Mean Time", "% of Parent Time", "Drill down link", "order" };
        private static OpenType[] childItemTypes = { SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.DOUBLE, SimpleType.STRING, SimpleType.OBJECTNAME, SimpleType.INTEGER };        
        private static CompositeType childRowType = makeChildRowType();
        private static TabularType childType = makeChildType();
//        private static OpenMBeanAttributeInfoSupport getStatsAttr = new OpenMBeanAttributeInfoSupport("stats", "time statistics", statsType, true, false, false); 
        private static OpenMBeanAttributeInfoSupport getChildrenAttr = new OpenMBeanAttributeInfoSupport("children", "nested statistics", childType, true, false, false); 
        private static OpenMBeanAttributeInfoSupport getParentAttr = new OpenMBeanAttributeInfoSupport("parent", "contained within...", SimpleType.OBJECTNAME, true, false, false); 
        //roll-up is only for resource / nested stats
        //private static OpenMBeanAttributeInfoSupport getRollupAttr = new OpenMBeanAttributeInfoSupport("rollup", "contained within...", SimpleType.OBJECTNAME, true, false, false); 
//        private static OpenMBeanAttributeInfo[] attributes = { getStatsAttr, getChildrenAttr, getParentAttr };
        private static OpenMBeanAttributeInfo[] attributes = makeAttributes();
        private static OpenMBeanAttributeInfo[] makeAttributes() {
            OpenMBeanAttributeInfo[] result = new OpenMBeanAttributeInfo[2+itemNames.length];
            int i;
            for (i=0; i<itemNames.length; i++) {
                result[i] = new OpenMBeanAttributeInfoSupport(itemNames[i], itemDescriptions[i], itemTypes[i], true, false, false);
            }
            result[i++] = getChildrenAttr;
            result[i++] = getParentAttr;
            return result;
        }
        
        private static int positionFor(String[] array, String element) {
            for (int i=0; i<array.length; i++) {
                if (element.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        }

        private static CompositeType makeStatsType() {
            return new CompositeType(PerfStatsImpl.class.getName(), "perf stats", itemNames, itemDescriptions, itemTypes);
        }
        private static CompositeType makeChildRowType() {
            return new CompositeType(PerfStatsImpl.class.getName(), "childStats", childItemNames, childItemDescriptions, childItemTypes);
        }
        
        private static TabularType makeChildType() {
            return new TabularType("children", "children", childRowType, new String[] { "row" });
        }

        public PerfStatsBeanImpl(PerfStatsImpl stats, String operationName) {
            this.stats = stats;
            
            info = new OpenMBeanInfoSupport(stats.getClass().getName(), operationName, attributes, new OpenMBeanConstructorInfo[]{}, new OpenMBeanOperationInfo[]{}, null);            
        }
        
        public Object getAttribute(String attribute) throws AttributeNotFoundException, ReflectionException {
//            if ("stats".equals(attribute)) {
//                try {
//                    // lock it... 
//                    return new CompositeDataSupport( statsType, itemNames, 
//                            new Object[] { new Integer(stats.getMaxTime()), new Integer(stats.getCount()), new Double(stats.getMeanTime())} );
//                } catch (OpenDataException e) {
//                    throw new ReflectionException(e, "failure getting: "+attribute);
//                }  
//            } else 
            if ("parent".equals(attribute)) {
                return getParentAttribute();
            } else if ("children".equals(attribute)) {
                return getChildrenAttribute();  
            } else {
                return getSimpleAttribute(attribute);
            }
        }
        private Object getSimpleAttribute(String attribute) throws AttributeNotFoundException, ReflectionException {
            try {
                Method getter = stats.getClass().getMethod("get"+Character.toUpperCase(attribute.charAt(0))+attribute.substring(1), null);
                return getter.invoke(stats, null);
            } catch (Exception e) {
                throw new ReflectionException(e, "Unknown attribute: "+attribute);
            }
        }
        private Object getChildrenAttribute() throws ReflectionException {
            try {
                // lock it & children?
                Collection children = stats.getChildren();
                if (children == null || children.size()==0) {
                    return null;
                }
                TabularDataSupport table = new TabularDataSupport( childType );
                
                Object[][] values = new Object[children.size()][];
                int pos = 0;
                long accum = 0L;
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    PerfStatsImpl child = (PerfStatsImpl) iter.next();
                    try {
                        Double val = new Double(child.getAccumulatedTime());
                        values[pos++] = new Object[] { child.getLayer(), child.getKey().toString(), new Long(child.getMaxTime()), new Integer(child.getCount()), 
                                new Integer(child.getFailureCount()), new Double(child.getMeanTime()), val, getObjectName(child), null };
                        accum += val.doubleValue();                        
                    } catch (MalformedObjectNameException e) {
                        throw new ReflectionException(e, "bad child name");
                    }
                }
                // need separate totals by link type, not all aggregated?
                 
                Arrays.sort(values, new Comparator() {
                    public int compare(Object left, Object right) {
                        double lval = valof(left);
                        double rval = valof(right);
                        
                        double delta = lval - rval;
                        return delta>0 ? -1 : (delta<0 ? 1 : 0);
                    }                      

                    private double valof(Object arg) {
                        Object[] arr = (Object[])arg;
                        return ((Double)arr[PERCENTAGE_POS]).doubleValue();
                    }

                });
                
                for (pos=0; pos<children.size(); pos++) {
                    double pct = 0.;
                    if (accum>0L) {
                        pct = 100. * ((Double)values[pos][PERCENTAGE_POS]).doubleValue() / (double)accum ;
                    }
//                        System.err.println("pos "+pos+": "+pct);
                    String strpct = Double.toString(pct);
                    if (strpct.length()>4) {
                        strpct = strpct.substring(0, 4);                            
                    }
                    values[pos][PERCENTAGE_POS] = strpct+"%"; //TODO: format with 1 decimal?
                    values[pos][ROW_POS] = new Integer(pos);
                    CompositeDataSupport childComposite = new CompositeDataSupport( childRowType, childItemNames, values[pos]); 
                    table.put(childComposite);
                }
                return table;
            } catch (OpenDataException e) {
                throw new ReflectionException(e, "failure getting children");
            }
        }
        private Object getParentAttribute() throws ReflectionException {
            ManagedBean bean = (ManagedBean)stats.getParent();
            if (bean == null) {
                return null;
            }
            try {
                return getObjectName(bean);
            } catch (MalformedObjectNameException e) {
                throw new ReflectionException(e, "bad parent name");
            }
            
        }
        
        private Object getObjectName(ManagedBean bean) throws MalformedObjectNameException {                        
            return ((DefaultJmxServerManager)(JmxManagement.aspectOf().getJmxServerManager())).getObjectName(bean);
        }
        
        public AttributeList getAttributes(String[] attributes) {
            if (attributes == null) {
                return null;
            }
            
            AttributeList lst = new AttributeList(attributes.length);
            for (int i = 0; i < attributes.length; i++) {
                String name = attributes[i];
                try {
                    lst.add(new Attribute(name, getAttribute(name)));
                } catch (Exception e) {
                    // keep trying
                }
            }
            return lst;
        }
        
        public AttributeList setAttributes(AttributeList attributes) {
            return null;
        }
        
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException  {
            throw new AttributeNotFoundException("no settable attributes");
        }
        
        public MBeanInfo getMBeanInfo() {
            return info;
        }

        public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
//            if (actionName=="reset") {
//                if ((signature==null || signature.length == 0) && (params==null || params.length == 0)) {
//                    stats.reset();
//                    return null;
//                } else {
//                    throw new MBeanException(new Exception(), "invalid params/signature, expected 0 received : "+params.length+"/ "+signature.length);                    
//                }
//            } else {
                throw new MBeanException(new Exception(), "unknown action: "+actionName);
//            }
        }

    }
    private pointcut typeCreation() : execution(* PerfStatsBeanImpl.make*Type());
    
    declare soft: OpenDataException: typeCreation();
    Object around() : typeCreation() {
        try {
            return proceed();
        } catch (OpenDataException e) {
            logError("Can't construct type in "+thisJoinPointStaticPart.getSignature().getName(), e);
            return null;
        }
    }
    
    public Object PerfStatsImpl.getMBean(JmxServerManager serverManager) {        
        try {
            if (!useCompositeBean) {
                return serverManager.getDefaultMBean(this);
            }
            String operationName = getOperationName();
            if (operationName == null) {
                logWarn("Unexpected null bean for operation: " + operationName);
                return null;
            }
            
            return new PerfStatsBeanImpl(this, operationName);
        } catch (Throwable t) {
            /* This is safe because @link glassbox.inspector.error.ErrorHandling will resolve it  */
            throw new RegistrationFailureException("can't register bean ", t);
        }
    }
    
    public Object TreeTimeStatsImpl.getMBean(JmxServerManager serverManager) {
        return serverManager.getDefaultMBean(this);
    }
    
    public Class TreeTimeStatsImpl.getManagementInterface() {
        return TreeTimeStatsManagementInterface.class;
    }
//    public String TreeTimeStatsImpl.getSlowestTrace() {
//        //XXX use the proper configured value here
//        SlowRequestDescriptor desc = getSlowestSignficant(getParent().getCount(), 0.05);
//        if (desc == null) {
//            return null;
//        }
//        return desc.toString();
//    }
    // this is good information but could be better formatted, probably as a nested tree of profiler-like data
    // especially given that the JConsole only shows a limit of maybe 1k of a String...
    public String TreeTimeStatsImpl.getSampledTimes() {
        return toString();
    }
    
    public String PerfStatsImpl.getTopic() {
        return "stats";
    }

    /** Make the @link PerfStats interface extend @link PerfStatsMBean, so all instances can be managed */
    declare parents: PerfStatsImpl extends PerfStatsMBean;
    declare parents: TreeTimeStatsImpl extends TreeTimeStatsMBean; 

    private String PerfStatsImpl.cachedOperationName;
    
    /** Determine JMX operation name for this performance statistics bean. */    
    public String PerfStatsImpl.getOperationName() {
        if (cachedOperationName != null) {
            return cachedOperationName;
        }
        
        StringBuffer buffer = new StringBuffer();
        appendOperationName(buffer);
        String operationName = buffer.toString();
        cachedOperationName = operationName; 
        return operationName;
    }
    
    /** Determine JMX operation name for this performance statistics bean. */    
    public void PerfStatsImpl.appendOperationName(StringBuffer buffer) {
        if (cachedOperationName != null) {
            buffer.append(cachedOperationName);
        } else {
            aspectOf().nameStrategy.appendOperationName(this, buffer);
        }
    }   
    
    public void PerfStatsImpl.appendName(StringBuffer buffer) {
        buffer.append('"');
        int pos = buffer.length();
        Object key = getKey();
        if (key instanceof Class) {
            buffer.append(((Class)key).getName());
        } else if (key instanceof Method){
            buffer.append(((Method)key).getName());
        } else if (key == null || key == StatisticsRegistryImpl.NULL_KEY) {
            buffer.append("connection");
        } else {
            buffer.append(key);
        }

        DefaultJmxServerManager.jmxEncode(buffer, pos);
        buffer.append('"');
    }
    
    public Class PerfStatsImpl.getManagementInterface() {
        return PerfStatsManagementInterface.class;
    }    
    
    public StatsJmxNameStrategy getNameStrategy() {
        return nameStrategy;
    }
    
    public void setNameStrategy(StatsJmxNameStrategy nameStrategy) {
        this.nameStrategy = nameStrategy;
    }

    pointcut setStatisticsKey(PerfStatsImpl stats): (execution(* setKey(..)) || execution(* setOwner(..))) && target(stats);
    
    after(PerfStatsImpl stats) returning: setStatisticsKey(stats) {
        if (stats.getOwner() != null) {
            // only register if the statistics were actually inserted: when there's a race 
            // the concurrent map may have chosen a different stats to be inserted
            JmxManagement.aspectOf().register(stats);
        }
    }
    
    pointcut removeStatistics() : execution(PerfStats StatisticsRegistry.removePerfStats(..));
    
    after() returning (PerfStatsImpl stats) : removeStatistics() {
        JmxManagement.aspectOf().unregister(stats);
    }

}
   