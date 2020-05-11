package edu.wustl.doc.utils;

import junit.framework.*;
import java.util.Hashtable;

public class FacetMTTestCase extends MultiThreadedTestCase 
{
	/**
	 * Convenience hash table to store object references.
	 * This is useful when we don't have a Naming Service or when
	 * using the Naming Service tests out too much.
	 */
	private Hashtable iorTable_ = null;
	
	public FacetMTTestCase (String s)
	{
		super (s);
	}

	public synchronized void addCorbaObjectIor (String name, String ior)
	{
		if (iorTable_ == null) {
			iorTable_ = new Hashtable(10);
		}

		iorTable_.put (name, ior);
	}

        public synchronized void addJavaObjectRef (String name, Object ior)
	{
		if (iorTable_ == null) {
			iorTable_ = new Hashtable(10);
		}
		
		iorTable_.put (name, ior);
	}

	public synchronized String getCorbaObjectIor (String name)
	{
		return (String) iorTable_.get (name);
	}
	
	public synchronized Object getJavaObjectRef (String name)
	{
		return iorTable_.get (name);
	}
}


				     
