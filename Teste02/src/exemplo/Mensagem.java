package exemplo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Mensagem extends Remote {

	public String getMensagem() throws RemoteException;
	
	public void setMensagem(String msg) throws RemoteException;
}
