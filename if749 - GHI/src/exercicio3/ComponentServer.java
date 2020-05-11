package exercicio3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Created on 02/11/2005
 */
public abstract class ComponentServer implements Component {
	private ServerSocket serverSocket;

	private boolean stop;

	private boolean suspended;

	public boolean init(ComponentRepository repository, Object... parameters) {
		boolean result = false;

		this.stop = false;
		this.suspended = false;

		if (parameters != null && parameters.length != 0) {
			try {
				this.serverSocket = new ServerSocket(Integer
						.parseInt((String) parameters[0]));
				result = true;
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		} else {
			throw new IllegalArgumentException("Invalid parameter.");
		}

		return result;
	}

	public void resume() {
		suspended = false;
	}

	public void suspend() {
		suspended = true;
	}

	public String info() {
		String status = "[Running]";

		if (suspended) {
			status = "[Suspended]";
		}

		return status + " " + getNomeServico() + " (port "
				+ serverSocket.getLocalPort() + ")";
	}

	protected abstract String getNomeServico();

	public void fini() {
		stop = true;

		try {
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public void run() {
		while (!stop) {
			try {
				if (!suspended) {
					Socket socket = serverSocket.accept();
					DataOutputStream dos = new DataOutputStream(socket
							.getOutputStream());
					dos.writeChars(getMessage());
					dos.close();
					socket.close();
				} else {
					Thread.sleep(300);
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
	}

	protected abstract String getMessage();

}
