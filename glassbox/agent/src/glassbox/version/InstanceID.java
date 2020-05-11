/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.version;

import glassbox.config.GlassboxInitializer;

import java.io.*;
import java.util.Properties;
import java.util.Random;

public class InstanceID implements Serializable {
   
    protected static String FILE_NAME="iid.dat";
    protected String iid = null; // could be used in future for support
    protected long firstUse = 0;
    protected long lastUse = 0;
    protected String agentBuildNum = null;
    protected String jvm = null;
    protected String os = null;
    protected static InstanceID instance = null;
    protected boolean shouldCheckUpdatesAutomatically = false;    
    private static final long serialVersionUID = 2;

    
    public boolean isShouldCheckUpdatesAutomatically() {
        return shouldCheckUpdatesAutomatically;
    }


    public void setShouldCheckUpdatesAutomatically(boolean shouldCheckUpdatesAutomatically) {
        this.shouldCheckUpdatesAutomatically = shouldCheckUpdatesAutomatically;
    }

    public static synchronized InstanceID getInstanceID() {
        if(instance == null) {
            instance = InstanceID.createInstanceID();
        }
        return instance;
    }
    
    protected static InstanceID createInstanceID() {
        InstanceID instance = null;
        if((instance = readObject()) == null) {
            instance = initializeIID();
            writeObject(instance);
        } else {
            instance.setLastUse(System.currentTimeMillis());
            writeObject(instance);
        }

        // get build number so we can check client vs agent compatability
        instance.setAgentBuildNum();
        return instance;
    }
    
    protected static InstanceID initializeIID() {
        InstanceID instanceId = new InstanceID();
        
        instanceId.setIid(String.valueOf(Math.abs(new Random().nextInt())));
        instanceId.setFirstUse(System.currentTimeMillis());
        instanceId.setLastUse(System.currentTimeMillis());
        instanceId.setJvm(System.getProperty("java.version", "not specified"));
        instanceId.setOs(System.getProperty("os.name", "not specified"));
        
        return instanceId;
    }
    
    
    
    protected static InstanceID readObject() {
       InstanceID instanceId = null;
        try {
            FileInputStream fis = new FileInputStream(getFileLocation());
            ObjectInputStream ois = new ObjectInputStream(fis);
            instanceId = (InstanceID)ois.readObject();
       } catch(Exception e) {
           //Create a new one.
       }
       return instanceId;
    }
    
    
    protected static void writeObject(InstanceID instance) {
        try {
            FileOutputStream fos = new FileOutputStream(getFileLocation());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(instance);
            oos.close();
        } catch(Exception e) {
            //Need to log.
        }
    }
    
    public long getFirstUse() {
        return firstUse;
    }
    public void setFirstUse(long firstUse) {
        this.firstUse = firstUse;
    }
    public String getIid() {
        return iid;
    }
    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getJvm() {
        return jvm;
    }
    public void setJvm(String jvm) {
        this.jvm = jvm;
    }
    public long getLastUse() {
        return lastUse;
    }
    public void setLastUse(long lastUse) {
        this.lastUse = lastUse;
    }
    public String getOs() {
        return os;
    }
    public void setOs(String os) {
        this.os = os;
    }

    public String getAgentBuildNum() {        
        return agentBuildNum;
    }
    public void setAgentBuildNum() {
        
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("glassbox_build.properties"));
            agentBuildNum = properties.getProperty("build.number");
        } catch (Exception e) {
            agentBuildNum = "null";
        } 
    }
    
    protected static String getFileLocation() {
        return GlassboxInitializer.CONFIG_DIR + File.separator + FILE_NAME;
    }
    
    
    public String getString(InstanceID instance) {
        StringBuffer buff = new StringBuffer();
        buff.append("Instance ");
        buff.append(" OS: ");
        buff.append(instance.getOs());
        buff.append(" | JVM: ");
        buff.append(instance.getJvm());
        buff.append(" | AgentBuildNum: ");
        buff.append(instance.getAgentBuildNum());
        return buff.toString();
    }
    
    public String getHTTPString() {
        StringBuffer buff = new StringBuffer();
        buff.append("os=");
        buff.append(getOs());
        buff.append("&jvm=");
        buff.append(getJvm());
        buff.append("&agentBuildNum=");
        buff.append(getAgentBuildNum());

        return buff.toString();
    }    
  
}
