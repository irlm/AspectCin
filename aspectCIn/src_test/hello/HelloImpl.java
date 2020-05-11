package hello;

import aspectcin.AnRemoteException;
import aspectcin.RemoteObject;

public class HelloImpl extends RemoteObject implements Hello{

    private static final long serialVersionUID = 1L;

    public void sayHello() throws AnRemoteException{
        System.out.println("Hello");
    }

    public void sayHelloWithParameter(String msg) throws AnRemoteException, ParametroNuloException{
    	if (msg == null)
    		throw new ParametroNuloException();
    	
    	System.out.println(msg);
    }

    public String sayHelloWithEcho(String msg) throws AnRemoteException, ParametroNuloException{
    	if (msg == null)
    		throw new ParametroNuloException();
    	
    	System.out.println(msg);
        return  msg;
    }

}
