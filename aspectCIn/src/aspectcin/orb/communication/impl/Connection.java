package aspectcin.orb.communication.impl;

import java.io.IOException;
import java.net.InetAddress;

import aspectcin.orb.communication.api.AnPackage;

public abstract class Connection {

	private InetAddress host;

	private int port;
	
	
	public Connection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public InetAddress getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public abstract void send(AnPackage p) throws IOException;

	public abstract void disconnect();
	
	public abstract boolean equals(Object o);
}
