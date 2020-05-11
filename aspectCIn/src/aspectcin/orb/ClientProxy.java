package aspectcin.orb;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import aspectcin.orb.communication.api.Reply;
import aspectcin.orb.communication.api.Request;
import aspectcin.orb.communication.api.Sender;

public abstract class ClientProxy {

	private Queue<Reply> replyQueue;
	private RemoteObject target;
	private long stubId;
	
	public void init(RemoteObject target) {		
		this.replyQueue = new LinkedList<Reply>();
		this.target = target;
		this.stubId = PackageHandler.getSingleton().registerStub(this);
	}

	private Reply nextReply() {
		return nextReply(Long.MAX_VALUE);
	}
	
	private Reply nextReply(long timeout) {
		Reply result = null;

		while (replyQueue.size() == 0 && timeout > 0) {
			try {
				Thread.sleep(50);
				timeout -= 50;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (replyQueue.size() > 0) {
			result = replyQueue.poll();
		}

		return result;
	}	
	
	public Reply invokeRemoteMethod(String methodName, Class<?>[] parameterTypes, Serializable[] parameters) throws IOException {
		Request request = new Request(stubId, target.getOid(), methodName, parameterTypes, parameters);
		Sender.getSingleton().send(target, request); 
		return nextReply();
	}
	
	public void onPackageArrived(Reply reply){
		this.replyQueue.add(reply);
	}
}
