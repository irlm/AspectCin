package exercicio1;

import java.rmi.RemoteException;

public class RMIChatSupportImpl implements RMIChatSupport {

	private static final long serialVersionUID = -5768367889620292015L;
	
	private ChatServer server;

	public RMIChatSupportImpl(ChatServer server) throws RemoteException {
		this.server = server;
	}

	public void send(Message message, int id) throws RemoteException {
		this.server.publish(message, id);
	}

	public Message receive(int id) throws RemoteException {
		return server.getMessage(id);
	}

	public int register() throws RemoteException {
		return server.register();
	}

}
