package exemplo;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class MensagemClient {

	public static void main(String[] args) {
		
		try {
			
			System.setSecurityManager(new RMISecurityManager());
			Mensagem mensagem = (Mensagem) Naming.lookup("rmi://localhost/mensagens");
			System.out.println(mensagem.getMensagem());
			System.out.println("Fim!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
