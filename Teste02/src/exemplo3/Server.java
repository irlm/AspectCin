package exemplo3;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

	public static void main(String[] args) {
		try {

			System.setSecurityManager(new RMISecurityManager());
			
			RMISuporte comunicacao = new RMISuporteImpl();

			Registry registry = LocateRegistry.getRegistry();
			registry.bind("comunicacao", comunicacao);

			Mensagem mensagem = new Mensagem("teste");

			System.out.println("Servidor no ar. "
					+ " Nome do Objeto servidor: '" + "comunicacao " + "'");

			comunicacao.receive(mensagem);
			System.out.println("Ja imprimiu " + mensagem.getMensagem());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
