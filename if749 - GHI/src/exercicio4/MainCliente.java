package exercicio4;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainCliente {

	public void init(String hostname, int port) {
		try {
			
			Connector connector = new TCPConnector();
			connector.open(hostname, port);
			connector.connect(new HandlerCliente());
			
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (SocketException e) {
			System.err.println("Connection refused");
		} catch (InstantiationException e) {
			System.err.println(e);
		} catch (IllegalAccessException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void main(String[] args) {

		int port = 1234;// valor default
		MainCliente connectorTest = new MainCliente();

		if (args.length == 2) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println(e);
			}
		}

		connectorTest.init(args[0], port);
	}
}
