/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

/***** This class is generated by an IDL compiler and is ORB-vendor specific. 
       A "dummy" implementation is provided so that the "official" org.omg.*
       packages may be compiled.  In order to actually use a Java ORB,
       the ORB vendor's implementation will provide a "real"
       implementation of the class.

       In order to be conformant the class shall support the signatures
       specified here, but will have an orb-specific implementation.

       The class may support additional vendor specific functionality.
       It shall have at least the inheritance relationships specified
       here. Any additional (vendor specific) inheritance relationships may 
       only be with other classes and interfaces that are guaranteed to be 
       present in the JDK core.
*****/

package org.omg.CORBA;

public final class ParameterDescription implements
                org.omg.CORBA.portable.IDLEntity {

    public java.lang.String name;
    public org.omg.CORBA.TypeCode type;
    public org.omg.CORBA.IDLType type_def;
    public org.omg.CORBA.ParameterMode mode;

    public ParameterDescription() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ParameterDescription(java.lang.String name, 
                org.omg.CORBA.TypeCode type, 
                org.omg.CORBA.IDLType type_def, 
                org.omg.CORBA.ParameterMode mode) {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
