

import java.io.Serializable;

public abstract class ClientProxy {

	
	/**
	 * @directed true
	 */
	
	private Reply reply;
	
	/**
	 * @directed true
	 */
	private RemoteObject target;
	private long stubId;
	
	/**
	 * @directed true
	 */
	Request request;
	
	
	/**
	 * @directed true
	 */
	
	Sender sender = new Sender();
	
	
	/**
	 * @directed true
	 */
	
	PackageHandler packageHandler;
	
	
	public void init(RemoteObject target) {		
		this.stubId = packageHandler.registerStub(this);
		this.target = target;
	}

	private Reply nextReply() {
		return nextReply(Long.MAX_VALUE);
	}
	
	private Reply nextReply(long timeout) {

		while (reply == null && timeout > 0) {

		}

		return reply;
	}	
	
	public Reply invokeRemoteMethod(String methodName, Class<?>[] parameterTypes, Serializable[] parameters){
		request = new Request(stubId, target.getOid(), methodName, parameterTypes, parameters);
		sender.send(target, request); 
		return nextReply();
	}
	
	public void onPackageArrived(Reply reply){
		this.reply = reply;
	}
}
