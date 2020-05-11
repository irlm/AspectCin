package anorb;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.logging.Logger;

import anorb.comunication.AnPackage;
import anorb.comunication.PackageBody;
import anorb.comunication.PackageHandler;
import anorb.comunication.Reply;
import anorb.comunication.ReplyException;
import anorb.comunication.Request;
import anorb.comunication.Sender;
import anorb.logging.AnLog;

public class Dispatcher implements PackageHandler {

	private static Dispatcher singleton;

	public static Dispatcher getSingleton() {
		if (singleton == null) {
			synchronized (Dispatcher.class) {
				if (singleton == null) {
					singleton = new Dispatcher();
				}
			}
		}
		return singleton;
	}

	private long stubCount;
	private Hashtable<Long, Stub> replyRegister;
	private Hashtable<String, Object> requestRegister;
	private Logger log = AnLog.comunication;

	private Dispatcher() {
		stubCount = 0;
		replyRegister = new Hashtable<Long, Stub>();
		requestRegister = new Hashtable<String, Object>();
	}

	public long registerStub(Stub stub) {
		long result = stubCount;
		replyRegister.put(result, stub);
		stubCount++;
		
		log.fine("Dispatcher registered " + stub + " with stub_id = " + result);
		log.fine("Next stub_id on dispatcher = " + stubCount);
		
		return result;
	}

	public void registerRemoteObject(String oid, Object ro) {
		if (requestRegister.contains(oid)) {
			requestRegister.remove(oid);
		}

		requestRegister.put(oid, ro);
	}

	private void onReplyArrived(AnPackage pkg) throws AnRemoteException {
		log.fine(this.toString() + " - Reply arrived");
		long stubId = pkg.getBody().getStubId();
		Stub stub = replyRegister.get(stubId);

		if (stub != null) {
			log.finest("Putting " + pkg + " on queue of " + stub);
			stub.onPackageArrived(pkg);
		} else {
			throw new AnRemoteException("Unregisted StubId");
		}
	}

	private AnPackage createPackage(PackageBody body, InetAddress host, int port) {
		AnPackage result = null;

		try {
			result = new AnPackage(InetAddress.getLocalHost(), AnORB
					.getLocalPort(), host, port);
			result.setBody(body);
		} catch (UnknownHostException e) {
		}

		return result;
	}	
	
	private void onRequestArrived(AnPackage pkg) throws AnRemoteException {
		log.fine(this.toString() + " - Request arrived");
		try {
			Request request = (Request) pkg.getBody();

			Object impl = requestRegister.get(request.getOId());
			if (impl != null) {
				Class[] parameterTypes = request.getParameterTypes();
				Object[] parameters = request.getParameters();
				Method method = impl.getClass().getMethod(request.getMethod(),
						parameterTypes);
				log.fine("Calling " + request.toString());
				try {
					Serializable returned = (Serializable) method.invoke(impl,
							parameters);
					Reply reply = new Reply(returned);
					pkg = createPackage(reply, pkg.getSource(), pkg
							.getSourcePort());
					reply.setStubId(request.getStubId());
					log.fine("Returning " + reply.toString());
					Sender.getSingleton().send(pkg);
				} catch (InvocationTargetException e) {

					ReplyException reply = new ReplyException(e.getTargetException());
					pkg = createPackage(reply, pkg.getSource(), pkg
							.getSourcePort());
					reply.setStubId(request.getStubId());
					log.fine("Returning " + reply.toString());
					Sender.getSingleton().send(pkg);
				}
			} else {
				log.warning("Object not found (" + request.getOId() + ")");
			}
		} catch (Exception e) {
			throw new AnRemoteException(e.getMessage());
		}
	}

	public void onPackageArrived(AnPackage pkg) throws AnRemoteException {
		if (pkg.getBody() instanceof Reply
				|| pkg.getBody() instanceof ReplyException) {
			onReplyArrived(pkg);
		} else {
			onRequestArrived(pkg);
		}
	}

}
