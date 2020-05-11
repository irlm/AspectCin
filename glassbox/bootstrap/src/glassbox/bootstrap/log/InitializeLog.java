/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.bootstrap.log;

import glassbox.config.GlassboxInitializer;
import glassbox.thread.context.SavedContextLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InitializeLog {

    public static final String LIMIT_COMPONENT_LOGGING_PROPERTY = "glassbox.limit.component.logging";
    
    private static Set held = new HashSet();
    private static Set roots = new HashSet();

    static {
        eagerlyLoadSharedJars();
    }
    
    /** 
     * 
     * Ugly: this method asks the parent to load any parts of commons logging that it can, to avoid nondelegating loading...
     * This is based on JCL 1.0.3, i.e., it lists ALL of the JCL classes that our web app bundles  
     * 
     * Is it possible that logging configuration could get *more* complicated? 
     */
    public static void eagerlyLoadSharedJars() {
        SavedContextLoader savedContextLoader = new SavedContextLoader(); 
        try {
            doEagerlyLoadCommonsLogging();
        } finally {
            savedContextLoader.restore();
        }    
    }

    private static void doEagerlyLoadCommonsLogging() {
/*        String classes[] = {
                "org.apache.commons.logging.Log",
                "org.apache.commons.logging.LogFactory",
                "org.apache.commons.logging.LogFactory$1",
                "org.apache.commons.logging.LogFactory$2",
                "org.apache.commons.logging.LogFactory$3",
                "org.apache.commons.logging.LogSource",
                "org.apache.commons.logging.LogConfigurationException",
                "org.apache.commons.logging.impl.SimpleLog",
                "org.apache.commons.logging.impl.SimpleLog$1",
                "org.apache.commons.logging.impl.LogFactoryImpl",
                "org.apache.commons.logging.impl.LogFactoryImpl$1",
                "org.apache.commons.logging.impl.Log4JLogger",
                "org.apache.commons.logging.impl.Log4JCategoryLog",
                "org.apache.commons.logging.impl.Log4JFactory",
                "org.apache.commons.logging.impl.Jdk14Logger",
                "org.apache.commons.logging.impl.AvalonLogger",
                "org.apache.commons.logging.impl.LogKitLogger",
                "org.apache.commons.logging.impl.NoOpLog",
                "org.apache.commons.logging.impl.Log4jProxy" // JBoss's addition
        };*/
        doEagerlyLoad("org.apache.commons.logging.Log");
    }

    private static synchronized void doEagerlyLoad(String className) {
        if (roots.contains(className)) {
            return;
        }
        roots.add(className);
        ClassLoader myLoader = InitializeLog.class.getClassLoader();
        
        // this RELIES on *Glassbox* having these deployed as a jar...
        
        if (myLoader != null) {
            ClassLoader parent = myLoader.getParent();
            URL url = myLoader.getResource(className.replace('.', '/').replace('$', '/')+".class");
            if (url != null) {
                try {
                    String jarloc = getJarLoc(url);
                    if (jarloc == null) {
                        // this is normal in dev configurations where eagerly loading isn't desirable anyhow
                        System.err.println("Warning: can't eagerly load from exploded directory "+url);
                    } else {
                        try {
                            ZipInputStream zis = new ZipInputStream(new FileInputStream(jarloc));
                            for (;;) {
                                ZipEntry entry = zis.getNextEntry();
                                if (entry == null) {
                                    break;
                                }
                                String name = entry.getName();
                                if (name.endsWith(".class")) {
                                    name=name.replace('/', '.').substring(0, name.length()-".class".length());
                                    if (tryToLoad(name, parent) == null) {
                                        // force loading now with the right context loader...
                                        tryToLoad(name, myLoader);
                                    }
                                }
                            }
                        } catch (IOException e) {
                        	System.err.println("Can't find ["+jarloc+"]");
                            cantLoadError(url, e);
                        }
                    }
                } catch (MalformedURLException e) {
                    cantLoadError(url, e);
                } catch (UnsupportedEncodingException e) {
                	cantLoadError(url, e);
                }
            }
        }
    }
    
    public static String getJarLoc(URL url) throws MalformedURLException, UnsupportedEncodingException {
        String resloc = url.toString();
        int splitPos = resloc.lastIndexOf('!');
        if (splitPos==-1 || splitPos==(resloc.length()-1)) {
            resloc = null;
        } else {
			resloc = URLDecoder.decode(resloc.substring("jar:".length(), splitPos), "UTF-8");
            if(resloc.startsWith("file:")) {
            	resloc = resloc.substring("file:".length());
                // handle file:/c:/, which should be c:/
            	if(isWindows() && resloc.length()>2 && resloc.charAt(0)=='/' && Character.isLetter(resloc.charAt(1)) && resloc.charAt(2)==':') {
            		resloc = resloc.substring(1);
            	}
            }
            if(resloc.startsWith("-source:")) {
            	resloc = resloc.substring("-source:".length());
                // handle system:\c:, which should be c:
            	if(isWindows() && resloc.length()>2 && resloc.charAt(0)=='\\' && Character.isLetter(resloc.charAt(1)) && resloc.charAt(2)==':') {
            		resloc = resloc.substring(1);
            	}
            }
            if (resloc.length()>0 && resloc.charAt(resloc.length()-1)==File.separatorChar) {
                resloc = resloc.substring(0, resloc.length()-1);
            }
        }
        return resloc;
    }
    
    private static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os != null && os.indexOf("indow") >= 0) {
			return true;
		}
		return false;
	}
        
    private static void cantLoadError(Object jar, Throwable t) {
        System.err.println("Warning: can't eagerly load jar "+jar);
        t.printStackTrace();
    }

    private static Class tryToLoad(String className, ClassLoader loader) {
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Class result = Class.forName(className, false, loader);
            held.add(result); // pin it to avoid GC...
            return result;
        } catch (ClassNotFoundException cne) {
            // ok - not present in parent
        } catch (NoClassDefFoundError cne) {
            // ok - not present in parent
        }
        return null;
    }

    public static void initializeLogging() {
        eagerlyLoadSharedJars();
        
        if (shouldLimitComponentLogging()) {
            setWarnLogging("uk.ltd.getahead.dwr.impl.ExecuteQuery");
            setWarnLogging("uk.ltd.getahead.dwr.impl.DefaultConfiguration");
            setWarnLogging("uk.ltd.getahead.dwr.impl.DefaultConverterManager");
            setWarnLogging("org.springframework.ui.velocity.CommonsLoggingLogSystem");            
        }
    }

    private static boolean shouldLimitComponentLogging() {
        // TODO: move these to Web client initialization instead
        InputStream str = null;
        try {
        	str = new FileInputStream(GlassboxInitializer.CONFIG_DIR + File.separator + "glassbox.properties");
        } catch(FileNotFoundException ioe) {
        	BootstrapLog.debug("couldn't find glassbox.properties", ioe);//not found: assume proceed
        }
        if (str != null) {
            Properties p = new Properties();
            try {
                p.load(str);
                String val = p.getProperty(LIMIT_COMPONENT_LOGGING_PROPERTY, "true");
                return "true".equalsIgnoreCase(val);
            } catch (IOException ioe) {
                BootstrapLog.debug("can't read glassbox.properties", ioe);//not found: assume proceed
            }            
        }
        return true;
    }

    private static void setWarnLogging(String className) {
        try {
            Object logObject = InitializeLog.getField(Class.forName(className), null, "log");
            if (logObject == null) {
                logObject = InitializeLog.getField(Class.forName(className), null, "logger");
            }
            if (logObject != null) {
                if (logObject.getClass().getName().indexOf(".dwr.")>0) {                    
                    Object output = InitializeLog.getField(logObject, "output");
                    logObject = InitializeLog.getField(output, "log");                    
                }
                BootstrapLog.setLogWarning(logObject, className);                    
            }
        } catch (Throwable t) {
            BootstrapLog.debug("Can't set warn level logging for "+className, t);
        }
    }

    private static Object getField(Class clazz, Object holder, String fieldName) throws Exception {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field == null) return null;
            field.setAccessible(true);
            return field.get(holder);
        } catch (NoSuchFieldError noField) {
            return null;
        } catch (NoSuchFieldException noField) {
            return null;
        }
    }

    static Object getField(Object holder, String fieldName) throws Exception {
        return getField(holder.getClass(), holder, fieldName);
    }

}