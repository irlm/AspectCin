package sendReceive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnection implements Connection {

	private Socket socket;

	public TCPConnection() {

		try {
			this.socket = new Socket("localhost", 1234);
		} catch (IOException e) {
			try {
				ServerSocket serverSocket = new ServerSocket(1234);
				this.socket = serverSocket.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void send(Mensagem mensagem) {

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			System.out.print("Enviando mensagem: '" + mensagem.getMensagem() + "'\n");
			out.print(mensagem.getMensagem());
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void receive(Mensagem mensagem) {

		try {

			BufferedReader inServer = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			while (!inServer.ready()) {
			}

			String ret = inServer.readLine();
			inServer.close();
			System.out.println("Recebendo mensagem = " + ret);
			mensagem.setMensagem(ret);

		} catch (IOException e) {

			e.printStackTrace();
		}
	
	}

}
