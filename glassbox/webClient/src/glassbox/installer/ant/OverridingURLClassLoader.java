/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.installer.ant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 
 * Warning: non-public classes caused a problem here.
 * 
 * @author jheintz
 *
 */
class OverridingURLClassLoader extends URLClassLoader {

	String redefineClassName;

	public OverridingURLClassLoader(String redefineClassName, URL[] urls,
			ClassLoader parentLoader) {
		super(urls, parentLoader);
		this.redefineClassName = redefineClassName;
	}

	protected synchronized Class loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if (c == null) {
			try {
				c = findClass(name);
				//System.err.println("2. found " + name);

			} catch (ClassNotFoundException e) {
				c = getParent().loadClass(name);
//				System.err.println("2. parent " + name);
			}
		}

		if (resolve) {
		    resolveClass(c);
		}
		
		return c;
	}

	public URL getResource(String name) {
		
		URL url = findResource(name);

		if (url == null) {
		    url = getParent().getResource(name);
		}
		
		return url;
	}

	protected Class findClass(String name) throws ClassNotFoundException {
		Class result=null;
		if (name.equals(this.redefineClassName)) {
			String path = name.replace('.', '/').concat(".class");
			URL resource = getParent().getResource(path);
			try {
				byte[] bytes;
				bytes = toBytes(resource.openStream());
				result = defineClass(name, bytes, 0, bytes.length);
			} catch (IOException e1) {
				throw new IllegalStateException("Can't get class definition",
						e1);
			}
		} else {
			result = super.findClass(name);
		}
		
		return result;
	}

	/**
	 */
	static byte[] toBytes(InputStream is) {
		try {
			try {
				ByteArrayOutputStream result = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024 * 8];
				int count = 0;

				while ((count = is.read(buffer)) != -1) {
					result.write(buffer, result.size(), result.size() + count);
				}

				return result.toByteArray();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}
}
