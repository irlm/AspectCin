package projeto.anORB.comunication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import projeto.anORB.Package;

public class Sender {
	
	private Socket socket;
	private ObjectOutputStream oos;

	public void connect(InetAddress host, int port) throws IOException {
		this.socket = new Socket(host, port);
		this.oos = new ObjectOutputStream(socket.getOutputStream());
	}

	public void send(Package p) throws IOException {		
		oos.writeObject(p);
	}	

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
