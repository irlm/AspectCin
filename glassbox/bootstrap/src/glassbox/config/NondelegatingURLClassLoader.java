/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class NondelegatingURLClassLoader extends URLClassLoader {
    public NondelegatingURLClassLoader(URL[] urls, ClassLoader parentLoader, URLStreamHandlerFactory urlStreamHandlerFactory) {
        super(urls, parentLoader, urlStreamHandlerFactory);
    }

    public NondelegatingURLClassLoader(URL[] urls, ClassLoader parentLoader) {
        super(urls, parentLoader);
    }

    public NondelegatingURLClassLoader(URL[] urls) {
        super(urls);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("com.sun.")
                || name.startsWith("org.xml") || name.startsWith("org.w3c")) {
            return super.loadClass(name);
        }

        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                return getParent().loadClass(name);
            }
        }

        return c;
    }
    
    public URL getResource(String name) {
        if (name.startsWith("java/") || name.startsWith("javax/") || name.startsWith("sun/") ||
          name.startsWith("com/sun/") || name.startsWith("org/xml/") || name.startsWith("org/w3c/")) {
            return super.getResource(name);
        }

        URL url = findResource(name);
        if (url != null) {
            return url;
        }

        return getParent().getResource(name);
    }
    
}
