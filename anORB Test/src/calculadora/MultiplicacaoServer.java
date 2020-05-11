package calculadora;

import java.io.IOException;

import anorb.AnORB;
import anorb.namingservice.NamingService;

public class MultiplicacaoServer {
	public static void main(String[] args) throws IOException {
		System.out.println("Server Multiplicacao - Porta 2170");
		
		AnORB anORB = AnORB.init(2170, args[0], 2178);
		
		NamingService ns = anORB.getNamingService();
		
		MultiplicacaoImpl impl = new MultiplicacaoImpl();
		
		ns.register("Multiplicacao", impl);

	}
}
