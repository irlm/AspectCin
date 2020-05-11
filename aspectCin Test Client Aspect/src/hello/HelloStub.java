package hello;

import aspectcin.orb.AnRemoteException;
import aspectcin.orb.ClientProxy;

public class HelloStub extends ClientProxy implements Hello {

	private static final long serialVersionUID = 1L;

	public void sayHello() throws AnRemoteException {

		try{
			
		}catch (Exception e) {
			if (e.getCause() instanceof AnRemoteException) {
				throw (AnRemoteException) e.getCause() ;
			}
		}
	}

	public void sayHelloWithParameter(String msg) throws AnRemoteException,
			ParametroNuloException {

		try{
			
		}catch (Exception e) {
			if (e.getCause() instanceof AnRemoteException) {
				throw (AnRemoteException) e.getCause() ;
			}
			if (e.getCause() instanceof ParametroNuloException) {
				throw (ParametroNuloException) e.getCause() ;
			}
		}
	}

	public String sayHelloWithEcho(String msg) throws AnRemoteException,
			ParametroNuloException {
		try{
			
		}catch (Exception e) {
			if (e.getCause() instanceof AnRemoteException) {
				throw (AnRemoteException) e.getCause() ;
			}
			if (e.getCause() instanceof ParametroNuloException) {
				throw (ParametroNuloException) e.getCause() ;
			}
		}
		return null;
	}

	public Primitive primitive(Primitive primitive) throws AnRemoteException,
			ParametroNuloException {
		
		try{
			
		}catch (Exception e) {
			if (e.getCause() instanceof AnRemoteException) {
				throw (AnRemoteException) e.getCause() ;
			}
			if (e.getCause() instanceof ParametroNuloException) {
				throw (ParametroNuloException) e.getCause() ;
			}
		}
		return null;
	}

}
