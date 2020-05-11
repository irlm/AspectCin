package exemplo;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MensagemServer {

	public static void main(String[] args) {
		try {
			
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("mensagens", new MensagemImpl());

			System.out.println("Servidor no ar. "
					+ " Nome do Objeto servidor: '" + "mensagens " + "'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
