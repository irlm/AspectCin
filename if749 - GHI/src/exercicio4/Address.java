package exercicio4;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Address {
	
	private InetAddress addr;

	private int port = 0;

	public Address() {

	}

	public Address(int port, String hostname) throws UnknownHostException {
		super();
		this.port = port;
		addr = InetAddress.getByName(hostname);

	}

	public Address(String address) throws UnknownHostException {
		int colon = address.indexOf(':');
		if (colon != 0) {
			addr = InetAddress.getByName(address.substring(0, colon));
			address = address.substring(colon + 1);
		}

		this.port = Integer.parseInt(address);
	}

	public String getHostName() {
		return addr.getHostName();
	}

	public String getHostAddr() {
		return addr.toString();
	}

	public int getPortNumber() {
		return port;
	}

	public String toString() {
		return getHostAddr() + Integer.toString(port);
	}
}
