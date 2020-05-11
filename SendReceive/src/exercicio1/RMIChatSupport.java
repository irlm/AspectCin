package exercicio1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIChatSupport extends Remote {

	int register() throws RemoteException;
	
	void send(Message message, int id) throws RemoteException;
	
	Message receive(int id) throws RemoteException;
	
}
