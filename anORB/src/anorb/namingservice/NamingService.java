package anorb.namingservice;

import anorb.AnRemoteException;
import anorb.RemoteObject;

public interface NamingService {

	public void register(String name, RemoteObject object)
			throws AnRemoteException;

	public RemoteObject lookup(String name) throws AnRemoteException;

	public String[] list() throws AnRemoteException;

	public void unregister(RemoteObject object) throws AnRemoteException;
}
