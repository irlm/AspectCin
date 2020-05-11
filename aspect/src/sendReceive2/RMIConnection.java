package sendReceive2;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIConnection implements Connection {

	private RMISuporte rmiSuporte;
	
	private Mensagem mensagem;
	
	public RMIConnection(){
		try {
			System.setSecurityManager(new RMISecurityManager());
			this.rmiSuporte = (RMISuporte) Naming.lookup("rmi://localhost/rmisuporte");
		} catch (Exception e) {
			try {
				Registry registry = LocateRegistry.getRegistry();
				this.rmiSuporte = new RMISuporteImpl();
				registry.bind("rmisuporte", this.rmiSuporte);
			} catch (Exception e1) {
				e1.printStackTrace();
			}			
		}	    		
	}

	public void send(Mensagem mensagem) {
		try {
			this.rmiSuporte.send(mensagem);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void receive(Mensagem mensagem) {
		try {
			this.rmiSuporte.receive(mensagem);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Mensagem getMensagem() {
		return mensagem;
	}

	public void setMensagem(Mensagem mensagem) {
		this.mensagem = mensagem;
	}
	
	

}
