package edu.wustl.doc.facet;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;


/**
 * Generates IDL for the feature configuration by reflecting on the
 * "barebones" classes we have inside the 'EventComm' and
 * 'EventChannelAdmin' directories
 *
 * @author Ravi Pratap
 * @version $Revision: 1.16 $
 */
public class IdlGenerator {

	public static void main (String [] args)
	{
		//
		// The argument is the path where the generated IDL files
		// should be written
		//
		
		generateEventCommIDL (args [0]);
		generateEventChannelAdminIDL (args [0]);
	}

	/**
         * Returns the base name from the fully qualified name of the type
         *
         * @param name  String representing the type
         */
	private static String getBaseName (String name)
	{
		return name.substring (name.lastIndexOf ('.') + 1);
	}

	/**
         * Returns the fully qualified IDL name of the type
         *
         * @param type  Class representing the type
         */
	private static String getFQIDLTypeName (Class type)
	{
		
		String ns = null;
		Package p = type.getPackage ();

		if (p != null)
			ns = getBaseName (p.getName ()) + "::";
		else
			ns = "";

		String t = getBaseName (type.getName ());

		if (t.equals ("long"))
			t = "long long";
		else if (t.equals ("int"))
			t = "long";

		String ret = ns + t;

		//
		// Now perform replacements for the corresponding array types since
		// they seem to have their names mangled in strange ways.
		// eg. "[Ledu.wustl....Event;" is what you get for "Event []" !
		//
 		if (ret.equals ("Event;"))
 			ret = "EventComm::EventSet";
		else if (ret.equals ("Dependency;"))
			ret = "EventChannelAdmin::DependencySet";
		else if (ret.equals ("lang::String"))
			ret = "string";
		else if (ret.equals ("CORBA::Any"))
			ret = "any";
		else if (ret.equals ("[B"))
			ret = "EventComm::EventPayload";
		
		return ret; 
	}

	/**
         * Make a union of the method set, eliminating duplicates and
         * choosing the best overloaded version of a method [max # of args here]
         *
         * @param method_set    Array of methods to choose from
         */
	private static Method [] makeMethodUnion (Method [] method_set)
	{
		ArrayList list = new ArrayList ();

		for (int i = 0; i < method_set.length; ++i) {
			Method curr = method_set [i];
			boolean skip = false;
			
			for (int j = i + 1; j < method_set.length; ++j) {
				Method m = method_set [j];

				if (m.getName ().equals (curr.getName ())) {
					if (m.getParameterTypes ().length > curr.getParameterTypes ().length)
						skip = true;
					else
						skip = false;

					break;
				}
			}

			if (!skip)
				list.add (curr);
		}

		Method [] new_set = new Method [list.size ()];

		new_set = (Method []) list.toArray (method_set);

		return new_set;
	}

        /**
         * Dumps IDL for the specified type on the supplied PrintWriter
         *
         * @param pw            PrintWriter to dump to
         * @param type_name     The name of the type
         */
	public static void dumpIDLForType (PrintWriter pw, String type_name) 
	{
		Class c = null;
		
		try {
			c = Class.forName (type_name);
		} catch (Exception e) { e.printStackTrace (); }

		pw.println ();

                // Is the type an interface ?
		if (c.isInterface ()) {
			Class base_class = null;
			String super_class = null;

			if (c.getInterfaces ().length != 0)
				base_class = c.getInterfaces () [0];
				
			if (base_class != null)
				super_class = " : "  + getFQIDLTypeName (base_class); 
			else
				super_class = "";

			Method [] methods = makeMethodUnion (c.getDeclaredMethods ());

			pw.println ("\t interface " + getBaseName (c.getName ()) + super_class + " {");

			for (int i = 0; i < methods.length; ++i) {
				Method m = methods [i];

				if (m == null)
					continue;
				
				String name = m.getName ();
				int mods = m.getModifiers ();

				if ((mods & Modifier.PUBLIC) == 0)
					continue;

				if (name.startsWith ("around") || name.startsWith ("dispatch") ||
				    name.endsWith ("ajcPostCall")) 
					continue;

				pw.print ("\t \t " + getBaseName (m.getReturnType ().getName ()) + " " +
					  name + " (");
				
				Class [] p_types = m.getParameterTypes ();

				for (int j = 0; j < p_types.length; ++j) {
					String type = getFQIDLTypeName (p_types [j]);
					String param_type = "in";

					//
					// This is a very ugly hack but I can't think of anything
					// else which does the job and I am in a hurry !
					//
					if (type.equals ("CORBA::BooleanHolder")) {
						type = "boolean";
						param_type = "out";
					}
					
					pw.print (param_type + " " + type + " param" + j);
					
					if (j != p_types.length - 1)
						pw.print (", ");
				}
			
				pw.print (");");
				pw.println ();
			}

			//
			// This is to take care of interfaces which have
			// constant fields like in FilterOpTypes
			//

			Field [] fields = c.getDeclaredFields ();

			for (int i = 0; i < fields.length; ++i) {
				Field f = fields [i];
				
				if ((f.getModifiers () & Modifier.FINAL) == 0)
					continue;
				
				String type = getFQIDLTypeName (f.getType ());
                                int val = -1;

                                try {
                                        val = f.getInt (c);
                                } catch (Throwable e) { e.printStackTrace (); }
                                
				pw.println ("\t \t const " + type + "\t " + f.getName () + " = " +
                                            val + ";");
			}
				
			pw.println ("\t };");
			
		} else {
                        // Okay, so this is a class
                        
			String t = getBaseName (c.getName ());

			if (t.endsWith ("Exception")) {
				pw.println ("\t exception " + t + " {};");
				
			} else {
						
				Field [] fields = c.getDeclaredFields ();

				if (fields.length != 0) {
					pw.println ("\t struct " + t + " {");

					for (int i = 0; i < fields.length; ++i) {
						Field f = fields [i];
						String type = getFQIDLTypeName (f.getType ());

						pw.println ("\t \t " + type + "\t " + f.getName () + ";");
					}
				
					pw.println ("\t };");

					if (t.equals ("Event")) 
						pw.println ("\n \t typedef sequence<Event> EventSet; \n \n");

					if (t.equals ("Dependency"))
						pw.println ("\n \t typedef sequence<Dependency> DependencySet; \n \n");

				
				}

			}
		}
	}

	/**
         * Dump IDL for types in EventComm
         *
         * FIXME! See below!
         */
	public static void dumpEventCommIDL (PrintWriter pw)
	{
		pw.println ("\n \t typedef sequence<octet> EventPayload; \n");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.EventHeader");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.Event");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.PushConsumer");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.PullConsumer");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.PushSupplier");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventComm.PullSupplier");
	}

        /**
         * Dump IDL for types in EventChannelAdmin
         *
         * FIXME: This should be modified to use aspects which automatically register
         * a particular type for IDL generation!!
         *
         */
	public static void dumpEventChannelAdminIDL (PrintWriter pw)
	{
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.EventChannelException");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.FilterOpTypes");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.Dependency");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ConsumerQOS");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ProxyPushConsumer");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ProxyPullConsumer");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ProxyPushSupplier");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ProxyPullSupplier");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.ConsumerAdmin");
	        dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.SupplierAdmin");
		dumpIDLForType (pw, "edu.wustl.doc.facet.EventChannelAdmin.EventChannel");
	}

        /**
         * Generate EventComm.idl
         *
         * @param path  Path to where the file should be created
         */
	public static void generateEventCommIDL (String path)
	{
		try {
			String filename = path + "EventComm.idl";
			PrintWriter pw = new PrintWriter (new FileWriter (filename));
			
			pw.println ("#ifndef __EventComm_IDL");
			pw.println ("#define __EventComm_IDL");
			pw.println ();
			pw.println ("module edu {");
			pw.println ("module wustl {");
			pw.println ("module doc {");
			pw.println ("module facet {");
			pw.println ();
			pw.println ("module EventComm {");

			dumpEventCommIDL (pw);

			pw.print ("\n};\n \n};\n};\n};\n};\n \n");
			pw.println ("#endif");
			pw.close ();

		} catch (Exception e) { e.printStackTrace (); }
	}

        /**
         * Generate EventChannelAdmin.idl
         *
         * @param path  Path to where the file should be created
         */
	public static void generateEventChannelAdminIDL (String path)
	{
		try {
			String filename = path + "EventChannelAdmin.idl";
			PrintWriter pw = new PrintWriter (new FileWriter (filename));
			
			pw.println ("#ifndef __EventChannelAdmin_IDL");
			pw.println ("#define __EventChannelAdmin_IDL");
			pw.println ();
			pw.println ("#include <EventComm.idl>");
			pw.println ();
			pw.println ("module edu {");
			pw.println ("module wustl {");
			pw.println ("module doc {");
			pw.println ("module facet {");
			pw.println ();
			pw.println ("module EventChannelAdmin {");
			
			dumpEventChannelAdminIDL (pw);

			pw.print ("\n};\n \n};\n};\n};\n};\n \n");
			pw.println ("#endif");
			pw.close ();

		} catch (Exception e) { e.printStackTrace (); }
	}
	
}
