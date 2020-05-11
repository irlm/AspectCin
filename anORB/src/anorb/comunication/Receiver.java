package anorb.comunication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import anorb.logging.AnLog;

public class Receiver extends Thread {

	private ServerSocket serverSocket;

	private PackageHandler handler;

	private Logger log;

	private boolean fim;

	public Receiver(int port, PackageHandler handler) throws IOException {
		this.log = AnLog.comunication;
		log.fine("Creating receiver on port " + port);
		this.serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(500);
		this.handler = handler;
		log.fine("Receiver created sucessfully");
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public void run() {
		log.fine("Starting receiver");
		while (!fim) {
			Socket s;
			try {
				s = serverSocket.accept();

				log.finest("Connection accepted on port " + s.getLocalPort());
				new ConnectionHandler(s, handler).start();
			} catch (SocketTimeoutException e) {
				// Tente novamente!
			} catch (IOException e) {
				log.log(Level.SEVERE,
						"SEVERE ERROR anorb.comunication.Receiver - 01", e);
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
				log.finest("Trying to read object from socket on port "
						+ socket.getLocalPort());
				ObjectInputStream ois = new ObjectInputStream(socket
						.getInputStream());
				AnPackage ret = null;

				ret = (AnPackage) ois.readObject();
				log.finest("Package read");
				if (ret != null) {
					log.finest(ret.toString());
				}
				handler.onPackageArrived(ret);
				ois.close();
			} catch (SocketException e) {
				log.warning("Conection lost");
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"SEVERE ERROR anorb.comunication.Receiver - 02", e);
			}
		}
	}

	public void stopRunning() {
		this.fim = true;
	}
}
