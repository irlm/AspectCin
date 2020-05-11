package exemplo3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class RMISuporteImpl extends UnicastRemoteObject implements RMISuporte{

	private static final long serialVersionUID = 1291073017550497405L;

	private Mensagem mensagem;
	private static boolean recebeu = false;
	
	public RMISuporteImpl()  throws RemoteException{
		this.mensagem = new Mensagem("");
	}
	
	public void receive(Mensagem mensagem) throws RemoteException {
		
		MyThread myThread =  new MyThread();
		myThread.run();
		
		while (myThread.isAlive()) {
			System.out.println("alive");
		}
		System.out.println("nops alive");
		
		mensagem.setMensagem(this.mensagem.getMensagem());
	}

	public void send(Mensagem mensagem)  throws RemoteException {
		recebeu = true;
		this.mensagem.setMensagem(mensagem.getMensagem());
	}

	public static boolean isRecebeu() {
		return recebeu;
	}

	public static void setRecebeu(boolean recebeu) {
		RMISuporteImpl.recebeu = recebeu;
	}

}
