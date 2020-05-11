package exemplo3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISuporte extends Remote {

	void send(Mensagem mensagem) throws RemoteException;

	void receive(Mensagem mensagem) throws RemoteException;
}
