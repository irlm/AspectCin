package exercicio1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPConnection implements Connection {

	private Socket skt;

	public TCPConnection(String host) {

		try {
			this.skt = new Socket("localhost", 1234);
		} catch (UnknownHostException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void send(Message message) {

		
		try {
			
			this.skt = new Socket("localhost", 1234);
			PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
			System.out.print("Sending string: '" + message.getMessage() + "'\n");
			out.print(message.getMessage());
			out.close();
			skt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Message receive() {

		try {

			BufferedReader inServer = new BufferedReader(new InputStreamReader(
					this.skt.getInputStream()));
			while (!inServer.ready()) {
			}

			String ret = inServer.readLine();
			System.out.println(ret); // Read one line and output it

			System.out.print("'\n");
			inServer.close();
			return new Message(ret);

		} catch (IOException e) {

			e.printStackTrace();
		}

		return null;
	}

}
