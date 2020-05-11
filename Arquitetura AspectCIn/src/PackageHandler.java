

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class PackageHandler {

	private long stubCount;
	
	/**
	 * @directed true
	 */
	private Vector<ClientProxy> replyRegister;
	
	/**
	 * @directed true
	 */
	private Vector<RemoteObject> requestRegister;
	
	
	/**
	 * @directed true
	 */
	
	ServerProxy serverProxy;
	/**
	 * @directed true
	 */
	
	private PackageHandler() {
		stubCount = 0;
		replyRegister = new Vector<ClientProxy>();
		requestRegister = new Vector<RemoteObject>();
	}

	public long registerStub(ClientProxy stub) {
		long result = stubCount;
		
		replyRegister.add(stub);
		stubCount++;

		return result;
	}

	public void registerRemoteObject(String oid, Object ro) {
		requestRegister.add((RemoteObject)ro);
	}

	public long getStubCount() {
		return stubCount;
	}

	public final void onPackageArrived(AnPackage anPackage) {
		if (anPackage.getBody() instanceof Reply) {
			onReplyArrived(anPackage);
		} else {
			onRequestArrived(anPackage);
		}
	}
	
	private void onReplyArrived(AnPackage pkg) {
		long stubId = pkg.getBody().getStubId();
		ClientProxy stub = this.replyRegister.get((int)stubId);
		stub.onPackageArrived((Reply)pkg.getBody());
	}
	
	private void onRequestArrived(AnPackage anPackage){
		
			Request request = (Request) anPackage.getBody();

			Object impl = this.requestRegister.get((int)stubCount);
				
			RemoteObject target = new RemoteObject();
			target.setHost(anPackage.getSource());
			target.setPort(anPackage.getSourcePort());
				
			serverProxy.invoke(target, request, impl);
	}
		
}
