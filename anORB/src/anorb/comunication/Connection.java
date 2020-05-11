package anorb.comunication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import anorb.logging.AnLog;

public class Connection {

	private Socket socket;
	private ObjectOutputStream oos;

	private Logger log;

	public void connect(InetAddress host, int port) throws IOException {
		this.log = AnLog.comunication;
		log.fine("Trying to connect to " + host + ":" + port);
		this.socket = new Socket(host, port);
		this.oos = new ObjectOutputStream(socket.getOutputStream());
		log.fine("Connected to " + host + ":" + port);
	}

	public void send(AnPackage p) throws IOException {
		oos.writeObject(p);
		log.fine("Sender sent a package");
		log.finest(p.toString());
	}

	public void disconnect() {
		try {
			socket.close();
			log.fine("Connection closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean equals(Object o){
		Connection c = (Connection) o;
		return c.socket.getInetAddress().equals(this.socket.getInetAddress()) && c.socket.getPort() == this.socket.getPort();
	}

}
