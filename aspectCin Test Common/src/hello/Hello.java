package hello;

import aspectcin.orb.AnRemoteException;


public interface Hello {
	void sayHello() throws AnRemoteException;
	
	void sayHelloWithParameter(String msg) throws AnRemoteException, ParametroNuloException;
	
	String sayHelloWithEcho(String msg) throws AnRemoteException, ParametroNuloException;

	Primitive primitive(Primitive primitive) throws AnRemoteException,ParametroNuloException;
}
