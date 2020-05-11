package exercicio4;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class SOCKConnector {

	public SOCKConnector() {

	}

	public SOCKConnector(SOCKStream sockStream, String hostname, int port)
			throws SocketException, IOException {
		this.connect(sockStream, hostname, port);
	}

	public void connect(SOCKStream sockStream, String hostname, int port)
			throws SocketException, IOException {
		sockStream.socket(new Socket(hostname, port));
	}

	public void connect(SOCKStream sockStream, Address addr)
			throws SocketException, IOException {
		sockStream.socket(new Socket(addr.getHostName(), addr.getPortNumber()));
	}
}
