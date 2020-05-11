package aspectcin.orb.communication.impl.tcp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.impl.Connection;


public class TCPConnection extends Connection{

	private Socket socket;
	private ObjectOutputStream oos;

	public TCPConnection(InetAddress host, int port) throws IOException {
		super(host, port);
		this.socket = new Socket(host, port);
		this.oos = new ObjectOutputStream(socket.getOutputStream());
	}

	public void send(AnPackage p) throws IOException {
		oos.writeObject(p);
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {	}
	}
	
	public boolean equals(Object o){
		TCPConnection c = (TCPConnection) o;
		return c.socket.getInetAddress().equals(this.socket.getInetAddress()) && c.socket.getPort() == this.socket.getPort();
	}

}
