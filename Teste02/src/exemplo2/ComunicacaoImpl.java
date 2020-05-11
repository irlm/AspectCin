package exemplo2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ComunicacaoImpl extends UnicastRemoteObject implements Comunicacao {

	private static final long serialVersionUID = 1291073017550497405L;

	private Mensagem mensagem;
	private static boolean recebeu = false;
	
	public ComunicacaoImpl()  throws RemoteException{
		this.mensagem = new Mensagem("");
	}
	
	public void receive(Mensagem mensagem)  throws RemoteException {
		
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
		ComunicacaoImpl.recebeu = recebeu;
	}	

}
