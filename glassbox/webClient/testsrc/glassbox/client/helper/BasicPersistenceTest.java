/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import glassbox.client.persistence.jdbc.ConfigurationDAO;
import glassbox.common.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class BasicPersistenceTest extends BaseTestCase {

    public BasicPersistenceTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BasicPersistenceTest.class);
        return suite;
    }

    public void testPersistenceSetUp() {
    	 System.out.println("Testing the Persistence Set up....");        
         assertNotNull(getContext());
                 
         ConfigurationDAO configDAO = (ConfigurationDAO)getContext().getBean("configurationDAO");
         assertNotNull(configDAO);        
         
         configDAO.configure();
         
         //assertFalse(configDAO.getConfiguration());    
         //configDAO.setConfiguration();        
         //assertTrue(configDAO.getConfiguration());       
         System.out.println(getEnv());
         
         configDAO.insert();       
         configDAO.defragment();
         assertTrue(configDAO.getConfiguration());       
         configDAO.insert();
         configDAO.shutdown();
        
    }
    
    /*
     * This is for debugging purposes. It should be included in junit failure report output
     */
    public String getEnv() {
    	StringBuffer result = new StringBuffer();
    	
    	Map getenv = System.getenv();
    	result.append("System Environment:\n");
    	for (Iterator iter = getenv.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			result.append(" "+key+":"+getenv.get(key)+"\n");
		}

    	Properties properties = System.getProperties();
    	result.append("System Properties:\n");
    	for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			result.append(" "+key+":"+properties.get(key)+"\n");
		}

    	
    	return result.toString();
    }
}
