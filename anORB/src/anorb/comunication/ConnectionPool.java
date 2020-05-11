package anorb.comunication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

public class ConnectionPool {

	private static final int MAX_CONNECTIONS = 10;

	private Hashtable<ConnectionKey, Connection> pool;
	private ConnectionKey[] ordem;
	private int nextIndex;

	public ConnectionPool() {
		pool = new Hashtable<ConnectionKey, Connection>(MAX_CONNECTIONS);
		ordem = new ConnectionKey[MAX_CONNECTIONS];
	}

	public Connection getConnection(InetAddress host, int port) throws IOException {
		Connection ret;

		ConnectionKey con = new ConnectionKey(host, port);
		
		if (pool.containsKey(con)) {
			ret = pool.get(con);
		} else {
			ret = new Connection();
			ret.connect(host, port);
			if (nextIndex == MAX_CONNECTIONS) {
				nextIndex = 0;
			}
			if (ordem[nextIndex] != null) {
				pool.remove(ordem[nextIndex]);
			}
			pool.put(con, ret);
			ordem[nextIndex] = con;
			nextIndex++;
		}
		return ret;
	}
	
	private class ConnectionKey {
		InetAddress host;
		int port;
		
		public ConnectionKey(InetAddress host, int port){
			this.host = host;
			this.port = port;			
		}
		
		public boolean equals(Object obj){
			ConnectionKey c = (ConnectionKey) obj;
			
			return host.equals(c.host) && port == c.port;
		}
	}
}
