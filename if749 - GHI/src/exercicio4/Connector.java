package exercicio4;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public interface Connector {

	public abstract void open(String hostname, int port);

	public abstract void connect(ServiceHandler sh) throws UnknownHostException,
			SocketException, InstantiationException, IllegalAccessException,
			IOException;

}