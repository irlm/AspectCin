

import java.io.Serializable;

public class ServerProxy {

	
	/**
	 * @directed true
	 */
	
	Sender sender;
	
	/**
	 * @directed true
	 */
	
	Reply reply;
	
	/**
	 * @directed true
	 */
	
	RemoteObject target;
	
	/**
	 * @directed true
	 */
	Request request;
	
	public void invoke(RemoteObject target, Request request, Object impl) {
		
		Serializable serializable = null;
		
		reply = new Reply(serializable);
		
		reply.setStubId(request.getStubId());		
		sender.send(target, reply);
	}

}
