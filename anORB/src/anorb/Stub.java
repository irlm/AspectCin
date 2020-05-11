package anorb;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

import anorb.comunication.AnPackage;
import anorb.comunication.PackageHandler;
import anorb.comunication.Request;
import anorb.comunication.Sender;

public abstract class Stub extends RemoteObject implements PackageHandler {

	private Queue<AnPackage> replyQueue;
	
	public void init() {		
		this.replyQueue = new LinkedList<AnPackage>();
		this.stubId = Dispatcher.getSingleton().registerStub(this);
	}

	private AnPackage nextReply() {
		return nextReply(Long.MAX_VALUE);
	}
	
	private AnPackage nextReply(long timeout) {
		AnPackage result = null;

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
	
	protected AnPackage invokeRemoteMethod(String methodName, Class[] parameterTypes,	Serializable[] parameters) throws IOException {
		Request request = new Request(stubId, oid, methodName, parameterTypes,	parameters);
		AnPackage pkg = new AnPackage(InetAddress.getLocalHost(), AnORB.getLocalPort(), host, port);
		pkg.setBody(request);
		Sender.getSingleton().send(pkg); 
		return nextReply();
	}
	
	public void onPackageArrived(AnPackage pkg){
		this.replyQueue.add(pkg);
	}
}
