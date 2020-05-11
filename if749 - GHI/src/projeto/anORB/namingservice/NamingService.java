package projeto.anORB.namingservice;

import projeto.anORB.RemoteObject;
import projeto.test.AnRemoteException;

public interface NamingService {

	public void register(String name, RemoteObject object)
			throws AnRemoteException;

	public RemoteObject lookup(String name) throws AnRemoteException;

	public String[] list() throws AnRemoteException;

	public void unregister(RemoteObject object) throws AnRemoteException;
}
