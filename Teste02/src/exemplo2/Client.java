package exemplo2;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class Client {

public static void main(String[] args) {
		
		try {
			
			System.setSecurityManager(new RMISecurityManager());
			Comunicacao comunicacao = (Comunicacao) Naming.lookup("rmi://localhost/comunicacao");
			
			Mensagem mensagem = new Mensagem("Opa epa");
			
			System.out.println("Send");
			comunicacao.send(mensagem);
			System.out.println(mensagem.getMensagem());
			
			/*System.out.println("Receive");
			comunicacao.receive(mensagem);
			System.out.println(mensagem.getMensagem());*/
			
			System.out.println("Fim!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
