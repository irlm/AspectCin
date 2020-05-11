/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.jacorb.ir.gui.remoteobject;


import org.jacorb.ir.gui.typesystem.*;
import org.jacorb.ir.gui.typesystem.remote.*;
import org.omg.CORBA.*;
import java.lang.reflect.*;	// zum Auslesen der Felder des Structs
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class Struct extends ObjectRepresentant implements AbstractContainer{



/**
 * Struct constructor comment.
 * @param counterPart java.lang.Object
 */
protected Struct(java.lang.Object counterPart, IRStruct typeSystemNode, String name) {
	super(counterPart,typeSystemNode,name);
}
/**
 * This method was created by a SmartGuide.
 * @return ModelParticipant[]
 */
public ModelParticipant[] contents() {
	if (counterPart!=null) {
		// wir holen uns den Inhalt des Struct per Reflection
		IRStructMember[] members = (IRStructMember[])((AbstractContainer)typeSystemNode).contents();
		ModelParticipant[] result = new ModelParticipant[members.length];
		for (int i=0; i<members.length; i++) {
			try {
				Field field = counterPart.getClass().getDeclaredField(members[i].getName());
				result[i] = ObjectRepresentantFactory.create(
					field.get(counterPart),			// per Reflection auslesen
					members[i].getAssociatedTypeSystemNode(),
					members[i]);
			}
			catch (Exception e) {
				e.printStackTrace();
			}		
		}	
		return result;
	}	// if counterPart!=null
	else  {
		return new ModelParticipant[0];
	}	
}
}









