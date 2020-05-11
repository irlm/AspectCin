package exercicio1;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIConnection implements Connection {

	private int id;
	private RMIChatSupport rmiChatSupport;
	
	public RMIConnection(String host){
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			this.rmiChatSupport = (RMIChatSupport) registry.lookup("RMIChatSupport");
			this.id = rmiChatSupport.register();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}	    		
	}

	public void send(Message message) {
		try {
			rmiChatSupport.send(message, id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Message receive() {
		Message retorno = null;
		
		while (retorno == null)
		{
			try {
				retorno = rmiChatSupport.receive(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		return retorno;
	}

}
