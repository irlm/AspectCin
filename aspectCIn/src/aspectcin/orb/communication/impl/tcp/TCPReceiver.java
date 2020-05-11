package aspectcin.orb.communication.impl.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import aspectcin.orb.PackageHandler;
import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.api.Receiver;

public class TCPReceiver extends Receiver{

	private ServerSocket serverSocket;

	private boolean fim;

	public TCPReceiver(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(500);
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public void run() {
		while (!fim) {
			Socket s;
			try {
				s = serverSocket.accept();

				new ConnectionHandler(s).start();
			} catch (SocketTimeoutException e) {
				// Tente novamente!
			} catch (IOException e) {

			}
		}

	}

	private class ConnectionHandler extends Receiver.ConnectionHandler {
		private Socket socket;

		public ConnectionHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {

				ObjectInputStream ois = new ObjectInputStream(socket
						.getInputStream());
				AnPackage ret = null;

				ret = (AnPackage) ois.readObject();

				//TODO como assim folhosos
				PackageHandler.getSingleton().onPackageArrived(ret);
				ois.close();
			} catch (SocketException e) {

			} catch (Exception e) {

			}
		}
	}

	public void stopRunning() {
		this.fim = true;
	}
}
