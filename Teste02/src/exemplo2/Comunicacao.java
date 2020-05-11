package exemplo2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Comunicacao extends Remote {

	void send(Mensagem mensagem) throws RemoteException;

	void receive(Mensagem mensagem) throws RemoteException;

}
