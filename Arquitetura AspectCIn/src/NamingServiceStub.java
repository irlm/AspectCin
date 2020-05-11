import java.io.Serializable;

public class NamingServiceStub extends ClientProxy implements NamingService {

	private static final long serialVersionUID = 7282821841724380863L;

	public NamingServiceStub(String namingAddress, int namingPort) {
		RemoteObject namingServiceTarget = new RemoteObject();
		namingServiceTarget.setHost(namingAddress);
		namingServiceTarget.setPort(namingPort);
		namingServiceTarget.setOid("NamingService");
		this.init(namingServiceTarget);
	}

	public void register(String oid, RemoteObject object) {

		RemoteObject ro = new RemoteObject();
		ro.setHost("Host");
		ro.setPort(CInORB.getLocalPort());
		ro.setOid(oid);
		// ro.setStubClass(object.getClass().getName().replaceFirst("Impl",
		// "Stub"));

		Class<?>[] parametersType = new Class[] { String.class,
				RemoteObject.class };

		Serializable[] parameters = new Serializable[] { oid, ro };

		Reply reply = invokeRemoteMethod("register", parametersType, parameters);

		packageHandler.registerRemoteObject(oid, object);
	}

	public Object lookup(String name) {
		ClientProxy result = null;

		Class<?>[] parametersType = new Class[] { String.class };

		Serializable[] parameters = new Serializable[] { name };

		Reply reply = this.invokeRemoteMethod("lookup", parametersType,
				parameters);
		RemoteObject target = (RemoteObject) reply.getReturned();

		ClientProxy clientProxy = null;

		result = clientProxy;

		result.init(target);

		return result;
	}

	public String[] list() {
		String[] result = null;

		Class<?>[] parametersType = new Class[] {};
		Serializable[] parameters = new Serializable[] {};

		Reply reply = invokeRemoteMethod("list", parametersType, parameters);
		result = (String[]) reply.getReturned();

		return result;
	}

	public void unregister(RemoteObject object) {
		// TODO Auto-generated method stub

	}

}
