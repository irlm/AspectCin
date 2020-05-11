


public abstract class Connection {

	private String host;

	private int port;
	
	
	public Connection(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public abstract void send(AnPackage p);

	public abstract void disconnect();
	
	public abstract boolean equals(Object o);
}
