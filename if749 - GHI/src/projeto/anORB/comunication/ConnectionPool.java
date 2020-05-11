package projeto.anORB.comunication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

public class ConnectionPool {

	private static final int MAX_CONNECTIONS = 10;

	private static ConnectionPool singleton;

	public static ConnectionPool getSingleton() {
		if (singleton == null) {
			synchronized (ConnectionPool.class) {
				if (singleton == null) {
					singleton = new ConnectionPool();
				}
			}
		}
		return singleton;
	}

	private Hashtable<InetAddress, Sender> pool;
	private InetAddress[] ordem;
	private int nextIndex;

	private ConnectionPool() {
		pool = new Hashtable<InetAddress, Sender>(MAX_CONNECTIONS);
		ordem = new InetAddress[MAX_CONNECTIONS];
	}

	// TODO Criar exceção : um host, duas portas
	public Sender getConnection(InetAddress host, int port)
			throws IOException {
		Sender ret;

		if (pool.containsKey(host)) {
			ret = pool.get(host);
		} else {
			ret = new Sender();
			ret.connect(host, port);
			if (nextIndex == MAX_CONNECTIONS) {
				nextIndex = 0;
			}
			if (ordem[nextIndex] != null) {
				pool.remove(ordem[nextIndex]);
			}
			pool.put(host, ret);
			ordem[nextIndex] = host;
			nextIndex++;
		}
		return ret;
	}
}
