package exercicio4;

import java.io.IOException;
import java.net.SocketException;

public interface Acceptor {

	public abstract void setHandlerFactory(Class handlerFactory);

	public abstract void open(int port) throws IOException;

	public abstract void accept() throws SocketException,
			InstantiationException, IllegalAccessException, IOException;

}