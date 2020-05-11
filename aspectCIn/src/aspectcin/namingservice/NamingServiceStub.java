package aspectcin.namingservice;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import aspectcin.orb.AnRemoteException;
import aspectcin.orb.CInORB;
import aspectcin.orb.ClientProxy;
import aspectcin.orb.PackageHandler;
import aspectcin.orb.RemoteObject;
import aspectcin.orb.communication.api.Reply;

public class NamingServiceStub extends ClientProxy implements NamingService {

	private static final long serialVersionUID = 7282821841724380863L;
	
	public NamingServiceStub(String namingAddress, int namingPort) {
		RemoteObject namingServiceTarget = new RemoteObject();
		namingServiceTarget.setHost(namingAddress);
		namingServiceTarget.setPort(namingPort);
		namingServiceTarget.setOid("NamingService");
		this.init(namingServiceTarget);
	}
	
	public void register(String oid, RemoteObject object) throws AnRemoteException {
		try {			
			RemoteObject ro = new RemoteObject();			
			ro.setHost(InetAddress.getLocalHost().getHostName());
			ro.setPort(CInORB.getLocalPort());
			ro.setOid(oid);					
			ro.setStubClass(object.getClass().getName().replaceFirst("Impl", "Stub"));			
			
			Class<?>[] parametersType = new Class[]{
					String.class, 
					RemoteObject.class
			};
			
			Serializable[] parameters = new Serializable[]{
					oid, 
					ro					
			};
			
			Reply reply = invokeRemoteMethod("register", parametersType, parameters);
			
			if (reply != null){
				PackageHandler.getSingleton().registerRemoteObject(oid, object);
			} else {
				throw new AnRemoteException("Unable to register!");
			}
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		}
	}

	public Object lookup(String name) throws AnRemoteException {
		ClientProxy result = null;

		try {
			Class<?>[] parametersType = new Class[]{
					String.class
			};
			
			Serializable[] parameters = new Serializable[]{ 
					name					
			};
			
			Reply reply = this.invokeRemoteMethod("lookup", parametersType, parameters);
			RemoteObject target = (RemoteObject) reply.getReturned();
			
			if (target != null){
				result = (ClientProxy) Class.forName(target.getStubClass()).newInstance();
				
				result.init(target);
			} else {
				throw new AnRemoteException("The object " + name + " was not found!");
			}			
		} catch (UnknownHostException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (InstantiationException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (ClassNotFoundException e) {
			//throw new AnRemoteException(e);
		}
		
		return result;
	}

	public String[] list() throws AnRemoteException {
		String[] result = null;
		
		try {
			Class<?>[] parametersType = new Class[]{};			
			Serializable[] parameters = new Serializable[]{};

			Reply reply = invokeRemoteMethod("list", parametersType, parameters);
			result = (String[]) reply.getReturned();			
			
		} catch (UnknownHostException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		} 
		
		return result;
	}

	public void unregister(RemoteObject object) throws AnRemoteException {
		// TODO Auto-generated method stub
			
		
	}

}
