package projeto.anORB.namingservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import projeto.anORB.Dispatcher;
import projeto.anORB.Package;
import projeto.anORB.RemoteObject;
import projeto.anORB.Request;
import projeto.test.AnRemoteException;

public class NamingServiceStub implements NamingService {

	private long stubId;

	private InetAddress host;

	private Dispatcher dispatcher;

	private Hashtable<String, RemoteObject> localRegistry;

	public NamingServiceStub(String host) throws UnknownHostException {
		this.host = InetAddress.getByName(host);
		this.dispatcher = Dispatcher.getSingleton();
		this.stubId = dispatcher.register(this.host, 2179);
		this.localRegistry = new Hashtable<String, RemoteObject>();
	}

	public void register(String oid, RemoteObject object)
			throws AnRemoteException {
		try {
			localRegistry.put(oid, object);
			Package pkg = new Package(InetAddress.getLocalHost(), host);
			String stubName = object.getClass().getName().replaceFirst("Impl",
					"Stub");
			Request req = new Request("NamingService", "register", oid,
					stubName);
			req.setStubId(this.stubId);
			pkg.setBody(req);
			dispatcher.send(pkg);
		} catch (UnknownHostException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		}
	}

	public RemoteObject lookup(String name) throws AnRemoteException {
		RemoteObject ro;

		try {
			Package pkg = new Package(InetAddress.getLocalHost(), host);
			Request req = new Request("NamingService", "lookup", name);
			req.setStubId(this.stubId);
			pkg.setBody(req);
			dispatcher.send(pkg);

			pkg = dispatcher.receive(this.stubId);
			Reply reply = (Reply) pkg.getBody();
			String[] str = ((String) reply.getResult()).split("|");

			ro = (RemoteObject) Class.forName(str[1]).newInstance();
			ro.setOid(str[0]);
			ro.setClassName(str[1]);
			ro.setHost(InetAddress.getByName(str[2]));
		} catch (UnknownHostException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IOException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (InstantiationException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new AnRemoteException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new AnRemoteException(e.getMessage());
		}

		return ro;
	}

	public String[] list() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unregister(RemoteObject object) {
		// TODO Auto-generated method stub

	}

}
