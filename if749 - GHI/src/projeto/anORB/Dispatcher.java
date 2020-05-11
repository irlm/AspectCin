package projeto.anORB;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import projeto.anORB.comunication.ConnectionPool;
import projeto.anORB.comunication.PackageHandler;
import projeto.anORB.comunication.Sender;

public class Dispatcher implements PackageHandler {

	private class Register {
		InetAddress host;

		int port;

		Queue<Package> queue;

		public Register(InetAddress host2, int port2, LinkedList<Package> name) {
			host = host2;
			port = port2;
			queue = name;
		}
	}

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

	private long count;

	private Hashtable<Long, Dispatcher.Register> register;

	private Dispatcher() {
		count = 0;
		register = new Hashtable<Long, Dispatcher.Register>();
	}

	public long register(InetAddress host, int port) {
		long result = count;
		Register reg = new Register(host, port, new LinkedList<Package>());
		register.put(result, reg);

		count++;

		return result;
	}

	public void send(Package pkg) throws IOException {

		ConnectionPool pool = ConnectionPool.getSingleton();
		Register reg = register.get(pkg.getBody().getStubId());
		Sender sender = pool.getConnection(reg.host, reg.port);
		sender.send(pkg);
	}

	public Package receive(long stubId) throws IOException {
		return receive(stubId, Long.MAX_VALUE);
	}

	public Package receive(long stubId, long timeout) throws IOException {
		Package result = null;

		Register reg = register.get(stubId);
		while (reg.queue.size() == 0 && timeout > 0) {
			try {
				Thread.sleep(100);
				timeout -= 100;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (reg.queue.size() > 0) {
			result = reg.queue.poll();
		}

		return result;
	}

	public void onPackageArrived(Package pkg) {
		long stubId = pkg.getBody().getStubId();
		Register reg = register.get(stubId);

		if (reg != null) {
			reg.queue.add(pkg);
		}
	}

}
