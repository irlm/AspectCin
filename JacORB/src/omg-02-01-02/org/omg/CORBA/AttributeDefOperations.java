/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface AttributeDefOperations extends
                    org.omg.CORBA.ContainedOperations {

    public org.omg.CORBA.TypeCode type();

    public org.omg.CORBA.IDLType type_def();
    public void type_def(org.omg.CORBA.IDLType type_def);

    public org.omg.CORBA.AttributeMode mode();
    public void mode(org.omg.CORBA.AttributeMode mode);

}
