package anorb.namingservice;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import anorb.AnORB;
import anorb.AnRemoteException;
import anorb.Dispatcher;
import anorb.RemoteObject;
import anorb.Stub;
import anorb.comunication.AnPackage;
import anorb.comunication.Reply;
import anorb.logging.AnLog;

public class NamingServiceStub extends Stub implements NamingService {

	private static final long serialVersionUID = 7282821841724380863L;
	
	private Logger log;

	public NamingServiceStub(String host, int port) throws UnknownHostException {
		log = AnLog.naming;
		this.oid = "NamingService";
		this.host = InetAddress.getByName(host);
		this.port = port;
	}	
	
	public void register(String oid, RemoteObject object) throws AnRemoteException {
		try {			
			RemoteObject ro = new RemoteObject();			
			ro.setHost(InetAddress.getLocalHost());
			ro.setPort(AnORB.getLocalPort());
			ro.setOid(oid);					
			ro.setStubClass(object.getClass().getName().replaceFirst("Impl", "Stub"));			
			
			Class[] parametersType = new Class[]{
					String.class, 
					RemoteObject.class
			};
			
			Serializable[] parameters = new Serializable[]{
					oid, 
					ro					
			};
			
			AnPackage pkg = invokeRemoteMethod("register", parametersType, parameters);
			
			if (pkg != null){
				Dispatcher.getSingleton().registerRemoteObject(oid, object);
			} else {
				throw new AnRemoteException("Unable to register!");
			}
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		}
	}

	public RemoteObject lookup(String name) throws AnRemoteException {
		Stub result = null;

		try {
			Class[] parametersType = new Class[]{
					String.class
			};
			
			Serializable[] parameters = new Serializable[]{ 
					name					
			};
			
			AnPackage pkg = invokeRemoteMethod("lookup", parametersType, parameters);
			Reply reply = (Reply) pkg.getBody();
			RemoteObject returned = (RemoteObject) reply.getReturned();
			
			if (returned != null){
				result = (Stub) Class.forName(returned.getStubClass()).newInstance();
				result.setOid(returned.getOid());
				result.setHost(returned.getHost());
				result.setPort(returned.getPort());
				result.init();
				log.fine("Stub registered (" + name + ", " + result.getStubId() + ")");
			} else {
				log.warning("The object " + name + " was not found!");
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
			e.printStackTrace();
		}
		
		return result;
	}

	public String[] list() throws AnRemoteException {
		String[] result = null;
		
		try {
			Class[] parametersType = new Class[]{};			
			Serializable[] parameters = new Serializable[]{};

			AnPackage pkg = invokeRemoteMethod("list", parametersType, parameters);
			Reply reply = (Reply) pkg.getBody();
			result = (String[]) reply.getReturned();			
			
		} catch (UnknownHostException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		} 
		
		return result;
	}

	public void unregister(RemoteObject object) {
		// TODO Auto-generated method stub

	}

}
