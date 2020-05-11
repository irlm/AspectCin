package exercicio4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class SOCKAcceptor {

	private ServerSocket listenSocket;

	public SOCKAcceptor() {
	}

	public SOCKAcceptor(int port) throws IOException {
		this.open(port);
	}

	public void open(int port) throws IOException {

		this.close();

		this.listenSocket = new ServerSocket(port);

	}

	public void close() throws IOException {
		if (this.listenSocket != null) {
			this.listenSocket.close();
			this.listenSocket = null;
		}
	}

	public void accept(SOCKStream sockStream) throws SocketException,
			IOException {

		sockStream.socket(this.listenSocket.accept());

	}

	public ServerSocket listenSocket() {
		return this.listenSocket;
	}

	public void listenSocket(ServerSocket s) {
		this.listenSocket = s;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.close();
	}

}
