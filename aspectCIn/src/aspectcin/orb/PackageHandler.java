package aspectcin.orb;

import java.util.Hashtable;

import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.api.Reply;
import aspectcin.orb.communication.api.Request;

public class PackageHandler {

	private long stubCount;
	private Hashtable<Long, ClientProxy> replyRegister;
	private Hashtable<String, Object> requestRegister;
	
	private static PackageHandler singleton;

	public static PackageHandler getSingleton() {
		if (singleton == null) {
			synchronized (PackageHandler.class) {
				if (singleton == null) {
					singleton = new PackageHandler();
				}
			}
		}
		return singleton;
	}

	private PackageHandler() {
		stubCount = 0;
		replyRegister = new Hashtable<Long, ClientProxy>();
		requestRegister = new Hashtable<String, Object>();
	}

	public long registerStub(ClientProxy stub) {
		long result = stubCount;
		
		replyRegister.put(result, stub);
		stubCount++;

		return result;
	}

	public void registerRemoteObject(String oid, Object ro) {
		if (requestRegister.contains(oid)) {
			requestRegister.remove(oid);
		}

		requestRegister.put(oid, ro);
	}

	public long getStubCount() {
		return stubCount;
	}

	public final void onPackageArrived(AnPackage anPackage) throws AnRemoteException {
		if (anPackage.getBody() instanceof Reply) {
			onReplyArrived(anPackage);
		} else {
			onRequestArrived(anPackage);
		}
	}
	
	private void onReplyArrived(AnPackage pkg) throws AnRemoteException {
		long stubId = pkg.getBody().getStubId();
		ClientProxy stub = this.replyRegister.get(stubId);

		if (stub != null) {
			stub.onPackageArrived((Reply)pkg.getBody());
		} else {
			throw new AnRemoteException("Unregisted StubId");
		}
	}
	
	private void onRequestArrived(AnPackage anPackage) throws AnRemoteException {
		
		try {
			Request request = (Request) anPackage.getBody();

			Object impl = this.requestRegister.get(request.getOId());
			if (impl != null) {
				
				RemoteObject target = new RemoteObject();
				target.setHost(anPackage.getSource().getHostName());
				target.setPort(anPackage.getSourcePort());
				
				ServerProxy.invoke(target, request, impl);
			} else {
				//TODO verificar se é para lançar exceção mesmo
				throw new AnRemoteException("Object not found (" + request.getOId() + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnRemoteException(e.getMessage());
		}
	}
		
}
