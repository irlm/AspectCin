/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import java.util.*;

public class MessageHelper extends BaseHelper  {
 
    private static final String BUNDLE_NAME = "glassbox.client.helper.performance";
    private static final ResourceBundle BASE_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    private static CompoundResourceBundle compoundBundle = new CompoundResourceBundle(BASE_RESOURCE_BUNDLE);
    
    private MessageHelper() {}
    
    public static String getString(String key) {
        
        try {
            return compoundBundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    public static Enumeration getKeys() {
        try {
            return compoundBundle.getKeys();
        } catch (MissingResourceException e) {
            return null;
        }
    }
    
    public static void addBundle(ResourceBundle bundle) {
        compoundBundle.add(bundle);
    }

    public static void removeBundle(ResourceBundle bundle) {
        compoundBundle.remove(bundle);
    }
    
    private static class CompoundResourceBundle extends ResourceBundle {
        private List contained = new LinkedList();
        
        public CompoundResourceBundle(ResourceBundle base) {
            add(base);
        }

        public void add(ResourceBundle bundle) {
            contained.add(bundle);
        }
        
        public void remove(ResourceBundle bundle) {
            contained.remove(bundle);
        }
        
        public Enumeration getKeys() {
            return new Enumeration() {
                private Iterator it = contained.iterator();
                private Enumeration inner = null;
                
                public boolean hasMoreElements() {
                    while (inner==null || !inner.hasMoreElements()) {
                        if (!it.hasNext()) {
                            return false;
                        }
                        ResourceBundle bundle = (ResourceBundle)it.next();
                        inner = bundle.getKeys();
                    }
                    return true;
                }

                public Object nextElement() {
                    if (!hasMoreElements()) {
                        throw new NoSuchElementException();
                    }
                    return inner.nextElement();
                }                
            };
        }

        protected Object handleGetObject(String key) {
            for (Iterator it=contained.iterator(); it.hasNext();) {
                ResourceBundle bundle = (ResourceBundle)it.next();
                try {
                    return bundle.getObject(key);
                } catch (MissingResourceException annoyingBadApiInTheJdk) {
                    ;
                }
            }
            return null;
        }
        
    };
    
}
