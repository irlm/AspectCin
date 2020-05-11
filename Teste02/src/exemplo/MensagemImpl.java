package exemplo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MensagemImpl extends UnicastRemoteObject implements Mensagem {

	private static final long serialVersionUID = -6200510741014425965L;
	
	private String mensagem = "Inicial";
	
	public MensagemImpl() throws RemoteException { }
	
	public String getMensagem() throws RemoteException {
		return this.mensagem;
	}

	public void setMensagem(String msg) throws RemoteException {
		this.mensagem = msg;
	}

}
