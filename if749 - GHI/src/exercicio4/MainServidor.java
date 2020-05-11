package exercicio4;

import java.io.IOException;
import java.net.SocketException;

public class MainServidor {

	public void init(int port) {
		try {
			System.out.println("To rodando");
			Acceptor acceptor = new TCPAcceptor(Class
					.forName("exercicio4.HandlerServidor"));
			acceptor.open(port);
			while (true) {
				acceptor.accept();
			}
		} catch (ClassNotFoundException e) {
			System.err.println (e);
		} catch (SocketException e) {
			System.err.println ("Socket Exception: " + e);
		} catch (InstantiationException e) {
			System.err.println (e);
		} catch (IllegalAccessException e) {
			System.err.println (e);
		} catch (IOException e) {
			System.err.println (e);
		}
	}

	public static void main(String[] args) {
		
		int port = 1234;
		MainServidor acceptorTest = new MainServidor();

		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}
		acceptorTest.init(port);
	}
}
