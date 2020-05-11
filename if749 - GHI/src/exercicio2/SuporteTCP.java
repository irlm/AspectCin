package exercicio2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SuporteTCP implements Runnable {

	private ServerSocket sc;

	private Demultiplexador demux;

	private Thread t;

	public SuporteTCP(int porta) throws IOException {
		this.sc = new ServerSocket(porta);
		this.sc.setSoTimeout(1000);
		this.demux = Demultiplexador.getSingleton();
		t = new Thread(this);
	}

	public void comecar() {
		t.start();
	}

	public void parar() {
		Thread thread = t;
		t = null;
		thread.interrupt();
	}

	public void run() {
		try {
			while (t != null) {
				try {
					Socket s = sc.accept();
					if (s != null) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(s.getInputStream()));
						String[] msg = br.readLine().split("\\|");
						demux.armazenar(new MensagemInfra(msg[0], new Mensagem(
								msg[1], msg[2])));
						s.close();
					}
				} catch (SocketTimeoutException e2) {

				}
			}
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
