package projeto.anORB.comunication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import projeto.anORB.Package;

public class Receiver extends Thread {

	private ServerSocket serverSocket;

	private PackageHandler handler;

	public Receiver(int port, PackageHandler handler) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.handler = handler;
	}

	public void run() {
		while (true) {
			Socket s;
			try {
				s = serverSocket.accept();
				new ConnectionHandler(s, handler).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ConnectionHandler extends Thread {
		private Socket socket;

		private PackageHandler handler;

		public ConnectionHandler(Socket socket, PackageHandler handler) {
			this.socket = socket;
			this.handler = handler;
		}

		public void run() {
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Package ret = null;
				do {
					ret = (Package) ois.readObject();
					handler.onPackageArrived(ret);
				}while(ret != null);
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
